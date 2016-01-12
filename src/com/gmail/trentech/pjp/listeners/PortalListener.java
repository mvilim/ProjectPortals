package com.gmail.trentech.pjp.listeners;

import java.util.HashMap;
import java.util.List;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.events.ConstructPortalEvent;
import com.gmail.trentech.pjp.events.TeleportEvent;
import com.gmail.trentech.pjp.portals.Portal;
import com.gmail.trentech.pjp.portals.builders.Builder;
import com.gmail.trentech.pjp.portals.builders.PortalBuilder;
import com.gmail.trentech.pjp.utils.ConfigManager;

import ninja.leaping.configurate.ConfigurationNode;

public class PortalListener {

	private static HashMap<Player, Builder> builders = new HashMap<>();
	
	@Listener
	public void onConstructPortalEvent(ConstructPortalEvent event, @First Player player){
		for(String locationName : event.getLocations()){
			if(Portal.get(locationName).isPresent()){
	        	player.sendMessage(Text.of(TextColors.DARK_RED, "Portals cannot over lap over portals"));
	        	event.setCancelled(true);
	        	return;
			}
		}

        List<String> locations = event.getLocations();
        
        ConfigurationNode config = new ConfigManager().getConfig();
        
        int size = config.getNode("Options", "Cube", "Size").getInt();
        if(locations.size() > size){
        	player.sendMessage(Text.of(TextColors.DARK_RED, "Portals cannot be larger than ", size, " blocks"));
        	event.setCancelled(true);
        	return;
        }
        
        if(locations.size() == 1){
        	player.sendMessage(Text.of(TextColors.DARK_RED, "Portal too small"));
        	player.setItemInHand(null);
        	event.setCancelled(true);        	
        	return;
        }
	}
	
	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Place event, @First Player player) {
		if(!builders.containsKey(player)){
			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				Location<World> location = transaction.getFinal().getLocation().get();		

				if(!Portal.listAllLocations().contains(location)){
					continue;
				}

				event.setCancelled(true);
				break;
			}
			return;
		}
		PortalBuilder builder = (PortalBuilder) builders.get(player);
		
		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			Location<World> location = transaction.getFinal().getLocation().get();
			builder.add(location);
			player.sendMessage(Text.of(TextColors.DARK_GREEN, "Added location to ", builder.getName()));
		}
	}
	
	@Listener
	public void onChangeBlockEvent(ChangeBlockEvent.Break event, @First Player player) {
		if(!builders.containsKey(player)){
			return;
		}
		PortalBuilder builder = (PortalBuilder) builders.get(player);
		
		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			Location<World> location = transaction.getFinal().getLocation().get();
			builder.remove(location);
			player.sendMessage(Text.of(TextColors.DARK_GREEN, "Removed location from ", builder.getName()));
		}
	}

	@Listener
	public void onDisplaceEntityEvent(DisplaceEntityEvent.TargetPlayer event){
		if (!(event.getTargetEntity() instanceof Player)){
			return;
		}
		Player player = (Player) event.getTargetEntity();

		Location<World> location = player.getLocation();		
		String locationName = location.getExtent().getName() + ":" + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();

		if(!Portal.get(locationName).isPresent()){
			return;
		}
		Portal portal = Portal.get(locationName).get();

		if(!player.hasPermission("pjp.cube.interact")){
			player.sendMessage(Text.of(TextColors.DARK_RED, "You do not have permission to interact with portals"));
			event.setCancelled(true);
			return;
		}
		
		if(!portal.getDestination().isPresent()){
			player.sendMessage(Text.of(TextColors.DARK_RED, "World does not exist"));
			return;
		}
		Location<World> spawnLocation = portal.getDestination().get();

		TeleportEvent teleportEvent = new TeleportEvent(player, player.getLocation(), spawnLocation, Cause.of("portal"));

		if(!Main.getGame().getEventManager().post(teleportEvent)){
			spawnLocation = teleportEvent.getDestination();
			player.setLocation(spawnLocation);
		}
	}
	
	public static HashMap<Player, Builder> getBuilders() {
		return builders;
	}
}
