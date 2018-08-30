package com.gmail.trentech.pjp.commands.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.effects.Colors;
import com.gmail.trentech.pjp.effects.Effect;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.features.Properties;
import com.gmail.trentech.pjp.utils.InvalidEffectException;

public class CommandParticle implements CommandCallable {
	
	private final Help help = Help.get("portal particle").get();

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

		Properties properties = portal.getProperties().get();

		if(args.length < 3 || args.length > 5) {
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
		
		Effect effect;
		if(args.length == 3) {
			effect = new Effect(args[1], intensity);
		} else {
			effect = new Effect(args[1], intensity, args[3], args[4]);
		}
		
		try {
			effect.getEffect();
		} catch(InvalidEffectException e) {
			throw new CommandException(Text.of(TextColors.RED, e.getMessage()), true);
		}
		
		properties.setEffect(Optional.of(effect));
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
					for(ParticleOption particleOption : Sponge.getRegistry().getAllOf(ParticleOption.class)) {
						list.add(particleOption.getId());
					}
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
			    	if(args[3].equalsIgnoreCase(ParticleOptions.BLOCK_STATE.getId())) {
			    		for(BlockType blockType : Sponge.getRegistry().getAllOf(BlockType.class)) {
							list.add(blockType.getId());
			    		}
			    	} else if(args[3].equalsIgnoreCase(ParticleOptions.COLOR.getId())) {
			    		for(Colors color : Colors.values()) {
							list.add(color.getName());
			    		}
			    	} else if(args[3].equalsIgnoreCase(ParticleOptions.DIRECTION.getId())) {
			    		for(Direction direction : Direction.values()) {
							list.add(direction.name());
			    		}
			    	} else if(args[3].equalsIgnoreCase(ParticleOptions.FIREWORK_EFFECTS.getId())) {
			    		// IMPLEMENT
			    	} else if(args[3].equalsIgnoreCase(ParticleOptions.ITEM_STACK_SNAPSHOT.getId())) {
			    		for(ItemType itemType : Sponge.getRegistry().getAllOf(ItemType.class)) {
							list.add(itemType.getId());
			    		}
			    	} else if(args[3].equalsIgnoreCase(ParticleOptions.NOTE.getId())) {
			    		for(NotePitch notePitch : Sponge.getRegistry().getAllOf(NotePitch.class)) {
							list.add(notePitch.getId());
			    		}
			    	} else if(args[3].equalsIgnoreCase(ParticleOptions.POTION_EFFECT_TYPE.getId())) {
			    		for(PotionEffectType potionEffect : Sponge.getRegistry().getAllOf(PotionEffectType.class)) {
							list.add(potionEffect.getId());
			    		}
			    	} else if(args[3].equalsIgnoreCase(ParticleOptions.SLOW_HORIZONTAL_VELOCITY.getId())) {
			    		list.add("true");
			    		list.add("false");
			    	}
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
	    		for(Direction direction : Direction.values()) {
					if(direction.name().toLowerCase().equalsIgnoreCase(args[4].toLowerCase())) {
						return list;
					}
					
					if(direction.name().toLowerCase().startsWith(args[4].toLowerCase())) {
						list.add(direction.name());
					}
	    		}
	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.FIREWORK_EFFECTS.getId())) {
	    		// IMPLEMENT
	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.ITEM_STACK_SNAPSHOT.getId())) {
	    		for(ItemType itemType : Sponge.getRegistry().getAllOf(ItemType.class)) {
					if(itemType.getId().toLowerCase().equalsIgnoreCase(args[4].toLowerCase())) {
						return list;
					}
					
					if(itemType.getId().toLowerCase().startsWith(args[4].toLowerCase())) {
						list.add(itemType.getId());
					}
	    		}
	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.NOTE.getId())) {
	    		for(NotePitch notePitch : Sponge.getRegistry().getAllOf(NotePitch.class)) {
					if(notePitch.getId().toLowerCase().equalsIgnoreCase(args[4].toLowerCase())) {
						return list;
					}
					
					if(notePitch.getId().toLowerCase().startsWith(args[4].toLowerCase())) {
						list.add(notePitch.getId());
					}
	    		}
	    	} else if(args[3].equalsIgnoreCase(ParticleOptions.POTION_EFFECT_TYPE.getId())) {
	    		for(PotionEffectType potionEffect : Sponge.getRegistry().getAllOf(PotionEffectType.class)) {
					if(potionEffect.getId().toLowerCase().equalsIgnoreCase(args[4].toLowerCase())) {
						return list;
					}
					
					if(potionEffect.getId().toLowerCase().startsWith(args[4].toLowerCase())) {
						list.add(potionEffect.getId());
					}
	    		}
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
