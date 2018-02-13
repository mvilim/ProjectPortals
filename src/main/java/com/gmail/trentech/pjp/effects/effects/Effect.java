package com.gmail.trentech.pjp.effects.effects;

import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.portal.Portal;

public abstract interface Effect {

	public void activate(Portal portal);
	public void deactivate(Portal portal);
	public void burst(Location<World> location, boolean player);
	
	ThreadLocalRandom random = ThreadLocalRandom.current();
}
