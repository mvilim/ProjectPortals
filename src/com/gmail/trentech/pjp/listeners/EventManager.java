package com.gmail.trentech.pjp.listeners;

import java.util.function.Consumer;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.ConfigManager;
import com.gmail.trentech.pjp.Resource;
import com.gmail.trentech.pjp.commands.CMDBack;
import com.gmail.trentech.pjp.events.TeleportEvent;
import com.gmail.trentech.pjp.portals.LocationType;

public class EventManager {

	@Listener
	public void onRespawnPlayerEvent(RespawnPlayerEvent event, @First Player player){
		System.out.println("FIRE");
	}
	
	@Listener
	public void onTeleportEvent(TeleportEvent event, @First Player player){
		Location<World> src = event.getSrc();
		Location<World> dest = event.getDest();

		if(!(player.hasPermission("pjp.worlds." + dest.getExtent().getName()) || player.hasPermission("pjw.worlds." + dest.getExtent().getName()))){
			player.sendMessage(Text.of(TextColors.DARK_RED, "You do not have permission to travel to ", dest.getExtent().getName()));
			return;
		}
		
		if(!player.setLocationSafely(dest)){
			player.sendMessage(Text.builder().color(TextColors.DARK_RED).append(Text.of("Unsafe spawn point detected. Teleport anyway? ")).onClick(TextActions.executeCallback(unsafeTeleport(dest))).append(Text.of(TextColors.GOLD, TextStyles.UNDERLINE, "Click Here")).build());
			return;
		}
		
		if(new ConfigManager().getConfig().getNode("Options", "Show-Particles").getBoolean()){
			Resource.spawnParticles(src, 0.5, true);
			Resource.spawnParticles(src.getRelative(Direction.UP), 0.5, true);
			
			Resource.spawnParticles(dest, 1.0, false);
			Resource.spawnParticles(dest.getRelative(Direction.UP), 1.0, false);
		}

		player.sendTitle(Title.of(Text.of(TextColors.DARK_GREEN, Resource.getPrettyName(dest.getExtent().getName())), Text.of(TextColors.AQUA, "x: ", dest.getBlockX(), ", y: ", dest.getBlockY(),", z: ", dest.getBlockZ())));
		
		if(event.getLocationType().equals(LocationType.RANDOM)){
			Resource.generateRandomLocation(dest.getExtent());
		}
		
		if(player.hasPermission("pjp.cmd.back")){
			CMDBack.players.put(player, src);
		}
	}
	
    @Listener
    public void onDamageEntityEvent(DamageEntityEvent event, @First BlockDamageSource damageSource) {
    	if(!(event.getTargetEntity() instanceof Player)) {
    		return;
    	}

        BlockSnapshot block = damageSource.getBlockSnapshot();
        
        if(!block.getState().getType().equals(BlockTypes.FLOWING_LAVA)){
        	return;
        }
        
        Location<World> location = block.getLocation().get();
        
		String locationName = location.getExtent().getName() + "." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();

		if(new ConfigManager("portals.conf").getConfig().getNode("Cuboids", locationName, "World").getString() == null){
			return;
		}
		
		event.setCancelled(true);
    }
    
    @Listener
    public void onIgniteEntityEvent(IgniteEntityEvent event, @First BlockDamageSource damageSource) {
    	if(!(event.getTargetEntity() instanceof Player)) {
    		return;
    	}

        BlockSnapshot block = damageSource.getBlockSnapshot();
        
        if(!block.getState().getType().equals(BlockTypes.FLOWING_LAVA)){
        	return;
        }
        
        Location<World> location = block.getLocation().get();
        
		String locationName = location.getExtent().getName() + "." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();

		if(new ConfigManager("portals.conf").getConfig().getNode("Cuboids", locationName, "World").getString() == null){
			return;
		}
		
		event.setCancelled(true);
    }

	public static Consumer<CommandSource> unsafeTeleport(Location<World> location){
		return (CommandSource src) -> {
			Player player = (Player)src;

			player.setLocation(location);
			player.sendTitle(Title.of(Text.of(TextColors.GOLD, Resource.getPrettyName(location.getExtent().getName())), Text.of(TextColors.DARK_PURPLE, "x: ", location.getBlockX(), ", y: ", location.getBlockY(),", z: ", location.getBlockZ())));
		};
	}
}
