package com.gmail.trentech.pjp.portal.features;

import static org.spongepowered.api.data.DataQuery.of;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

public class Coordinate implements DataSerializable {

	private static final DataQuery WORLD = of("world");
	private static final DataQuery VECTOR3D = of("vector3d");
	private static final DataQuery PRESET = of("preset");
	private static final DataQuery RANDOM = of("random");
	private static final DataQuery BED_RESPAWN = of("bedrespawn");
	
	private String world;
	private Optional<Vector3d> vector3d = Optional.empty();
	private Preset preset = Preset.NONE;
	
	public Coordinate(String world, Vector3d vector3d) {
		this.world = world;
		this.vector3d = Optional.of(vector3d);
	}
	
	public Coordinate(String world, Preset preset) {
		this.world = world;		
		this.preset = preset;
	}
	
	public Coordinate(Location<World> location) {
		this.world = location.getExtent().getName();
		this.vector3d = Optional.of(location.getPosition());
	}
	
	public String getWorld() {
		return world;
	}

	public Optional<World> getOptionalWorld() {
		return Sponge.getServer().getWorld(world);
	}
	
	public Preset getPreset() {
		return preset;
	}

	public Optional<Location<World>> getLocation() {
		return getOptionalWorld().map(world -> {
			if (vector3d.isPresent()) {
				return new Location<World>(world, this.vector3d.get());
			} else {
				return world.getSpawnLocation();
			}
		});
	}
	
	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	public DataContainer toContainer() {
		DataContainer dataContainer = DataContainer.createNew().set(WORLD, world).set(PRESET, preset.getName());
		
		if(vector3d.isPresent()) {
			dataContainer.set(VECTOR3D, DataTranslators.VECTOR_3_D.translate(vector3d.get()));
		}
		
		return dataContainer;
	}
	
	public static class Builder extends AbstractDataBuilder<Coordinate> {

		public Builder() {
			super(Coordinate.class, 0);
		}

		@Override
		protected Optional<Coordinate> buildContent(DataView container) throws InvalidDataException {
			if (container.contains(WORLD)) {
				Optional<String> optionalWorld = container.getString(WORLD);

				if (!optionalWorld.isPresent()) {
					return Optional.empty();
				}
				
				if (container.contains(VECTOR3D)) {
					Vector3d vector3d = DataTranslators.VECTOR_3_D.translate(container.getView(VECTOR3D).get());
					
					return Optional.of(new Coordinate(optionalWorld.get(), vector3d));
				}
				
				Preset preset;
				
				if(container.contains(RANDOM)) {
					if(container.getBoolean(RANDOM).get()) {
						preset = Preset.RANDOM;
					} else if(container.getBoolean(BED_RESPAWN).get()){
						preset = Preset.BED;
					} else {
						preset = Preset.NONE;
					}
				} else {
					preset = Preset.get(container.getString(PRESET).get());
				}

				return Optional.of(new Coordinate(optionalWorld.get(), preset));
			}

			return Optional.empty();
		}
	}
	
	public enum Preset {

		RANDOM("random"), BED("bed"), LAST_LOCATION("last_location"), NONE("none");

		private final String name;

		private Preset(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static Preset get(String name) {
			Preset[] presets = Preset.values();

			for (Preset preset : presets) {
				if (preset.getName().equals(name.toLowerCase())) {
					return preset;

				}
			}

			return null;
		}
	}
}
