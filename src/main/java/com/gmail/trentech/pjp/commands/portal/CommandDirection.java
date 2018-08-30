package com.gmail.trentech.pjp.commands.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.rotation.Rotation;

public class CommandDirection implements CommandCallable {
	
	private final Help help = Help.get("portal direction").get();

	@Override
	public CommandResult process(CommandSource source, String arguments) throws CommandException {
		if(arguments.equalsIgnoreCase("direction")) {
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

		if(portal instanceof Portal.Server) {
			throw new CommandException(Text.of(TextColors.RED, "Direction property has no effect on Bungee Portals"), false);
		}
		Portal.Local local = (Portal.Local) portal;
		
		Rotation rotation;
		
		try {
			String direction = args[1];
			
			Optional<Rotation> optionalRotation = Rotation.get(direction);

			if(!optionalRotation.isPresent()) {
				throw new CommandException(Text.of(TextColors.RED, "Not a valid Direction"), true);
			}
			rotation = optionalRotation.get();
		} catch(Exception e) {
			throw new CommandException(getHelp().getUsageText());
		}
		
		local.setRotation(rotation);

		Sponge.getServiceManager().provide(PortalService.class).get().update(portal);
		
		source.sendMessage(Text.of(TextColors.DARK_GREEN, "changed portal direction to", rotation.name()));
		
		return CommandResult.success();
	}

	@Override
	public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition) throws CommandException {
		List<String> list = new ArrayList<>();
		
		if(arguments.equalsIgnoreCase("direction")) {
			return list;
		}

		String[] args = arguments.split(" ");
		
		if(args.length == 1) {
			for(Portal portal : Sponge.getServiceManager().provideUnchecked(PortalService.class).all(PortalType.PORTAL)) {
				if(portal.getName().equalsIgnoreCase(args[0])) {
					for(Rotation rotation : Rotation.values()) {
						list.add(rotation.getName());
					}
					return list;
				}
				
				if(portal.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					list.add(portal.getName());
				}
			}
		}
		
		if(args.length == 2) {
			for(Rotation rotation : Rotation.values()) {
				if(rotation.getName().toLowerCase().equalsIgnoreCase(args[1].toLowerCase())) {
					return list;
				}
				
				if(rotation.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
					list.add(rotation.getName());
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
