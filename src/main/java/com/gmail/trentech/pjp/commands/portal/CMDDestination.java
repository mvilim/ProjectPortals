package com.gmail.trentech.pjp.commands.portal;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjc.core.BungeeManager;
import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.features.Coordinate;
import com.gmail.trentech.pjp.portal.features.Coordinate.Preset;
import com.gmail.trentech.pjp.rotation.Rotation;

public class CMDDestination implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Help help = Help.get("portal destination").get();
		
		if (args.hasAny("help")) {		
			help.execute(src);
			return CommandResult.empty();
		}
		
		if (!(src instanceof Player)) {
			throw new CommandException(Text.of(TextColors.RED, "Must be a player"), false);
		}
		Player player = (Player) src;

		if (!args.hasAny("name")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		Portal portal = args.<Portal>getOne("name").get();

		if (!args.hasAny("destination")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		String destination = args.<String>getOne("destination").get();

		if(portal.getServer().isPresent()) {
			if (!args.hasAny("b")) {
				throw new CommandException(Text.of(TextColors.RED, "Bungee portals cannot be changed to local server portals at this time."), false);
			}
			
			Consumer<List<String>> consumer1 = (list) -> {
				if (!list.contains(destination)) {
					try {
						throw new CommandException(Text.of(TextColors.RED, destination, " does not exist"), false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Consumer<String> consumer2 = (s) -> {
					if (destination.equalsIgnoreCase(s)) {
						try {
							throw new CommandException(Text.of(TextColors.RED, "Destination cannot be the server you are currently on"), false);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					portal.setServer(destination);
					
					Sponge.getServiceManager().provideUnchecked(PortalService.class).update(portal);
				};
				BungeeManager.getServer(consumer2, player);
			};			
			BungeeManager.getServers(consumer1, player);
		} else {
			if (args.hasAny("b")) {
				throw new CommandException(Text.of(TextColors.RED, "Local server portals cannot be changed to bungee portals at this time."), false);
			}
			
			Optional<World> world = Sponge.getServer().getWorld(destination);

			if (!world.isPresent()) {
				throw new CommandException(Text.of(TextColors.RED, destination, " is not loaded or does not exist"), false);
			}

			if (args.hasAny("x,y,z")) {
				String[] coords = args.<String>getOne("x,y,z").get().split(",");

				if (coords[0].equalsIgnoreCase("random")) {
					portal.setCoordinate(new Coordinate(world.get(), Preset.RANDOM));
				} else if(coords[0].equalsIgnoreCase("bed")) {
					portal.setCoordinate(new Coordinate(world.get(), Preset.BED));
				} else if(coords[0].equalsIgnoreCase("last")) {
					portal.setCoordinate(new Coordinate(world.get(), Preset.LAST_LOCATION));
				} else {
					try {
						portal.setCoordinate(new Coordinate(world.get(), new Vector3d(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]))));
					} catch (Exception e) {
						throw new CommandException(Text.of(TextColors.RED, coords.toString(), " is not valid"), true);
					}
				}
			} else {
				portal.setCoordinate(new Coordinate(world.get(), Preset.NONE));
			}

			if (args.hasAny("direction")) {
				portal.setRotation(args.<Rotation>getOne("direction").get());
			}

			if (args.hasAny("f")) {
				portal.setForce(true);
			}

			Sponge.getServiceManager().provideUnchecked(PortalService.class).update(portal);
		}
		
		player.sendMessage(Text.of(TextColors.DARK_GREEN, "changed portal destination"));

		return CommandResult.success();
	}
}
