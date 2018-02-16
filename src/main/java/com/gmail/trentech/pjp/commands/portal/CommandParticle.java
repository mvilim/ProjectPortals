package com.gmail.trentech.pjp.commands.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleEffect.Builder;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.effects.Colors;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.features.Properties;

public class CommandParticle implements CommandCallable {
	
	private final Help help = Help.get("portal particle").get();

	@SuppressWarnings("rawtypes")
	@Override
	public CommandResult process(CommandSource source, String arguments) throws CommandException {
		if(arguments.equalsIgnoreCase("particle")) {
			throw new CommandException(getHelp().getUsageText());
		}

		String[] args = arguments.split(" ");
		
		if(args[args.length - 1].equalsIgnoreCase("--help")) {
			getHelp().execute(source);
			return CommandResult.success();
		}
		
		Portal portal;

		try {
			String portalName = args[0];
			
			Optional<Portal> optionalPortal = Sponge.getServiceManager().provideUnchecked(PortalService.class).get(portalName, PortalType.PORTAL);
			
			if(!optionalPortal.isPresent()) {
				throw new CommandException(Text.of(TextColors.RED, portalName, " does not exist"), false);
			}
			portal = optionalPortal.get();
		} catch(Exception e) {
			throw new CommandException(getHelp().getUsageText());
		}

		ParticleType particle;
		
		try {
			String particleId = args[1];
			
			Optional<ParticleType> optionalParticle = Sponge.getRegistry().getType(ParticleType.class, particleId);
			
			if(!optionalParticle.isPresent()) {
				throw new CommandException(Text.of(TextColors.RED, "Not a valid ParticleType"), true);
			}
			particle = optionalParticle.get();
		} catch(Exception e) {
			throw new CommandException(getHelp().getUsageText());
		}
		
		int intensity;
		
		try {
			String intensityString = args[2];
			
			try {
				intensity = Integer.parseInt(intensityString);
			} catch(Exception e) {
				throw new CommandException(Text.of(TextColors.RED, "Not a valid Integer"), true);
			}			
		} catch(Exception e) {
			throw new CommandException(getHelp().getUsageText());
		}
		
		Builder builder = ParticleEffect.builder().type(particle);
		if(args.length > 3) {
			try {
				String particleOption = args[3];
				
				Optional<ParticleOption> optionalOption = Sponge.getRegistry().getType(ParticleOption.class, particleOption);

				if(!optionalOption.isPresent()) {
					throw new CommandException(Text.of(TextColors.RED, "Not a valid ParticleOption"), false);
				}
			} catch(Exception e) {
				throw new CommandException(getHelp().getUsageText());
			}

			String value;
			String previous;
			try {
				value = args[4];
				previous = args[3];
			} catch(Exception e) {
				throw new CommandException(getHelp().getUsageText());
			}

	    	if(previous.equalsIgnoreCase(ParticleOptions.BLOCK_STATE.getId())) {
	    		String id[] = value.split(":");
	    		
	    		if(id.length < 2) {
	    			throw new CommandException(Text.of(TextColors.RED, "Not a valid BlockType"), false);
	    		}
				Optional<BlockType> optionalBlockType = Sponge.getRegistry().getType(BlockType.class, id[0] + ":" + id[1]);
				
				if(!optionalBlockType.isPresent()) {
					throw new CommandException(Text.of(TextColors.RED, "Not a valid BlockType"), false);
				}
				
				BlockState state = optionalBlockType.get().getDefaultState();
				
	    		if(id.length > 2) {
					try {
						Integer.parseInt(id[2]);
					} catch (Exception e) {
						throw new CommandException(Text.of(TextColors.RED, "Not a valid Data Value"), false);
					}
					
					DataContainer container = state.toContainer();
					DataQuery query = DataQuery.of('/', "UnsafeDamage");
					
					container.set(query, Integer.parseInt(id[2]));
					
					state = Sponge.getDataManager().deserialize(BlockState.class, container).get();
	    		}
	    		
	    		builder.option(ParticleOptions.BLOCK_STATE, state);
	    	} else if(previous.equalsIgnoreCase(ParticleOptions.COLOR.getId())) {
	    		Optional<Color> color = Colors.get(value);
	    		
	    		if(!color.isPresent()) {
	    			throw new CommandException(Text.of(TextColors.RED, "Not a valid Color"), false);
	    		}
	    		builder.option(ParticleOptions.COLOR, color.get());
	    	} else if(previous.equalsIgnoreCase(ParticleOptions.DIRECTION.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.FIREWORK_EFFECTS.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.ITEM_STACK_SNAPSHOT.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.NOTE.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.OFFSET.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.POTION_EFFECT_TYPE.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.QUANTITY.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.SCALE.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.SLOW_HORIZONTAL_VELOCITY.getId())) {

	    	} else if(previous.equalsIgnoreCase(ParticleOptions.VELOCITY.getId())) {

	    	}
		}

		Properties properties = portal.getProperties().get();
		
		properties.setParticle(Optional.of(builder.build()));
		properties.setIntensity(intensity);
		
		portal.setProperties(properties);
		
		Sponge.getServiceManager().provide(PortalService.class).get().update(portal);
		
		return CommandResult.success();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition) throws CommandException {
		List<String> list = new ArrayList<>();
		
		if(arguments.equalsIgnoreCase("particle")) {
			return list;
		}

		String[] args = arguments.split(" ");
		
		if(args.length == 1) {
			for(Portal portal : Sponge.getServiceManager().provideUnchecked(PortalService.class).all(PortalType.PORTAL)) {
				if(portal.getName().equalsIgnoreCase(args[0])) {
					for(ParticleType particleType : Sponge.getRegistry().getAllOf(ParticleType.class)) {
						list.add(particleType.getId());
					}
					return list;
				}
				
				if(portal.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					list.add(portal.getName());
				}
			}
		}
		
		if(args.length == 2) {
			for(ParticleType particleType : Sponge.getRegistry().getAllOf(ParticleType.class)) {
				if(particleType.getId().toLowerCase().equalsIgnoreCase(args[1].toLowerCase())) {
					return list;
				}
				
				if(particleType.getId().toLowerCase().startsWith(args[1].toLowerCase())) {
					list.add(particleType.getId());
				}
			}
		}
		
		if(args.length == 4) {
			for(ParticleOption particleOption : Sponge.getRegistry().getAllOf(ParticleOption.class)) {
				if(particleOption.getId().toLowerCase().equalsIgnoreCase(args[3].toLowerCase())) {
					return list;
				}
				
				if(particleOption.getId().toLowerCase().startsWith(args[3].toLowerCase())) {
					list.add(particleOption.getId());
				}
			}
		}
		
		if(args.length == 5) {
	    	if(args[3].equalsIgnoreCase(ParticleOptions.BLOCK_STATE.getId())) {
	    		for(BlockType blockType : Sponge.getRegistry().getAllOf(BlockType.class)) {
					if(blockType.getId().toLowerCase().equalsIgnoreCase(args[4].toLowerCase())) {
						return list;
					}
					
					if(blockType.getId().toLowerCase().startsWith(args[4].toLowerCase())) {
						list.add(blockType.getId());
					}
	    		}
	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.COLOR.getId())) {
	    		for(Colors color : Colors.values()) {
					if(color.getName().toLowerCase().equalsIgnoreCase(args[4].toLowerCase())) {
						return list;
					}
					
					if(color.getName().toLowerCase().startsWith(args[4].toLowerCase())) {
						list.add(color.getName());
					}
	    		}
	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.DIRECTION.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.FIREWORK_EFFECTS.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.ITEM_STACK_SNAPSHOT.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.NOTE.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.OFFSET.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.POTION_EFFECT_TYPE.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.QUANTITY.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.SCALE.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.SLOW_HORIZONTAL_VELOCITY.getId())) {

	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.VELOCITY.getId())) {

	    	}
		}
		return list;


	}

	@Override
	public boolean testPermission(CommandSource source) {
		Optional<String> permission = getHelp().getPermission();
		
		if(permission.isPresent()) {
			return source.hasPermission(permission.get());
		}
		return true;
	}

	@Override
	public Optional<Text> getShortDescription(CommandSource source) {
		return Optional.of(Text.of(getHelp().getDescription()));
	}

	@Override
	public Optional<Text> getHelp(CommandSource source) {
		return Optional.of(Text.of(getHelp().getDescription()));
	}

	@Override
	public Text getUsage(CommandSource source) {
		return getHelp().getUsageText();
	}
	
	public Help getHelp() {
		return help;
	}
}
