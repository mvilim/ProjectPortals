package com.gmail.trentech.pjp.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.effects.PortalEffect;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.utils.Timings;

public class DoorListener {

	public static ConcurrentHashMap<UUID, Portal> builders = new ConcurrentHashMap<>();

	private Timings timings;

	public DoorListener(Timings timings) {
		this.timings = timings;
	}

	@Listener
	public void onChangeBlockEventBreak(ChangeBlockEvent.Break event, @Root Player player) {
		timings.onChangeBlockEventBreak().startTiming();

		try {
			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				Location<World> location = transaction.getFinal().getLocation().get();

				PortalService portalService = Sponge.getServiceManager().provide(PortalService.class).get();
				
				Optional<Portal> optionalPortal = portalService.get(location, PortalType.DOOR);

				if (!optionalPortal.isPresent()) {
					continue;
				}
				Portal portal = optionalPortal.get();

				if (!player.hasPermission("pjp.door.break")) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to break door portals"));
					event.setCancelled(true);
				} else {
					portalService.remove(portal);
					player.sendMessage(Text.of(TextColors.DARK_GREEN, "Broke door portal"));
				}
			}
		} finally {
			timings.onChangeBlockEventBreak().stopTiming();
		}
	}

	@Listener(order = Order.POST)
	public void onChangeBlockEventPlace(ChangeBlockEvent.Place event, @Root Player player) {
		timings.onChangeBlockEventPlace().startTiming();

		try {
			if (!builders.containsKey(player.getUniqueId())) {
				return;
			}

			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				Location<World> location = transaction.getFinal().getLocation().get();

				Optional<Boolean> optionalOpen = location.get(Keys.OPEN);
				
				if(!optionalOpen.isPresent()) {
					continue;
				}				
				
				if (!player.hasPermission("pjp.door.place")) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to place door portals"));
					builders.remove(player.getUniqueId());
					event.setCancelled(true);
					return;
				}

				Portal portal = builders.get(player.getUniqueId());
				
				Sponge.getServiceManager().provide(PortalService.class).get().create(portal, location);
				PortalEffect.create(location);
				PortalEffect.create(location.getRelative(Direction.UP));
				
				player.sendMessage(Text.of(TextColors.DARK_GREEN, "New door portal created"));

				builders.remove(player.getUniqueId());
				break;
			}
		} finally {
			timings.onChangeBlockEventPlace().stopTiming();
		}
	}

	private static List<UUID> cache = new ArrayList<>();

	@Listener
	public void onMoveEntityEventPlayer(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
		timings.onMoveEntityEvent().startTimingIfSync();

		try {
			Location<World> location = player.getLocation();

			Optional<Boolean> optionalOpen = location.get(Keys.OPEN);
			
			if(!optionalOpen.isPresent()) {
				return;
			}
			
			if(!optionalOpen.get()) {
				return;
			}
			
			PortalService portalService = Sponge.getServiceManager().provide(PortalService.class).get();
			
			Optional<Portal> optionalPortal = portalService.get(location, PortalType.DOOR);

			if (!optionalPortal.isPresent()) {
				return;
			}
			Portal portal = optionalPortal.get();

			UUID uuid = player.getUniqueId();

			if (cache.contains(uuid)) {
				return;
			}

			cache.add(uuid);

			portalService.execute(player, portal);

			Sponge.getScheduler().createTaskBuilder().delayTicks(20).execute(c -> {
				cache.remove(uuid);
			}).submit(Main.getPlugin());
		} finally {
			timings.onMoveEntityEvent().stopTimingIfSync();
		}
	}
}
