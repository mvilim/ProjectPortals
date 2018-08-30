package com.gmail.trentech.pjp.portal;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.effects.Effect;
import com.gmail.trentech.pjp.effects.PortalEffect;
import com.gmail.trentech.pjp.events.ConstructPortalEvent;
import com.gmail.trentech.pjp.portal.features.Properties;

import ninja.leaping.configurate.ConfigurationNode;

public class PortalBuilder {

	private boolean fill = false;
	private Portal portal;

	public PortalBuilder(Portal portal) {
		if (portal.getProperties().isPresent()) {
			this.portal = portal;
		}
	}

	public Portal getPortal() {
		return portal;
	}

	public PortalBuilder addFrame(Location<World> location) {
		portal.getProperties().get().addFrame(location);
		return this;
	}

	public PortalBuilder removeFrame(Location<World> location) {
		portal.getProperties().get().removeFrame(location);
		return this;
	}

	public PortalBuilder addFill(Location<World> location) {
		portal.getProperties().get().addFill(location);
		return this;
	}

	public PortalBuilder removeFill(Location<World> location) {
		portal.getProperties().get().removeFill(location);
		return this;
	}

	public boolean isFill() {
		return fill;
	}

	public PortalBuilder setFill(boolean fill) {
		this.fill = fill;
		return this;
	}

	public boolean build(Player player) {
		Optional<Properties> optional = portal.getProperties();
		
		if(!optional.isPresent()) {
			return false;
		}
		Properties properties = optional.get();
		
		if (properties.getFill().isEmpty()) {
			return false;
		}

		if (!Sponge.getEventManager().post(new ConstructPortalEvent(properties.getFrame(), properties.getFill(), Cause.builder().append(portal).build(EventContext.builder().add(EventContextKeys.CREATOR, player).build())))) {
			BlockState block = BlockTypes.AIR.getDefaultState();

			for (Location<World> location : properties.getFill()) {
				if (!location.getExtent().setBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ(), block)) {
					return false;
				}
			}

			ConfigurationNode node = ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "particles");
			
			if(node.getNode("enable").getBoolean()) {
				String particle = node.getNode("portal", "type").getString();
				
				String optionType = node.getNode("portal", "option", "type").getString();
				String optionValue = node.getNode("portal", "option", "value").getString();
				
				Effect effect;
				if(!optionType.equalsIgnoreCase("none") && !optionValue.equalsIgnoreCase("none")) {
					effect = new Effect(particle, optionType, optionValue);
				} else {
					effect = new Effect(particle);
				}

				properties.setEffect(Optional.of(effect));
				portal.setProperties(properties);
			}
			
			Sponge.getServiceManager().provide(PortalService.class).get().create(portal);

			for(Location<World> location : properties.getFill()) {
				PortalEffect.create(location);
			}
			return true;
		}
		return false;
	}

}
