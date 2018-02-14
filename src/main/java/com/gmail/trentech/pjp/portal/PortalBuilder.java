package com.gmail.trentech.pjp.portal;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.events.ConstructPortalEvent;

public class PortalBuilder {

	private boolean valid = false;
	private Portal portal;

	public PortalBuilder(Portal portal, Location<World> location, Direction direction) {
		if (portal.getProperties().isPresent()) {
			this.portal = portal;

			if (direction.equals(Direction.NORTH)) {
				direction = Direction.EAST;
			} else if (direction.equals(Direction.EAST)) {
				direction = Direction.SOUTH;
			} else if (direction.equals(Direction.SOUTH)) {
				direction = Direction.WEST;
			} else if (direction.equals(Direction.WEST)) {
				direction = Direction.NORTH;
			}

			findPortal(location, direction, Direction.UP);

			if (!isValid()) {
				portal.getProperties().get().getFill().clear();
				portal.getProperties().get().getFrame().clear();

				if (direction.equals(Direction.NORTH) || direction.equals(Direction.SOUTH)) {
					findPortal(location, direction, Direction.EAST);

					if (!isValid()) {
						portal.getProperties().get().getFill().clear();
						portal.getProperties().get().getFrame().clear();

						findPortal(location, direction, Direction.WEST);
					}
				} else {
					findPortal(location, direction, Direction.NORTH);

					if (!isValid()) {
						portal.getProperties().get().getFill().clear();
						portal.getProperties().get().getFrame().clear();

						findPortal(location, direction, Direction.SOUTH);
					}
				}
			}
		}
	}

	public boolean isValid() {
		return valid;
	}

	public boolean spawnPortal(Cause cause) {
		if (isValid()) {
			if (!Sponge.getEventManager().post(new ConstructPortalEvent(portal.getProperties().get().getFrame(), portal.getProperties().get().getFill(), cause))) {
				portal.setProperties(portal.getProperties().get());

				Sponge.getServiceManager().provide(PortalService.class).get().create(portal, portal.getName());
				
				return true;
			}
		}

		return false;
	}

	private void findPortal(Location<World> location, Direction horizonal, Direction vertical) {
		scanEdge(location, horizonal, vertical, false);

		for (int i = 1; i < 50; ++i) {
			location = location.getRelative(vertical);

			if (!scanLine(location, horizonal, vertical, i == 1)) {
				break;
			}
		}

		if (isValid()) {
			scanEdge(location, horizonal, vertical, true);

			for (Location<World> loc : portal.getProperties().get().getFill()) {
				List<Location<World>> list = new ArrayList<>();

				list.add(loc.getRelative(vertical));
				list.add(loc.getRelative(vertical.getOpposite()));
				list.add(loc.getRelative(horizonal));
				list.add(loc.getRelative(horizonal.getOpposite()));

				for (Location<World> loc2 : list) {
					BlockState state = loc2.getBlock();

					if ((state.getType().equals(BlockTypes.AIR))) {
						if (!portal.getProperties().get().getFill().contains(loc2)) {
							valid = false;
							break;
						}
					}
				}
			}
		}
	}

	private void scanEdge(Location<World> location, Direction direction, Direction vertical, boolean top) {
		Location<World> loc = location;

		if (top) {
			vertical = vertical.getOpposite();
		}

		for (int i = 1; i < 25; ++i) {
			BlockType blockType = loc.getBlock().getType();

			if (blockType.equals(BlockTypes.AIR)) {
				break;
			}

			if (!portal.getProperties().get().getFrame().contains(loc)) {
				portal.getProperties().get().getFrame().add(loc);
			}

			if (!loc.getRelative(vertical).getBlock().getType().equals(BlockTypes.AIR)) {
				break;
			}

			loc = loc.getRelative(direction);
		}

		loc = location;

		for (int i = 1; i < 25; ++i) {
			BlockType blockType = loc.getBlock().getType();

			if (blockType.equals(BlockTypes.AIR)) {
				break;
			}

			if (!portal.getProperties().get().getFrame().contains(loc)) {
				portal.getProperties().get().getFrame().add(loc);
			}

			if (!loc.getRelative(vertical).getBlock().getType().equals(BlockTypes.AIR)) {
				break;
			}

			loc = loc.getRelative(direction.getOpposite());
		}
	}

	private boolean scanDirection(Location<World> location, Direction direction, Direction vertical, boolean first) {
		boolean valid = true;

		for (int i = 1; i < 25; ++i) {
			BlockState state = location.getBlock();

			if (state.getType().equals(BlockTypes.AIR)) {
				if (!portal.getProperties().get().getFill().contains(location)) {
					portal.getProperties().get().getFill().add(location);
				}
				if (first) {
					if (location.getRelative(vertical.getOpposite()).getBlock().getType().equals(BlockTypes.AIR)) {
						this.valid = false;
						return false;
					}
				} else if (location.getRelative(vertical).getBlock().getType().equals(BlockTypes.AIR)) {
					valid = false;
				}

				location = location.getRelative(direction);
			} else {
				if (i != 1) {
					if (!portal.getProperties().get().getFrame().contains(location)) {
						portal.getProperties().get().getFrame().add(location);
					}
					break;
				} else {
					return false;
				}
			}
		}
		this.valid = valid;
		return true;
	}

	private boolean scanLine(Location<World> location, Direction direction, Direction vertical, boolean first) {
		if (scanDirection(location, direction.getOpposite(), vertical, first)) {
			if (!isValid()) {
				if (scanDirection(location, direction, vertical, first)) {
					valid = false;
					return true;
				}
				valid = false;
			} else {
				if (scanDirection(location, direction, vertical, first)) {
					return true;
				}
			}
		}
		return false;
	}
}