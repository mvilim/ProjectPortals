package com.gmail.trentech.pjp.effects;

import static org.spongepowered.api.data.DataQuery.of;

import java.util.Optional;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjp.utils.InvalidEffectException;

public class Effect implements DataSerializable {

	private static final DataQuery PARTICLE = of("particle");
	private static final DataQuery INTENSITY = of("intensity");
	private static final DataQuery OPTION = of("option");
	private static final DataQuery VALUE = of("value");
	
	private String particle;
	private int intensity = 40;
	private Optional<String> option = Optional.empty();
	private Optional<String> value = Optional.empty();
	
	public Effect(String particle) {
		this.particle = particle;
	}
	
	public Effect(String particle, int intensity) {
		this.particle = particle;
		this.intensity = intensity;
	}
	
	public Effect(String particle, String option, String value) {
		this.particle = particle;
		this.option = Optional.of(option);
		this.value = Optional.of(value);
	}
	
	public Effect(String particle, int intensity, String option, String value) {
		this.particle = particle;
		this.intensity = intensity;
		this.option = Optional.of(option);
		this.value = Optional.of(value);
	}

	public int getIntensity() {
		return intensity;
	}
	
	public ParticleEffect getEffect() throws InvalidEffectException {
		String[] particleArgs = particle.split(":");

		if(particleArgs.length != 2) {
			throw new InvalidEffectException(particleArgs + " is not a valid ParticleType");
		}
		
		Optional<ParticleType> optionalParticle = Sponge.getRegistry().getType(ParticleType.class, CatalogKey.of(particleArgs[0], particleArgs[1]));
		
		if(!optionalParticle.isPresent()) {
			throw new InvalidEffectException(particleArgs + " is not a valid ParticleType");
		}
		ParticleType particleType = optionalParticle.get();

		if(!option.isPresent() || !value.isPresent()) {
			return ParticleEffect.builder().type(particleType).build();
		}
		
		ParticleEffect.Builder builder = ParticleEffect.builder().type(particleType);
		
		String[] optionArgs = option.get().split(":");

		if(optionArgs.length != 2) {
			throw new InvalidEffectException(optionArgs + " is not a valid ParticleOption");
		}
		
		@SuppressWarnings("rawtypes")
		Optional<ParticleOption> optionalOption = Sponge.getRegistry().getType(ParticleOption.class, CatalogKey.of(optionArgs[0], optionArgs[1]));

		if(!optionalOption.isPresent()) {
			throw new InvalidEffectException(optionArgs + " is not a valid ParticleOption");
		}

		if(option.get().equalsIgnoreCase(ParticleOptions.BLOCK_STATE.getKey().toString())) {
    		String valueArgs[] = value.get().split(":");
    		
    		if(valueArgs.length < 2) {
    			throw new InvalidEffectException("Not a valid BlockType");
    		}

			Optional<BlockType> optionalBlockType = Sponge.getRegistry().getType(BlockType.class, CatalogKey.of(valueArgs[0], valueArgs[1]));
			
			if(!optionalBlockType.isPresent()) {
				throw new InvalidEffectException(valueArgs[0] + ":" + valueArgs[1] + " is not a valid BlockType");
			}
			
			BlockState state = optionalBlockType.get().getDefaultState();
			
    		if(valueArgs.length == 3) {
				try {
					Integer.parseInt(valueArgs[2]);
				} catch (Exception e) {
					throw new InvalidEffectException(valueArgs[2] + " is not a valid Data Value");
				}
				
				DataContainer container = state.toContainer();
				DataQuery query = DataQuery.of('/', "UnsafeDamage");
				
				container.set(query, Integer.parseInt(valueArgs[2]));
				
				state = Sponge.getDataManager().deserialize(BlockState.class, container).get();
    		}
    		
    		builder.option(ParticleOptions.BLOCK_STATE, state);
		} else if(option.get().equalsIgnoreCase(ParticleOptions.COLOR.getKey().toString())) {
    		Optional<Color> color = Colors.get(value.get());
    		
    		if(!color.isPresent()) {
    			throw new InvalidEffectException(value.get() + " is not a valid Color");
    		}
    		
    		builder.option(ParticleOptions.COLOR, color.get());
		} else if(option.get().equalsIgnoreCase(ParticleOptions.DIRECTION.getKey().toString())) {
			builder.option(ParticleOptions.DIRECTION, Direction.valueOf(value.get()));
		} else if(option.get().equalsIgnoreCase(ParticleOptions.FIREWORK_EFFECTS.getKey().toString())) {
			// ADD IMPLEMENTATION
    	} else if(option.get().equalsIgnoreCase(ParticleOptions.ITEM_STACK_SNAPSHOT.getKey().toString())) {
    		String valueArgs[] = value.get().split(":");
    		
    		if(valueArgs.length < 2) {
    			throw new InvalidEffectException(value.get() + " is not a valid ItemType");
    		}

			Optional<ItemType> optionalItemType = Sponge.getRegistry().getType(ItemType.class, CatalogKey.of(valueArgs[0], valueArgs[1]));
			
			if(!optionalItemType.isPresent()) {
				throw new InvalidEffectException(value.get() + " is not a valid ItemType");
			}
			
			ItemStack itemStack = ItemStack.of(optionalItemType.get(), 1);
			
    		if(valueArgs.length == 3) {
				try {
					Integer.parseInt(valueArgs[2]);
				} catch (Exception e) {
					throw new InvalidEffectException(valueArgs[2] + " is not a valid Data Value");
				}
				
				DataContainer container = itemStack.toContainer();
				DataQuery query = DataQuery.of('/', "UnsafeDamage");
				
				container.set(query, Integer.parseInt(valueArgs[2]));
				
				itemStack = Sponge.getDataManager().deserialize(ItemStack.class, container).get();
    		}
    		
    		builder.option(ParticleOptions.ITEM_STACK_SNAPSHOT, itemStack.createSnapshot());
    	} else if(option.get().equalsIgnoreCase(ParticleOptions.NOTE.getKey().toString())) {
    		String valueArgs[] = value.get().split(":");
    		
    		if(valueArgs.length != 2) {
    			throw new InvalidEffectException(value.get() + " is not a valid NotePitch");
    		}

    		Optional<NotePitch> notepitch = Sponge.getRegistry().getType(NotePitch.class, CatalogKey.of(valueArgs[0], valueArgs[1]));
    		
    		if(!notepitch.isPresent()) {
    			throw new InvalidEffectException(value.get() + " is not a valid NotePitch");
    		}
    		
    		builder.option(ParticleOptions.NOTE, notepitch.get());
    	} else if(option.get().equalsIgnoreCase(ParticleOptions.OFFSET.getKey().toString())) {
    		String valueArgs[] = value.get().split(",");
    		
    		if(valueArgs.length != 3) {
    			throw new InvalidEffectException(value.get() + " is not a valid Vector3d");
    		}
    		
    		for(String str : valueArgs) {
    			try {
    				Double.parseDouble(str);
    			} catch (Exception e) {
    				throw new InvalidEffectException(str + " is not a valid Double");
    			}
    		}
    		
    		builder.option(ParticleOptions.OFFSET, new Vector3d(Double.parseDouble(valueArgs[0]), Double.parseDouble(valueArgs[1]), Double.parseDouble(valueArgs[2])));
    	} else if(option.get().equalsIgnoreCase(ParticleOptions.POTION_EFFECT_TYPE.getKey().toString())) {
    		String valueArgs[] = value.get().split(":");
    		
    		if(valueArgs.length != 2) {
    			throw new InvalidEffectException(value.get() + " is not a valid PotionEffectType");
    		}

    		Optional<PotionEffectType> potionEffect = Sponge.getRegistry().getType(PotionEffectType.class, CatalogKey.of(valueArgs[0], valueArgs[1]));
    		
    		if(!potionEffect.isPresent()) {
    			throw new InvalidEffectException(value.get() + " is not a valid PotionEffectType");
    		}
    		
    		builder.option(ParticleOptions.POTION_EFFECT_TYPE, potionEffect.get());
    	} else if(option.get().equalsIgnoreCase(ParticleOptions.QUANTITY.getKey().toString())) {
    		int quantity;
    		
    		try {
    			quantity = Integer.parseInt(value.get());
    		} catch (Exception e) {
    			throw new InvalidEffectException(value.get() + " is not a valid Integer");
    		}
    		
    		builder.option(ParticleOptions.QUANTITY, quantity);
    	} else if(option.get().equalsIgnoreCase(ParticleOptions.SCALE.getKey().toString())) {
    		double scale;
    		
    		try {
    			scale = Double.parseDouble(value.get());
    		} catch (Exception e) {
    			throw new InvalidEffectException(value.get() + " is not a valid Double");
    		}
    		
    		builder.option(ParticleOptions.SCALE, scale);
    	} else if(option.get().equalsIgnoreCase(ParticleOptions.SLOW_HORIZONTAL_VELOCITY.getKey().toString())) {

    		if(!value.get().equalsIgnoreCase("true") && !value.get().equalsIgnoreCase("false")) {
    			throw new InvalidEffectException(value.get() + " is not a valid Boolean");
    		}

    		builder.option(ParticleOptions.SLOW_HORIZONTAL_VELOCITY, Boolean.parseBoolean(value.get()));
    	} else if(option.get().equalsIgnoreCase(ParticleOptions.VELOCITY.getKey().toString())) {
    		String valueArgs[] = value.get().split(",");
    		
    		if(valueArgs.length != 3) {
    			throw new InvalidEffectException(value.get() + " is not a valid Vector3d");
    		}
    		
    		for(String str : valueArgs) {
    			try {
    				Double.parseDouble(str);
    			} catch (Exception e) {
    				throw new InvalidEffectException(str + " is not a valid Double");
    			}
    		}
    		
    		builder.option(ParticleOptions.VELOCITY, new Vector3d(Double.parseDouble(valueArgs[0]), Double.parseDouble(valueArgs[1]), Double.parseDouble(valueArgs[2])));
    	}
		
		return builder.build();
	}
	
	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	public DataContainer toContainer() {
		DataContainer container = DataContainer.createNew().set(PARTICLE, particle).set(INTENSITY, intensity);

		if (option.isPresent()) {
			container.set(OPTION, option.get());
		}

		if(value.isPresent()) {
			container.set(VALUE, value.get());
		}
		
		return container;
	}

	public static class Builder extends AbstractDataBuilder<Effect> {

		public Builder() {
			super(Effect.class, 0);
		}

		@Override
		protected Optional<Effect> buildContent(DataView container) throws InvalidDataException {
			if (container.contains(PARTICLE)) {
				String particle = container.getString(PARTICLE).get();
				int intensity = container.getInt(INTENSITY).get();

				if (container.contains(OPTION)) {			
					String option = container.getString(OPTION).get();
					String value = container.getString(VALUE).get();
					
					return Optional.of(new Effect(particle, intensity, option, value));
				}
				
				return Optional.of(new Effect(particle, intensity));
			}

			return Optional.empty();
		}
	}
}
