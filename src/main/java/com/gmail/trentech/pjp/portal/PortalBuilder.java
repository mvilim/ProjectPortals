package com.gmail.trentech.pjp.portal;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.events.ConstructPortalEvent;

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
		if (portal.getProperties().get().getFill().isEmpty()) {
			return false;
		}

		if (!Sponge.getEventManager().post(new ConstructPortalEvent(portal.getProperties().get().getFrame(), portal.getProperties().get().getFill(), Cause.builder().append(portal).build(EventContext.builder().add(EventContextKeys.CREATOR, player).build())))) {
			BlockState block = BlockTypes.AIR.getDefaultState();

			for (Location<World> location : portal.getProperties().get().getFill()) {
				if (!location.getExtent().setBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ(), block)) {
					return false;
				}
			}

			Sponge.getServiceManager().provide(PortalService.class).get().create(portal, portal.getName());

			return true;
		}
		return false;
	}

}
