package com.gmail.trentech.pjp.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleEffect.Builder;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.features.Properties;

import ninja.leaping.configurate.ConfigurationNode;

public class PortalEffect {

	private static ThreadLocalRandom random = ThreadLocalRandom.current();
	
	public static void activate(Portal portal) {
		Optional<Properties> optionalProperties = portal.getProperties();
		
		if(!optionalProperties.isPresent()) {
			return;
		}
		Properties properties = optionalProperties.get();
		
		Optional<ParticleEffect> optionalParticle = properties.getParticle();
		
		if(optionalParticle.isPresent()) {
			ParticleEffect particle = optionalParticle.get();
					
			AtomicReference<List<Location<World>>> list = new AtomicReference<>(properties.getFill());
			AtomicReference<Double> y = new AtomicReference<>(random.nextDouble());

			if(particle.getType().equals(ParticleTypes.DRIP_LAVA) || particle.getType().equals(ParticleTypes.DRIP_WATER) || particle.getType().equals(ParticleTypes.FIREWORKS_SPARK)) {
				List<Location<World>> locs = new ArrayList<>();
				
				first:
				for(Location<World> location : properties.getFill()) {
					for(Location<World> loc : properties.getFill()) {
						if(location.getY() < loc.getY()) {
							continue first;
						}
					}
					locs.add(location);
				}
				
				y.set(1.0);
				list.set(locs);
			}
			
			Sponge.getScheduler().createTaskBuilder().interval(properties.getIntensity(), TimeUnit.MILLISECONDS).name(portal.getName() + "particleupdate").execute(t -> {
				for (Location<World> location : list.get()) {
					Optional<Chunk> optionalChunk = location.getExtent().getChunk(location.getChunkPosition());
					
					if(optionalChunk.isPresent() && optionalChunk.get().isLoaded()) {
						location.getExtent().spawnParticles(optionalParticle.get(), location.getPosition().add(random.nextDouble(), y.get(), random.nextDouble()), 64);
						location.getExtent().spawnParticles(optionalParticle.get(), location.getPosition().add(random.nextDouble(), y.get(), random.nextDouble()), 64);
					}
				}
			}).submit(Main.getPlugin());
		}
		
		Optional<BlockState> optionalBlockState = properties.getBlockState();
		
		if(optionalBlockState.isPresent()) {
			BlockState blockState = getBlock(properties.getFrame(), properties.getFill(), optionalBlockState.get());
			
			Sponge.getScheduler().createTaskBuilder().intervalTicks(100).name(portal.getName() + "blockupdate").execute(t -> {
				World world = properties.getFrame().get(0).getExtent();

				Predicate<Entity> filter = e -> {
					return e.getType().equals(EntityTypes.PLAYER);
				};

				for (Entity entity : world.getEntities(filter)) {
					Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(c -> {
						for (Location<World> location : properties.getFill()) {
							Optional<Chunk> optionalChunk = location.getExtent().getChunk(location.getChunkPosition());
							
							if(optionalChunk.isPresent() && optionalChunk.get().isLoaded()) {
								((Player) entity).sendBlockChange(location.getBlockPosition(), blockState);
							}
						}
					}).submit(Main.getPlugin());
				}
			}).submit(Main.getPlugin());
		}
	}

