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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.features.Properties;

public class CommandBlock implements CommandCallable {
	
	private final Help help = Help.get("portal block").get();

	@Override
	public CommandResult process(CommandSource source, String arguments) throws CommandException {
		if(arguments.equalsIgnoreCase("block")) {
			throw new CommandException(getHelp().getUsageText());
		}

		String[] args = arguments.split(" ");
		
		if(args[args.length - 1].equalsIgnoreCase("--help")) {
			getHelp().execute(source);
			return CommandResult.success();
		}
		
		Portal portal;
		Properties properties;
		
		try {
			String portalName = args[0];
			
			Optional<Portal> optionalPortal = Sponge.getServiceManager().provideUnchecked(PortalService.class).get(portalName, PortalType.PORTAL);
			
			if(!optionalPortal.isPresent()) {
				throw new CommandException(Text.of(TextColors.RED, portalName, " does not exist"), false);
			}
			portal = optionalPortal.get();
			properties = portal.getProperties().get();
		} catch(Exception e) {
			throw new CommandException(getHelp().getUsageText());
		}

		BlockState blockState;
		
		try {
			String[] blockId = args[1].split(":");

			Optional<BlockType> optionalBlockType = Sponge.getRegistry().getType(BlockType.class, blockId[0] + ":" + blockId[1]);
			
			if(!optionalBlockType.isPresent()) {
				throw new CommandException(Text.of(TextColors.RED, "Not a valid BlockType"), true);
			}
			blockState = optionalBlockType.get().getDefaultState();

			if(blockId.length == 3) {
				try {
					Integer.parseInt(blockId[2]);
				} catch (Exception e) {
					throw new CommandException(Text.of(TextColors.RED, blockId[2] + " is not a valid Data Value"));
				}
				
				DataContainer container = blockState.toContainer();
				DataQuery query = DataQuery.of('/', "UnsafeDamage");
				
				container.set(query, Integer.parseInt(blockId[2]));
				
				blockState = Sponge.getDataManager().deserialize(BlockState.class, container).get();
			}
		} catch(Exception e) {
			throw new CommandException(getHelp().getUsageText());
		}
		
		properties.setBlockState(Optional.of(blockState));
		portal.setProperties(properties);
		
		Sponge.getServiceManager().provide(PortalService.class).get().update(portal);
		
		return CommandResult.success();
	}

	@Override
	public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition) throws CommandException {
		List<String> list = new ArrayList<>();
		
		if(arguments.equalsIgnoreCase("block")) {
			return list;
		}

		String[] args = arguments.split(" ");
		
		if(args.length == 1) {
			for(Portal portal : Sponge.getServiceManager().provideUnchecked(PortalService.class).all(PortalType.PORTAL)) {
				if(portal.getName().equalsIgnoreCase(args[0])) {
					for(BlockType blockType : Sponge.getRegistry().getAllOf(BlockType.class)) {
						list.add(blockType.getId());
					}
					return list;
				}
				
				if(portal.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					list.add(portal.getName());
				}
			}
		}
		
		if(args.length == 2) {
			for(BlockType blockType : Sponge.getRegistry().getAllOf(BlockType.class)) {
				if(blockType.getId().toLowerCase().equalsIgnoreCase(args[1].toLowerCase())) {
					return list;
				}
				
				if(blockType.getId().toLowerCase().startsWith(args[1].toLowerCase())) {
					list.add(blockType.getId());
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
