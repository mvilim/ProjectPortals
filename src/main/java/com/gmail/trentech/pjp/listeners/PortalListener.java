package com.gmail.trentech.pjp.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.data.mutable.LastLocationData;
import com.gmail.trentech.pjp.events.ConstructPortalEvent;
import com.gmail.trentech.pjp.portal.PortalBuilder;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.features.Coordinate;
import com.gmail.trentech.pjp.utils.Timings;

import ninja.leaping.configurate.ConfigurationNode;

public class PortalListener {

	public static ConcurrentHashMap<UUID, PortalBuilder> builders = new ConcurrentHashMap<>();

	private Timings timings;

	public PortalListener(Timings timings) {
		this.timings = timings;
	}

	@Listener
	public void onChangeBlockEventPlaceCreate(ChangeBlockEvent.Place event, @Root Entity entity) {
		timings.onChangeBlockEventPlace().startTiming();

		try {
			if(entity instanceof Player) {
				Player player = (Player) entity;
				
				if (builders.containsKey(player.getUniqueId())) {
					PortalBuilder builder = builders.get(player.getUniqueId());

					for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
						if (transaction.getFinal().getState().getType().equals(BlockTypes.FIRE)) {
							event.setCancelled(true);
							break;
						}

						Location<World> location = transaction.getFinal().getLocation().get();

						if (builder.isFill()) {
							builder.addFill(location);
						} else {
							builder.addFrame(location);
						}
					}
				}
			} else {
				for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
					Location<World> location = transaction.getFinal().getLocation().get();

					if (!Sponge.getServiceManager().provide(PortalService.class).get().get(location, PortalType.PORTAL).isPresent()) {
						continue;
					}

					event.setCancelled(true);
					break;
				}
			}
		} finally {
			timings.onChangeBlockEventPlace().stopTiming();
		}
	}

	@Listener
	public void onChangeBlockEventBreakCreate(ChangeBlockEvent.Break event, @Root Entity entity) {
		timings.onChangeBlockEventBreak().startTiming();

		try {
			if(entity instanceof Player) {
				Player player = (Player) entity;
				
				if (builders.containsKey(player.getUniqueId())) {
					PortalBuilder builder = builders.get(player.getUniqueId());

					for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
						Location<World> location = transaction.getFinal().getLocation().get();
						if (builder.isFill()) {
							builder.removeFill(location);
						} else {
							builder.removeFrame(location);
						}
					}
				}
			}else {
				for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
					Location<World> location = transaction.getFinal().getLocation().get();

					if (!Sponge.getServiceManager().provide(PortalService.class).get().get(location, PortalType.PORTAL).isPresent()) {
						continue;
					}

					event.setCancelled(true);
					break;
				}
			}
		} finally {
			timings.onChangeBlockEventBreak().stopTiming();
		}
	}
	
	@Listener
	public void onConnectionEvent(ClientConnectionEvent.Login event, @Root Player player) {
		Location<World> location = event.getToTransform().getLocation();

		PortalService portalService = Sponge.getServiceManager().provide(PortalService.class).get();
		
		while (portalService.get(location, PortalType.PORTAL).isPresent() || portalService.get(location, PortalType.DOOR).isPresent()) {
			ThreadLocalRandom random = ThreadLocalRandom.current();

			int x = (random.nextInt(3 * 2) - 3) + location.getBlockX();
			int z = (random.nextInt(3 * 2) - 3) + location.getBlockZ();

			Optional<Location<World>> optionalLocation = Sponge.getGame().getTeleportHelper().getSafeLocation(location.getExtent().getLocation(x, location.getBlockY(), z));

			if (optionalLocation.isPresent()) {
				location = optionalLocation.get();
				event.setToTransform(new Transform<World>(location));
			}
		}
		
		try {
			player.remove(LastLocationData.class);
		} catch (Exception e) { }
	}

	@Listener
	public void onConstructPortalEvent(ConstructPortalEvent event, @Root Player player) {
		timings.onConstructPortalEvent().startTiming();

		try {
			List<Location<World>> locations = event.getLocations();

			for (Location<World> location : event.getLocations()) {
				if (Sponge.getServiceManager().provide(PortalService.class).get().get(location, PortalType.PORTAL).isPresent()) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "Portals cannot over lap other portals"));
					event.setCancelled(true);
					return;
				}
			}

			ConfigurationNode config = ConfigManager.get(Main.getPlugin()).getConfig();

			int size = config.getNode("options", "portal", "size").getInt();
			if (locations.size() > size) {
				player.sendMessage(Text.of(TextColors.DARK_RED, "Portals cannot be larger than ", size, " blocks"));
				event.setCancelled(true);
				return;
			}

			if (locations.size() < 9) {
				player.sendMessage(Text.of(TextColors.DARK_RED, "Portal too small"));
				event.setCancelled(true);
				return;
			}
		} finally {
			timings.onConstructPortalEvent().stopTiming();
		}
	}

	@Listener
	public void onMoveEntityEventItem(MoveEntityEvent event, @Getter("getTargetEntity") Item item) {
		timings.onMoveEntityEvent().startTimingIfSync();

		try {
			Location<World> location = item.getLocation();

			Optional<Portal> optionalPortal = Sponge.getServiceManager().provide(PortalService.class).get().get(location, PortalType.PORTAL);

			if (!optionalPortal.isPresent()) {
				return;
			}
			Portal portal = optionalPortal.get();

			if (portal.getServer().isPresent()) {
				return;
			}

			if (!ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "portal", "teleport_item").getBoolean()) {
				return;
			}

			Optional<Coordinate> optionalCoordinate = portal.getCoordinate();
			
			if(!optionalCoordinate.isPresent()) {
				return;
			}
			Coordinate coordinate = optionalCoordinate.get();
			
			Optional<Location<World>> optionalSpawnLocation = coordinate.getLocation();

			if (!optionalSpawnLocation.isPresent()) {
				return;
			}
			Location<World> spawnLocation = optionalSpawnLocation.get();

			Vector3d rotation = portal.getRotation().toVector3d();

			event.setToTransform(new Transform<World>(spawnLocation.getExtent(), spawnLocation.getPosition(), rotation));
		} finally {
			timings.onMoveEntityEvent().stopTimingIfSync();
		}
	}

	private static List<UUID> mobCache = new ArrayList<>();
	
	@Listener
	public void onMoveEntityEventLiving(MoveEntityEvent event, @Getter("getTargetEntity") Living living) {
		if (living instanceof Player) {
			return;
		}

		timings.onMoveEntityEvent().startTimingIfSync();

		try {
			UUID uuid = living.getUniqueId();

			if (mobCache.contains(uuid)) {
				return;
			}
			
			Location<World> location = living.getLocation();

			Optional<Portal> optionalPortal = Sponge.getServiceManager().provide(PortalService.class).get().get(location, PortalType.PORTAL);

			if (!optionalPortal.isPresent()) {
				return;
			}
			Portal portal = optionalPortal.get();

			if (portal.getServer().isPresent()) {
				return;
			}

			if (!ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "portal", "teleport_mob").getBoolean()) {
				return;
			}

			Optional<Coordinate> optionalCoordinate = portal.getCoordinate();
			
			if(!optionalCoordinate.isPresent()) {
				return;
			}
			Coordinate coordinate = optionalCoordinate.get();
			
			Optional<Location<World>> optionalSpawnLocation = coordinate.getLocation();

			if (!optionalSpawnLocation.isPresent()) {
				return;
			}
			
			mobCache.add(uuid);
			
			Location<World> spawnLocation = optionalSpawnLocation.get();

			Vector3d rotation = portal.getRotation().toVector3d();

			living.setLocationAndRotation(spawnLocation, rotation);
			
			Sponge.getScheduler().createTaskBuilder().delayTicks(100).execute(c -> {
				mobCache.remove(uuid);
			}).submit(Main.getPlugin());
		} finally {
			timings.onMoveEntityEvent().stopTimingIfSync();
		}
	}

	private static List<UUID> playerCache = new ArrayList<>();

	@Listener(order = Order.FIRST)
	public void onMoveEntityEventPlayer(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {
		timings.onMoveEntityEvent().startTimingIfSync();

		try {
			UUID uuid = player.getUniqueId();

			if (playerCache.contains(uuid)) {
				return;
			}
			
			Location<World> location = event.getFromTransform().getLocation();

			PortalService portalService = Sponge.getServiceManager().provide(PortalService.class).get();
			
			Optional<Portal> optionalPortal = portalService.get(location, PortalType.PORTAL);

			if (!optionalPortal.isPresent()) {
				return;
			}
			Portal portal = optionalPortal.get();

			playerCache.add(uuid);

			portalService.execute(player, portal);

			Sponge.getScheduler().createTaskBuilder().delayTicks(40).execute(c -> {
				playerCache.remove(uuid);
			}).submit(Main.getPlugin());
		} finally {
			timings.onMoveEntityEvent().stopTimingIfSync();
		}
	}

	@Listener
	public void onChangeBlockEventPlace(ChangeBlockEvent.Place event, @Root Entity entity) {
		timings.onChangeBlockEventPlace().startTiming();

		try {
			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				Location<World> location = transaction.getFinal().getLocation().get();

				if (!Sponge.getServiceManager().provide(PortalService.class).get().get(location, PortalType.PORTAL).isPresent()) {
					continue;
				}

				event.setCancelled(true);
				break;
			}
		} finally {
			timings.onChangeBlockEventPlace().stopTiming();
		}
	}

	@Listener
	public void onChangeBlockEventBreak(ChangeBlockEvent.Break event, @Root Entity entity) {
		timings.onChangeBlockEventBreak().startTiming();

		try {
			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				Location<World> location = transaction.getFinal().getLocation().get();

				if (!Sponge.getServiceManager().provide(PortalService.class).get().get(location, PortalType.PORTAL).isPresent()) {
					continue;
				}

				event.setCancelled(true);
				break;
			}
		} finally {
			timings.onChangeBlockEventBreak().stopTiming();
		}
	}
}
