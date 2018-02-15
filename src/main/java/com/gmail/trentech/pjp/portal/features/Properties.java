package com.gmail.trentech.pjp.portal.features;

import static com.gmail.trentech.pjp.data.DataQueries.BLOCKSTATE;
import static com.gmail.trentech.pjp.data.DataQueries.FILL;
import static com.gmail.trentech.pjp.data.DataQueries.FRAME;
import static com.gmail.trentech.pjp.data.DataQueries.PARTICLE;
import static org.spongepowered.api.data.DataQuery.of;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Properties implements DataSerializable {

	private static final DataQuery LOC = of("location");
	private static final DataQuery INTENSITY = of("intensity");
	
	private List<Location<World>> frame = new ArrayList<>();
	private List<Location<World>> fill = new ArrayList<>();
	private Optional<ParticleEffect> particle = Optional.empty();
	private Optional<BlockState> blockState = Optional.empty();
	private int intensity = 40;
	
	public Properties(Optional<ParticleEffect> particle, Optional<BlockState> blockState, int intensity) {
		this.particle = particle;
		this.blockState = blockState;
		this.intensity = intensity;
	}

	public Properties(List<Location<World>> frame, List<Location<World>> fill, Optional<ParticleEffect> particle, Optional<BlockState> blockState, int intensity) {
		this.frame = frame;
		this.fill = fill;
		this.particle = particle;
		this.blockState = blockState;
		this.intensity = intensity;
	}

	public Properties() {

	}

	public Optional<ParticleEffect> getParticle() {
		return particle;
	}

	public void setParticle(Optional<ParticleEffect> particle) {
		this.particle = particle;
	}

	public int getIntensity() {
		return intensity;
	}
	
	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}
	
	public Optional<BlockState> getBlockState() {
		return blockState;
	}

	public void setBlockState(Optional<BlockState> blockState) {
		this.blockState = blockState;
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

	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	public DataContainer toContainer() {
		DataContainer container = DataContainer.createNew().set(INTENSITY, intensity);
		
		if(particle.isPresent()) {
			container.set(PARTICLE, particle.get());
		}

		if (blockState.isPresent()) {
			container.set(BLOCKSTATE, blockState.get());
		}

		List<DataView> frame = new LinkedList<>();

		for (Location<World> location :  this.frame) {
			frame.add(DataContainer.createNew().set(LOC, new Coordinate(location).toContainer()));
		}
		container.set(FRAME, frame);

		List<DataView> fill = new LinkedList<>();

		for (Location<World> location :  this.fill) {
			fill.add(DataContainer.createNew().set(LOC, new Coordinate(location).toContainer()));
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
			int intensity = 40;
			Optional<ParticleEffect> particle = Optional.empty();
			Optional<BlockState> blockState = Optional.empty();
			List<Location<World>> frame = new ArrayList<>();
			List<Location<World>> fill = new ArrayList<>();
			
			if (container.contains(INTENSITY)) {
				intensity = container.getInt(INTENSITY).get();
			}

			if (container.contains(FRAME)) {
				for (DataView data : container.getViewList(FRAME).get()) {
					frame.add(Sponge.getDataManager().deserialize(Coordinate.class, data.getView(LOC).get()).get().getLocation().get());
				}
			}

			if (container.contains(FILL)) {
				for (DataView data : container.getViewList(FILL).get()) {
					frame.add(Sponge.getDataManager().deserialize(Coordinate.class, data.getView(LOC).get()).get().getLocation().get());
				}
			}


			if (container.contains(PARTICLE)) {
				particle = container.getSerializable(PARTICLE, ParticleEffect.class);
			}

			if (container.contains(BLOCKSTATE)) {			
				blockState = container.getSerializable(BLOCKSTATE, BlockState.class);
			}

			
			if (container.contains(FILL)) {
				for (DataView data : container.getViewList(FILL).get()) {
					fill.add(Sponge.getDataManager().deserialize(Coordinate.class, data.getView(LOC).get()).get().getLocation().get());
				}
			}

			return Optional.of(new Properties(frame, fill, particle, blockState, intensity));
		}
	}
}
