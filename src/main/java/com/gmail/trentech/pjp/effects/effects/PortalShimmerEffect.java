package com.gmail.trentech.pjp.effects.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.features.Properties;

public class PortalShimmerEffect implements Effect {

	private long time = 40;

	@Override
	public void activate(Portal portal) {
		Optional<Properties> optionalProperties = portal.getProperties();
		
		if(!optionalProperties.isPresent()) {
			return;
		}
		Properties properties = optionalProperties.get();
		
		Sponge.getScheduler().createTaskBuilder().intervalTicks(time).name(portal.getName()).execute(t -> {
			World world = properties.getFrame().get(0).getExtent();

			Predicate<Entity> filter = e -> {
				return e.getType().equals(EntityTypes.PLAYER);
			};

			for (Entity entity : world.getEntities(filter)) {
				BlockState state = getBlock(properties.getFrame(), properties.getFill());
				
				Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(c -> {
					for (Location<World> location : properties.getFill()) {
						Optional<Chunk> optionalChunk = location.getExtent().getChunk(location.getChunkPosition());
						
						if(optionalChunk.isPresent() && optionalChunk.get().isLoaded()) {
							((Player) entity).sendBlockChange(location.getBlockPosition(), state);
						}
					}
				}).submit(Main.getPlugin());
			}
			portal.getProperties().get().blockUpdate(false);
		}).submit(Main.getPlugin());
	}

	@Override
	public void burst(Location<World> location, boolean player) {
		return;
	}

	private BlockState getBlock(List<Location<World>> frame, List<Location<World>> fill) {
		BlockState blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();

		List<Vector3i> frameV = new ArrayList<>();

		for (Location<World> location : frame) {
			frameV.add(location.getBlockPosition());
		}

		for (Location<World> location : fill) {
			Location<World> east = location.getRelative(Direction.EAST);
			Location<World> west = location.getRelative(Direction.WEST);
			Location<World> north = location.getRelative(Direction.NORTH);
			Location<World> south = location.getRelative(Direction.SOUTH);
			Location<World> up = location.getRelative(Direction.UP);
			Location<World> down = location.getRelative(Direction.DOWN);

			if (frameV.contains(east.getBlockPosition()) && frameV.contains(up.getBlockPosition()) && !frameV.contains(north.getBlockPosition()) && !frameV.contains(south.getBlockPosition())) {
				blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.X).get();
				break;
			} else if (frameV.contains(west.getBlockPosition()) && frameV.contains(up.getBlockPosition()) && !frameV.contains(north.getBlockPosition()) && !frameV.contains(south.getBlockPosition())) {
				blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.X).get();
				break;
			} else if (frameV.contains(east.getBlockPosition()) && frameV.contains(down.getBlockPosition()) && !frameV.contains(north.getBlockPosition()) && !frameV.contains(south.getBlockPosition())) {
				blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.X).get();
				break;
			} else if (frameV.contains(west.getBlockPosition()) && frameV.contains(down.getBlockPosition()) && !frameV.contains(north.getBlockPosition()) && !frameV.contains(south.getBlockPosition())) {
				blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.X).get();
				break;
			} else if (frameV.contains(north.getBlockPosition()) && frameV.contains(up.getBlockPosition()) && !frameV.contains(east.getBlockPosition()) && !frameV.contains(west.getBlockPosition())) {
				blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();
				break;
			} else if (frameV.contains(south.getBlockPosition()) && frameV.contains(up.getBlockPosition()) && !frameV.contains(east.getBlockPosition()) && !frameV.contains(west.getBlockPosition())) {
				blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();
				break;
			} else if (frameV.contains(north.getBlockPosition()) && frameV.contains(down.getBlockPosition()) && !frameV.contains(east.getBlockPosition()) && !frameV.contains(west.getBlockPosition())) {
				blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();
				break;
			} else if (frameV.contains(south.getBlockPosition()) && frameV.contains(down.getBlockPosition()) && !frameV.contains(east.getBlockPosition()) && !frameV.contains(west.getBlockPosition())) {
				blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();
				break;
			} else if (frameV.contains(east.getBlockPosition()) && frameV.contains(north.getBlockPosition()) && !frameV.contains(up.getBlockPosition()) && !frameV.contains(down.getBlockPosition())) {
				blockState = BlockTypes.END_PORTAL.getDefaultState();
				break;
			} else if (frameV.contains(west.getBlockPosition()) && frameV.contains(north.getBlockPosition()) && !frameV.contains(up.getBlockPosition()) && !frameV.contains(down.getBlockPosition())) {
				blockState = BlockTypes.END_PORTAL.getDefaultState();
				break;
			} else if (frameV.contains(east.getBlockPosition()) && frameV.contains(south.getBlockPosition()) && !frameV.contains(up.getBlockPosition()) && !frameV.contains(down.getBlockPosition())) {
				blockState = BlockTypes.END_PORTAL.getDefaultState();
				break;
			} else if (frameV.contains(west.getBlockPosition()) && frameV.contains(south.getBlockPosition()) && !frameV.contains(up.getBlockPosition()) && !frameV.contains(down.getBlockPosition())) {
				blockState = BlockTypes.END_PORTAL.getDefaultState();
				break;
			}
		}
		
		return blockState;
	}

	@Override
	public void deactivate(Portal portal) {
		for (Task task : Sponge.getScheduler().getScheduledTasks()) {
			if (task.getName().equals(portal.getName())) {
				task.cancel();
				break;
			}
		}
	}
}
