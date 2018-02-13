package com.gmail.trentech.pjp.portal.features;

import static com.gmail.trentech.pjp.data.DataQueries.COLOR;
import static com.gmail.trentech.pjp.data.DataQueries.FILL;
import static com.gmail.trentech.pjp.data.DataQueries.FRAME;
import static com.gmail.trentech.pjp.data.DataQueries.PARTICLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.effects.Particle;
import com.gmail.trentech.pjp.effects.ParticleColor;
import com.gmail.trentech.pjp.effects.Particles;

public class Properties implements DataSerializable {

	private List<Location<World>> frame = new ArrayList<>();
	private List<Location<World>> fill = new ArrayList<>();
	private Particle particle;
	private Optional<ParticleColor> color;

	public Properties(Particle particle, Optional<ParticleColor> color) {
		this.particle = particle;
		this.color = color;
	}

	public Properties(List<Location<World>> frame, List<Location<World>> fill, Particle particle, Optional<ParticleColor> color) {
		this.frame = frame;
		this.fill = fill;
		this.particle = particle;
		this.color = color;
	}

	public Particle getParticle() {
		return particle;
	}

	public void setParticle(Particle particle) {
		this.particle = particle;
	}

	public Optional<ParticleColor> getParticleColor() {
		return color;
	}

	public void setParticleColor(Optional<ParticleColor> color) {
		this.color = color;
	}

	public List<Location<World>> getFrame() {
		return frame;
	}

	public void addFrame(Location<World> location) {
		frame.add(location);
	}

	public void removeFrame(Location<World> location) {
		frame.remove(location);
	}

	public List<Location<World>> getFill() {
		return fill;
	}

	public void addFill(Location<World> location) {
		fill.add(location);
	}

	public void removeFill(Location<World> location) {
		fill.remove(location);
	}

	private void updateClient(Player player, boolean reset) {
		BlockState state = getBlock();
		
		Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(c -> {
			for (Location<World> location : getFill()) {
				Optional<Chunk> optionalChunk = location.getExtent().getChunk(location.getChunkPosition());
				
				if(optionalChunk.isPresent() && optionalChunk.get().isLoaded()) {
					if (reset) {
						player.resetBlockChange(location.getBlockPosition());
					} else {
						player.sendBlockChange(location.getBlockPosition(), state);
					}
				}
			}
		}).submit(Main.getPlugin());
	}

	public void blockUpdate(boolean reset) {
		World world = getFrame().get(0).getExtent();

		Predicate<Entity> filter = e -> {
			return e.getType().equals(EntityTypes.PLAYER);
		};

		for (Entity entity : world.getEntities(filter)) {
			updateClient((Player) entity, reset);
		}
	}

	private BlockState getBlock() {
		if(getParticle().getName().equalsIgnoreCase("PORTAL_SHIMMER")) {
			BlockState blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();

			List<Vector3i> frameV = new ArrayList<>();

			for (Location<World> location : getFrame()) {
				frameV.add(location.getBlockPosition());
			}

			for (Location<World> location : getFill()) {
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
		} else if(getParticle().getName().equalsIgnoreCase("WATER_FLOW")) {
			return BlockTypes.WATER.getDefaultState();
		} else if(getParticle().getName().equalsIgnoreCase("LAVA_FLOW")) {
			return BlockTypes.LAVA.getDefaultState();
		}
		
		return BlockTypes.STONE.getDefaultState();
	}

	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	public DataContainer toContainer() {
		DataContainer container = DataContainer.createNew().set(PARTICLE, particle.getName());

		if (color.isPresent()) {
			container.set(COLOR, color.get().getName());
		}

		List<String> frame = new ArrayList<>();

		for (Location<World> location : this.frame) {
			frame.add(Coordinate.serialize(location));
		}
		container.set(FRAME, frame);

		List<String> fill = new ArrayList<>();

		for (Location<World> location : this.fill) {
			fill.add(Coordinate.serialize(location));
		}
		container.set(FILL, fill);

		return container;
	}

	public static class Builder extends AbstractDataBuilder<Properties> {

		public Builder() {
			super(Properties.class, 0);
		}

		@Override
		protected Optional<Properties> buildContent(DataView container) throws InvalidDataException {
			if (container.contains(FRAME, FILL, PARTICLE)) {
				Particle particle = Particles.get(container.getString(PARTICLE).get()).get();
				Optional<ParticleColor> color = Optional.empty();
				List<Location<World>> frame = new ArrayList<>();
				List<Location<World>> fill = new ArrayList<>();

				if (container.contains(COLOR)) {
					color = ParticleColor.get(container.getString(COLOR).get());
				}

				for (String loc : container.getStringList(FRAME).get()) {
					frame.add(Coordinate.deserialize(loc).get());
				}

				for (String loc : container.getStringList(FILL).get()) {
					fill.add(Coordinate.deserialize(loc).get());
				}

				return Optional.of(new Properties(frame, fill, particle, color));
			}

			return Optional.empty();
		}
	}
}
