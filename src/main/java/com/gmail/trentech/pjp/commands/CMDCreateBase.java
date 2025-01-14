package com.gmail.trentech.pjp.commands;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.features.Command;
import com.gmail.trentech.pjp.portal.features.Command.SourceType;
import com.gmail.trentech.pjp.portal.features.Coordinate;
import com.gmail.trentech.pjp.portal.features.Coordinate.Preset;
import com.gmail.trentech.pjp.rotation.Rotation;

public interface CMDCreateBase extends CommandExecutor {

	Help getHelp();

	PortalType getType();

	void complete(Player player, Portal portal, String name);

	default Optional<CommandResult> checkHelp(CommandSource src, CommandContext args)
	{
		if (args.hasAny("help")) {
			getHelp().execute(src);
			return Optional.of(CommandResult.empty());
		}
		return Optional.empty();
	}

	default Optional<Portal> getPortal(String name)
	{
		return Sponge.getServiceManager().provide(PortalService.class).get().get(name, getType());
	}

	default void checkNameExistence(String name) throws CommandException
	{
		if (getPortal(name).isPresent()) {
			throw new CommandException(Text.of(TextColors.RED, name, " already exists"), false);
		}
	}

	default String getName(CommandContext args) throws CommandException
	{
		if (!args.hasAny("name")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(getHelp().execute())).append(getHelp().getUsageText()).build(), false);
		}
		String name = args.<String>getOne("name").get().toLowerCase();

		checkNameExistence(name);

		return name;
	}

	default double getPrice(CommandContext args)
	{
		return args.<Double>getOne("price").orElse(0.0);
	}

	default Player getPlayer(CommandSource src) throws CommandException
	{
		if (!(src instanceof Player)) {
			throw new CommandException(Text.of(TextColors.RED, "Must be a player"), false);
		}
		return (Player) src;
	}

	default Optional<String> getDestinationArg(CommandContext args) throws CommandException
	{
		return args.<String>getOne("destination");
	}

	default String getRequiredDestination(CommandContext args) throws CommandException
	{
		return getDestinationArg(args).orElseThrow(() -> new CommandException(Text.builder().onClick(TextActions.executeCallback(getHelp().execute())).append(getHelp().getUsageText()).build(), false));
	}

	default String getDestination(CommandContext args) throws CommandException
	{
		String destination = getRequiredDestination(args);
		if (!getServerArg(args).isPresent())
		{
			Optional<World> world = Sponge.getServer().getWorld(destination);
			if (!world.isPresent())
			{
				throw new CommandException(Text.of(TextColors.RED, destination, " is not loaded or does not exist"), false);
			}
		}
		return destination;
	}

	default Optional<String> getServerArg(CommandContext args)
	{
		return args.<String>getOne("server");
	}

	default Optional<String> getServer(CommandSource src, CommandContext args) throws CommandException
	{
		Optional<String> optServer = getServerArg(args);
		if (optServer.isPresent())
		{
			String server = optServer.get();
			Player player = getPlayer(src);
			// these are used not for their atomicity, but for their mutability inside a lambda
			AtomicBoolean destinationExists = new AtomicBoolean(true);
			AtomicBoolean destinationCurrent = new AtomicBoolean(false);

			Consumer<List<String>> consumer1 = (list) -> {
				destinationExists.set(list.contains(server));

				Consumer<String> consumer2 = (s) -> {
					destinationCurrent.set(server.equalsIgnoreCase(s));
				};
				BungeeManager.getServer(consumer2, player);
			};
			BungeeManager.getServers(consumer1, player);

			if (!destinationExists.get())
			{
				throw new CommandException(Text.of(TextColors.RED, server, " does not exist"), false);
			}

			if (destinationCurrent.get())
			{
				throw new CommandException(Text.of(TextColors.RED, "Destination cannot be the server you are currently on"), false);
			}
		};

		return optServer;
	}

	default Optional<String> getPermission(CommandContext args)
	{
		return args.<String>getOne("permission");
	}

	default Rotation getDirection(CommandContext args)
	{
		if (args.hasAny("direction")) {
			return args.<Rotation>getOne("direction").get();
		}
		return Rotation.EAST;
	}

	default Optional<Command> getCommand(CommandContext args) throws CommandException
	{
		if (args.hasAny("command")) {
			String rawCommand = args.<String>getOne("command").get();
			String source = rawCommand.substring(0, 2);

			if(rawCommand.length() < 2) {
				throw new CommandException(Text.of(TextColors.RED, "Did not specify command source. P: for player or C: for console. Example \"P:say hello world\""), false);
			}

			if(source.equalsIgnoreCase("P:")) {
				return Optional.of(new Command(SourceType.PLAYER, rawCommand.substring(2)));
			} else if(source.equalsIgnoreCase("C:")) {
				return Optional.of(new Command(SourceType.CONSOLE, rawCommand.substring(2)));
			} else {
				throw new CommandException(Text.of(TextColors.RED, "Did not specify command source. P: for player or C: for console. Example \"P:say hello world\""), false);
			}
		}
		return Optional.empty();
	}

	default boolean getForce(CommandContext args)
	{
		return args.hasAny("f");
	}

	default Coordinate getCoordinate(CommandContext args) throws CommandException
	{
		String world = getDestination(args);
		if (args.hasAny("x,y,z")) {
			String[] coords = args.<String>getOne("x,y,z").get().split(",");

			if (coords[0].equalsIgnoreCase("random")) {
				return new Coordinate(world, Preset.RANDOM);
			} else if(coords[0].equalsIgnoreCase("bed")) {
				return new Coordinate(world, Preset.BED);
			} else if(coords[0].equalsIgnoreCase("last")) {
				return new Coordinate(world, Preset.LAST_LOCATION);
			} else {
				try {
					return new Coordinate(world, new Vector3d(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2])));
				} catch (Exception e) {
					throw new CommandException(Text.of(TextColors.RED, coords.toString(), " is not valid"), true);
				}
			}
		} else {
			return new Coordinate(world, Preset.NONE);
		}
	}

	@Override
	default CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String name = getName(args);
		checkHelp(src, args);

		Player player = getPlayer(src);

		PortalType type = getType();
		Portal portal = new Portal(name, type);
		if (type.equals(PortalType.WARP) && !getDestinationArg(args).isPresent())
		{
			portal.setCoordinate(new Coordinate(player.getLocation()));
			portal.setRotation(Rotation.getClosest(player.getRotation().getFloorY()));
			portal.setForce(getForce(args));
		}
		else {
			portal.setCoordinate(getCoordinate(args));
			portal.setRotation(getDirection(args));
			portal.setForce(getForce(args));
		}

		portal.setPrice(getPrice(args));
		getServer(src, args).ifPresent(portal::setServer);
		getPermission(args).ifPresent(portal::setPermission);
		getCommand(args).ifPresent(portal::setCommand);

		complete(player, portal, name);

		return CommandResult.success();
	}
}
