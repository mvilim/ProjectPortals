package com.gmail.trentech.pjp.portal.features;

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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.effects.Effect;

public class Properties implements DataSerializable {

	private static final DataQuery LOC = of("location");
	private static final DataQuery FRAME = of("frame");
	private static final DataQuery FILL = of("fill");
	private static final DataQuery EFFECT = of("effect");
	private static final DataQuery BLOCKSTATE = of("block");
	
	private List<Location<World>> frame = new ArrayList<>();
	private List<Location<World>> fill = new ArrayList<>();
	private Optional<Effect> effect = Optional.empty();
	private Optional<BlockState> blockState = Optional.empty();

	public Properties(Optional<Effect> effect, Optional<BlockState> blockState) {
		this.effect = effect;
		this.blockState = blockState;
	}

	public Properties(List<Location<World>> frame, List<Location<World>> fill, Optional<Effect> effect, Optional<BlockState> blockState) {
		this.frame = frame;
		this.fill = fill;
		this.effect = effect;
		this.blockState = blockState;
	}

	public Properties() {

	}

	public Optional<Effect> getEffect() {
		return effect;
	}

	public void setEffect(Optional<Effect> effect) {
		this.effect = effect;
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
		DataContainer container = DataContainer.createNew();
		
		if(effect.isPresent()) {
			container.set(EFFECT, effect.get());
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
			Optional<Effect> effect = Optional.empty();
			Optional<BlockState> blockState = Optional.empty();
			List<Location<World>> frame = new ArrayList<>();
			List<Location<World>> fill = new ArrayList<>();

			if (container.contains(FRAME)) {
				for (DataView data : container.getViewList(FRAME).get()) {
					frame.add(Sponge.getDataManager().deserialize(Coordinate.class, data.getView(LOC).get()).get().getLocation().get());
				}
			}

			if (container.contains(FILL)) {
				for (DataView data : container.getViewList(FILL).get()) {
					fill.add(Sponge.getDataManager().deserialize(Coordinate.class, data.getView(LOC).get()).get().getLocation().get());
				}
			}

			if (container.contains(EFFECT)) {
				effect = container.getSerializable(EFFECT, Effect.class);
			}

			if (container.contains(BLOCKSTATE)) {			
				blockState = container.getSerializable(BLOCKSTATE, BlockState.class);
			}

			return Optional.of(new Properties(frame, fill, effect, blockState));
		}
	}
}
