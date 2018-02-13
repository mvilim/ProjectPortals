package com.gmail.trentech.pjp.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalService;

public class Particle {

	private final String name;
	private final ParticleType type;
	private final long time;

	private static ThreadLocalRandom random = ThreadLocalRandom.current();

	protected Particle(String name, ParticleType type, long time) {
		this.name = name;
		this.type = type;
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public ParticleType getType() {
		return type;
	}

	private long getTime() {
		return time;
	}

	public boolean isColorable() {
		if (getType() == null) {
			return false;
		}

		if (getType().getDefaultOption(ParticleOptions.COLOR).isPresent()) {
			return true;
		}

		return false;
	}

	public void spawnParticle(Location<World> location, boolean player, Optional<ParticleColor> color) {
		if (getType() == null) {
			return;
		}

		if (ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "particles", "enable").getBoolean()) {
			spawnNonRepeat(location, player, color);
		}
	}

	public void createTask(String name, List<Location<World>> locations, Optional<ParticleColor> color) {
		if (getType() == null) {
			return;
		}

		if (ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "particles", "enable").getBoolean()) {
			spawnRepeat(name, locations, color);
		}
	}

	private void spawnNonRepeat(Location<World> location, boolean player, Optional<ParticleColor> color) {
		if (getType() == null) {
			return;
		}

		ParticleEffect particle = ParticleEffect.builder().type(getType()).build();

		for (int i = 0; i < 9; i++) {
			if (isColorable() && color.isPresent()) {
				particle = ParticleEffect.builder().type(getType()).option(ParticleOptions.COLOR, color.get().getColor()).build();
			}
			
			if (player) {
				location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5));
				location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5));
			} else {
				location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble(), random.nextDouble(), random.nextDouble()));
				location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble(), random.nextDouble(), random.nextDouble()));
			}
		}
	}

	private void spawnRepeat(String name, List<Location<World>> locations, Optional<ParticleColor> color) {
		if (getType() == null) {
			return;
		}

		if (getName().equals("PORTAL_SHIMMER") || getName().equals("WATER_FLOW") || getName().equals("LAVA_FLOW")) {
			Portal portal = Sponge.getServiceManager().provide(PortalService.class).get().get(locations.get(0), PortalType.PORTAL).get();

			Sponge.getScheduler().createTaskBuilder().intervalTicks(getTime()).name(portal.getName()).execute(t -> {
				portal.getProperties().get().blockUpdate(false);
			}).submit(Main.getPlugin());
		} else if(getName().equalsIgnoreCase("DRIP_WATER") || getName().equalsIgnoreCase("DRIP_LAVA")) {
			ArrayList<Location<World>> list = new ArrayList<>();
			
			first:
			for(Location<World> location : locations) {
				for(Location<World> loc : locations) {
					if(location.getY() < loc.getY()) {
						continue first;
					}
				}
				list.add(location);
			}
			
			Sponge.getScheduler().createTaskBuilder().interval(getTime(), TimeUnit.MILLISECONDS).name(name).execute(t -> {
				ParticleEffect particle = ParticleEffect.builder().type(getType()).build();

				for (Location<World> location : list) {
					Optional<Chunk> optionalChunk = location.getExtent().getChunk(location.getChunkPosition());
					
					if(optionalChunk.isPresent() && optionalChunk.get().isLoaded()) {
						location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble(), 1, random.nextDouble()));
						location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble(), 1, random.nextDouble()));
					}
				}
			}).submit(Main.getPlugin());
		} else {
			Sponge.getScheduler().createTaskBuilder().interval(getTime(), TimeUnit.MILLISECONDS).name(name).execute(t -> {
				ParticleEffect particle = ParticleEffect.builder().type(getType()).build();

				for (Location<World> location : locations) {
					Optional<Chunk> optionalChunk = location.getExtent().getChunk(location.getChunkPosition());
					
					if(optionalChunk.isPresent() && optionalChunk.get().isLoaded()) {
						if (isColorable() && color.isPresent()) {
							particle = ParticleEffect.builder().type(getType()).option(ParticleOptions.COLOR, color.get().getColor()).build();
						}

						location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble(), random.nextDouble(), random.nextDouble()));
						location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble(), random.nextDouble(), random.nextDouble()));
					}
				}
			}).submit(Main.getPlugin());
		}
	}
}