	public static void burst(ParticleEffect particle, Location<World> location, boolean player) {
		for (int i = 0; i < 9; i++) {
			if (player) {
				location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5), 64);
				location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5), 64);
			} else {
				location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble(), random.nextDouble(), random.nextDouble()), 64);
				location.getExtent().spawnParticles(particle, location.getPosition().add(random.nextDouble(), random.nextDouble(), random.nextDouble()), 64);
			}
		}
	}

	public static void teleport(Location<World> location) {
		ConfigurationNode node = ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "particles");
		
		if(node.getNode("enable").getBoolean()) {
			Optional<ParticleType> particleType = Sponge.getRegistry().getType(ParticleType.class, node.getNode("teleport", "type").getString());
			
			if(particleType.isPresent()) {
				Builder builder = ParticleEffect.builder().type(particleType.get());
				
				String colorName = node.getNode("teleport", "color").getString();
				
				if(!colorName.equalsIgnoreCase("none")) {
					Optional<Color> color = Colors.get(colorName);
					
					if(color.isPresent()) {
						builder.option(ParticleOptions.COLOR, color.get());
					}
				}

				PortalEffect.burst(builder.build(), location, true);
			}	
		}	
	}
		
	public static void create(Location<World> location) {
		ConfigurationNode node = ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "particles");
		
		if(node.getNode("enable").getBoolean()) {
			Optional<ParticleType> particleType = Sponge.getRegistry().getType(ParticleType.class, node.getNode("creation", "type").getString());
			
			if(particleType.isPresent()) {
				Builder builder = ParticleEffect.builder().type(particleType.get());
				
				String colorName = node.getNode("creation", "color").getString();
				
				if(!colorName.equalsIgnoreCase("none")) {
					Optional<Color> color = Colors.get(colorName);
					
					if(color.isPresent()) {
						builder.option(ParticleOptions.COLOR, color.get());
					}
				}

				PortalEffect.burst(builder.build(), location, false);
			}	
		}	
	}
	
	public static void deactivate(Portal portal) {
		for (Task task : Sponge.getScheduler().getScheduledTasks()) {
			if (task.getName().equalsIgnoreCase(portal.getName() + "blockudate") || task.getName().equalsIgnoreCase(portal.getName() + "particleupdate")) {
				task.cancel();
			}
		}
		
		Optional<Properties> optionalProperties = portal.getProperties();
		
		if(!optionalProperties.isPresent()) {
			return;
		}
		Properties properties = optionalProperties.get();
		
		Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(c -> {
			World world = properties.getFrame().get(0).getExtent();

			Predicate<Entity> filter = e -> {
				return e.getType().equals(EntityTypes.PLAYER);
			};
			
			for (Entity entity : world.getEntities(filter)) {
				for (Location<World> location : properties.getFill()) {
					Optional<Chunk> optionalChunk = location.getExtent().getChunk(location.getChunkPosition());
					
					if(optionalChunk.isPresent() && optionalChunk.get().isLoaded()) {
						((Player) entity).resetBlockChange(location.getBlockPosition());
					}
				}
			}
		}).submit(Main.getPlugin());
	}
	
	private static BlockState getBlock(List<Location<World>> frame, List<Location<World>> fill, BlockState blockState) {
		if(blockState.supports(Keys.AXIS)) {
			blockState = blockState.with(Keys.AXIS, Axis.Z).get();

			List<Vector3i> frameV = new ArrayList<>();

			for (Location<World> location : frame) {
				frameV.add(location.getBlockPosition());
			}

			for (Location<World> location : fill) {
				Location<World> east = location.getRelative(Direction.EAST);
				Location<World> west = location.getRelative(Direction.WEST);
				Location<World> north = location.getRelative(Direction.NORTH);
				Location<World> south = location.getRelative(Direction.SOUTH);
				Location<World> up = location.getRelative(Direction.UP);
				Location<World> down = location.getRelative(Direction.DOWN);

				if (frameV.contains(east.getBlockPosition()) && frameV.contains(up.getBlockPosition()) && !frameV.contains(north.getBlockPosition()) && !frameV.contains(south.getBlockPosition())) {
					blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.X).get();
					break;
				} else if (frameV.contains(west.getBlockPosition()) && frameV.contains(up.getBlockPosition()) && !frameV.contains(north.getBlockPosition()) && !frameV.contains(south.getBlockPosition())) {
					blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.X).get();
					break;
				} else if (frameV.contains(east.getBlockPosition()) && frameV.contains(down.getBlockPosition()) && !frameV.contains(north.getBlockPosition()) && !frameV.contains(south.getBlockPosition())) {
					blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.X).get();
					break;
				} else if (frameV.contains(west.getBlockPosition()) && frameV.contains(down.getBlockPosition()) && !frameV.contains(north.getBlockPosition()) && !frameV.contains(south.getBlockPosition())) {
					blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.X).get();
					break;
				} else if (frameV.contains(north.getBlockPosition()) && frameV.contains(up.getBlockPosition()) && !frameV.contains(east.getBlockPosition()) && !frameV.contains(west.getBlockPosition())) {
					blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();
					break;
				} else if (frameV.contains(south.getBlockPosition()) && frameV.contains(up.getBlockPosition()) && !frameV.contains(east.getBlockPosition()) && !frameV.contains(west.getBlockPosition())) {
					blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();
					break;
				} else if (frameV.contains(north.getBlockPosition()) && frameV.contains(down.getBlockPosition()) && !frameV.contains(east.getBlockPosition()) && !frameV.contains(west.getBlockPosition())) {
					blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();
					break;
				} else if (frameV.contains(south.getBlockPosition()) && frameV.contains(down.getBlockPosition()) && !frameV.contains(east.getBlockPosition()) && !frameV.contains(west.getBlockPosition())) {
					blockState = BlockTypes.PORTAL.getDefaultState().with(Keys.AXIS, Axis.Z).get();
					break;
				} else if (frameV.contains(east.getBlockPosition()) && frameV.contains(north.getBlockPosition()) && !frameV.contains(up.getBlockPosition()) && !frameV.contains(down.getBlockPosition())) {
					blockState = BlockTypes.END_PORTAL.getDefaultState();
					break;
				} else if (frameV.contains(west.getBlockPosition()) && frameV.contains(north.getBlockPosition()) && !frameV.contains(up.getBlockPosition()) && !frameV.contains(down.getBlockPosition())) {
					blockState = BlockTypes.END_PORTAL.getDefaultState();
					break;
				} else if (frameV.contains(east.getBlockPosition()) && frameV.contains(south.getBlockPosition()) && !frameV.contains(up.getBlockPosition()) && !frameV.contains(down.getBlockPosition())) {
					blockState = BlockTypes.END_PORTAL.getDefaultState();
					break;
				} else if (frameV.contains(west.getBlockPosition()) && frameV.contains(south.getBlockPosition()) && !frameV.contains(up.getBlockPosition()) && !frameV.contains(down.getBlockPosition())) {
					blockState = BlockTypes.END_PORTAL.getDefaultState();
					break;
				}
			}
			return blockState;
		}
		
		return blockState;
	}

}
