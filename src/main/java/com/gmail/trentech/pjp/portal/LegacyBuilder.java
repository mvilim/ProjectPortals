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

import com.gmail.trentech.pjp.effects.Particle;
import com.gmail.trentech.pjp.effects.ParticleColor;
import com.gmail.trentech.pjp.effects.Particles;
import com.gmail.trentech.pjp.events.ConstructPortalEvent;

public class LegacyBuilder {

	private boolean fill = false;
	private Portal portal;

	public LegacyBuilder(Portal portal) {
		if (portal.getProperties().isPresent()) {
			this.portal = portal;
		}
	}

	public Portal getPortal() {
		return portal;
	}

	public LegacyBuilder addFrame(Location<World> location) {
		portal.getProperties().get().addFrame(location);
		return this;
	}

	public LegacyBuilder removeFrame(Location<World> location) {
		portal.getProperties().get().removeFrame(location);
		return this;
	}

	public LegacyBuilder addFill(Location<World> location) {
		portal.getProperties().get().addFill(location);
		return this;
	}

	public LegacyBuilder removeFill(Location<World> location) {
		portal.getProperties().get().removeFill(location);
		return this;
	}

	public boolean isFill() {
		return fill;
	}

	public LegacyBuilder setFill(boolean fill) {
		this.fill = fill;
		return this;
	}

	public boolean build(Player player) {
		if (portal.getProperties().get().getFill().isEmpty()) {
			return false;
		}

		if (!Sponge.getEventManager().post(new ConstructPortalEvent(portal.getProperties().get().getFrame(), portal.getProperties().get().getFill(), Cause.builder().append(portal).build(EventContext.builder().add(EventContextKeys.CREATOR, player).build())))) {
			BlockState block = BlockTypes.AIR.getDefaultState();

			Particle particle = Particles.getDefaultEffect("creation");
			Optional<ParticleColor> color = Particles.getDefaultColor("creation", particle.isColorable());

			for (Location<World> location : portal.getProperties().get().getFill()) {
				if (!location.getExtent().setBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ(), block)) {
					return false;
				}

				particle.spawnParticle(location, false, color);
			}

			Sponge.getServiceManager().provide(PortalService.class).get().create(portal, portal.getName());

			return true;
		}
		return false;
	}

}
