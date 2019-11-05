package com.gmail.trentech.pjp.commands;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.events.TeleportEvent;
import com.gmail.trentech.pjp.events.TeleportEvent.Local;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalService;

public class CMDBack implements CommandExecutor {

	public CommandSpec cmdBack = CommandSpec.builder().description(Text.of("Send player to last place they were")).permission("pjp.cmd.back").executor(this).build();

	public static ConcurrentHashMap<Player, Location<World>> players = new ConcurrentHashMap<>();

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			throw new CommandException(Text.of(TextColors.RED, "Must be a player"));
		}
		Player player = (Player) src;

		if (players.get(player) == null) {
			throw new CommandException(Text.of(TextColors.RED, "No position to teleport to"));
		}
		Location<World> spawnLocation = players.get(player);

		PortalService portalService = Sponge.getServiceManager().provide(PortalService.class).get();
		
		while (portalService.get(spawnLocation, PortalType.PORTAL).isPresent() || portalService.get(spawnLocation, PortalType.DOOR).isPresent()) {
			ThreadLocalRandom random = ThreadLocalRandom.current();

			int x = (random.nextInt(5 * 2) - 5) + spawnLocation.getBlockX();
			int z = (random.nextInt(5 * 2) - 5) + spawnLocation.getBlockZ();

			Optional<Location<World>> optionalLocation = Sponge.getGame().getTeleportHelper().getSafeLocation(spawnLocation.getExtent().getLocation(x, spawnLocation.getBlockY(), z));

			if (optionalLocation.isPresent()) {
				spawnLocation = optionalLocation.get();
			}
		}

		Local teleportEvent = new TeleportEvent.Local(player, player.getLocation(), spawnLocation, 0, true, Optional.empty(), Optional.empty(), Cause.of(EventContext.builder().add(EventContextKeys.PLAYER, player).build(), player));

		if (!Sponge.getEventManager().post(teleportEvent)) {
			spawnLocation = teleportEvent.getDestination();
			player.setLocation(spawnLocation);
		}

		return CommandResult.success();
	}
}
