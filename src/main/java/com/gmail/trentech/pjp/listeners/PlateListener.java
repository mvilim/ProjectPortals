package com.gmail.trentech.pjp.listeners;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DisplaceEntityEvent.TargetPlayer;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.data.object.Plate;
import com.gmail.trentech.pjp.effects.Particle;
import com.gmail.trentech.pjp.effects.Particles;
import com.gmail.trentech.pjp.events.TeleportEvent;
import com.gmail.trentech.pjp.events.TeleportEvent.Local;
import com.gmail.trentech.pjp.events.TeleportEvent.Server;
import com.gmail.trentech.pjp.utils.ConfigManager;

import flavor.pie.spongee.Spongee;

public class PlateListener {

	public static ConcurrentHashMap<UUID, Plate> builders = new ConcurrentHashMap<>();

	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Modify event, @First Player player) {
		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			BlockSnapshot snapshot = transaction.getFinal();
			BlockState block = snapshot.getExtendedState();
			BlockType blockType = block.getType();
			
			if(!blockType.equals(BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE) && !blockType.equals(BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE) 
					&& !blockType.equals(BlockTypes.STONE_PRESSURE_PLATE) && !blockType.equals(BlockTypes.WOODEN_PRESSURE_PLATE)) {
				continue;
			}

			if(!block.get(Keys.POWERED).isPresent()) {
				continue;
			}

			if(!block.get(Keys.POWERED).get()) {
				continue;
			}

			Location<World> location = snapshot.getLocation().get();		

			Optional<Plate> optionalPlate = Plate.get(location);
			
			if(!optionalPlate.isPresent()) {
				continue;
			}
			Plate plate = optionalPlate.get();
			
			if(new ConfigManager().getConfig().getNode("options", "advanced_permissions").getBoolean()) {
				if(!player.hasPermission("pjp.plate." + location.getExtent().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ())) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "You do not have permission to use this pressure plate portal"));
					event.setCancelled(true);
					return;
				}
			}else{
				if(!player.hasPermission("pjp.plate.interact")) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to interact with pressure plate portals"));
					event.setCancelled(true);
					return;
				}
			}
			
			if(plate.isBungee()) {
				String source = "source";
				
				Server teleportEvent = new TeleportEvent.Server(player, source, plate.getServer(), plate.getPrice(), Cause.of(NamedCause.source(plate)));

				if(!Main.getGame().getEventManager().post(teleportEvent)) {
					Spongee.API.connectPlayer(player, teleportEvent.getDestination());
				}
			}else {
				Optional<Location<World>> optionalSpawnLocation = plate.getDestination();
				
				if(!optionalSpawnLocation.isPresent()) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "World does not exist"));
					continue;
				}
				Location<World> spawnLocation = optionalSpawnLocation.get();

				Local teleportEvent = new TeleportEvent.Local(player, player.getLocation(), spawnLocation, plate.getPrice(), Cause.of(NamedCause.source(plate)));

				if(!Main.getGame().getEventManager().post(teleportEvent)) {
					Location<World> currentLocation = player.getLocation();
					spawnLocation = teleportEvent.getDestination();
					
					Vector3d rotation = plate.getRotation().toVector3d();

					player.setLocationAndRotation(spawnLocation, rotation);
					
					TargetPlayer displaceEvent = SpongeEventFactory.createDisplaceEntityEventTargetPlayer(Cause.of(NamedCause.source(this)), new Transform<World>(currentLocation), new Transform<World>(spawnLocation), player);
					Main.getGame().getEventManager().post(displaceEvent);
				}
			}
		}
	}

	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Break event, @First Player player) {
		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			Location<World> location = transaction.getFinal().getLocation().get();		

			Optional<Plate> optionalPlate = Plate.get(location);
			
			if(!optionalPlate.isPresent()) {
				continue;
			}
			Plate plate = optionalPlate.get();
			
			if(!player.hasPermission("pjp.plate.break")) {
				player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to break pressure plate portals"));
				event.setCancelled(true);
			}else{
				plate.remove();
				player.sendMessage(Text.of(TextColors.DARK_GREEN, "Broke pressure plate portal"));
			}
		}
		return;
	}

	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Place event, @First Player player) {
		if(!builders.containsKey(player.getUniqueId())) {
			return;
		}

		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			BlockType blockType = transaction.getFinal().getState().getType();
			
			if(!blockType.equals(BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE) && !blockType.equals(BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE) 
					&& !blockType.equals(BlockTypes.STONE_PRESSURE_PLATE) && !blockType.equals(BlockTypes.WOODEN_PRESSURE_PLATE)) {
				continue;
			}

			Location<World> location = transaction.getFinal().getLocation().get();

			if(!player.hasPermission("pjp.plate.place")) {
	        	player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to place pressure plate portals"));
	        	builders.remove(player.getUniqueId());
	        	return;
			}

			Plate plate = builders.get(player.getUniqueId());
			plate.setLocation(location);
			plate.create();

			Particle particle = Particles.getDefaultEffect("creation");
			particle.spawnParticle(location, false, Particles.getDefaultColor("creation", particle.isColorable()));

            player.sendMessage(Text.of(TextColors.DARK_GREEN, "New pressure plate portal created"));
            
            builders.remove(player.getUniqueId());
		}
	}
}
