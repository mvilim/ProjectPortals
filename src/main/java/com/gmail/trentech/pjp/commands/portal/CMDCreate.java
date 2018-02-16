package com.gmail.trentech.pjp.commands.portal;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleEffect.Builder;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjc.core.BungeeManager;
import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.effects.Colors;
import com.gmail.trentech.pjp.listeners.PortalListener;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalBuilder;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.features.Coordinate;
import com.gmail.trentech.pjp.portal.features.Coordinate.Preset;
import com.gmail.trentech.pjp.portal.features.Properties;
import com.gmail.trentech.pjp.rotation.Rotation;

import ninja.leaping.configurate.ConfigurationNode;

public class CMDCreate implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Help help = Help.get("portal create").get();
		
		if (args.hasAny("help")) {		
			help.execute(src);
			return CommandResult.empty();
		}
		
		if (!(src instanceof Player)) {
			throw new CommandException(Text.of(TextColors.RED, "Must be a player"), false);
		}
		Player player = (Player) src;

		if (!args.hasAny("name")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		String name = args.<String>getOne("name").get().toLowerCase();

		if (Sponge.getServiceManager().provide(PortalService.class).get().get(name, PortalType.PORTAL).isPresent()) {
			throw new CommandException(Text.of(TextColors.RED, name, " already exists"), false);
		}

		if (!args.hasAny("destination")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		String destination = args.<String>getOne("destination").get();

		ConfigurationNode node = ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "particles");

		Optional<ParticleType> particleType = Sponge.getRegistry().getType(ParticleType.class, node.getNode("teleport", "type").getString());
		
		Properties properties = new Properties();
		
		if(particleType.isPresent()) {
			Builder builder = ParticleEffect.builder().type(particleType.get());
			
			String colorName = node.getNode("teleport", "color").getString();
			
			if(!colorName.equalsIgnoreCase("none")) {
				Optional<Color> color = Colors.get(colorName);
				
				if(color.isPresent()) {
					builder.option(ParticleOptions.COLOR, color.get());
				}
			}
			properties.setParticle(Optional.of(builder.build()));
		}	
		
		if (args.hasAny("b")) {
			Consumer<List<String>> consumer1 = (list) -> {
				if (!list.contains(destination)) {
					try {
						throw new CommandException(Text.of(TextColors.RED, destination, " does not exist"), false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Consumer<String> consumer2 = (s) -> {
					if (destination.equalsIgnoreCase(s)) {
						try {
							throw new CommandException(Text.of(TextColors.RED, "Destination cannot be the server you are currently on"), false);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					Portal.Server server = new Portal.Server(name, PortalType.PORTAL, destination);
					server.setProperties(properties);
					
					PortalListener.builders.put(player.getUniqueId(), new PortalBuilder(server));
				};
				BungeeManager.getServer(consumer2, player);
			};			
			BungeeManager.getServers(consumer1, player);
		} else {
			Optional<World> world = Sponge.getServer().getWorld(destination);

			if (!world.isPresent()) {
				throw new CommandException(Text.of(TextColors.RED, destination, " is not loaded or does not exist"), false);
			}

			Portal.Local local = new Portal.Local(name, PortalType.PORTAL);
			
			if (args.hasAny("x,y,z")) {
				String[] coords = args.<String>getOne("x,y,z").get().split(",");

				if (coords[0].equalsIgnoreCase("random")) {
					local.setCoordinate(new Coordinate(world.get(), Preset.RANDOM));
				} else if(coords[0].equalsIgnoreCase("bed")) {
					local.setCoordinate(new Coordinate(world.get(), Preset.BED));
				} else if(coords[0].equalsIgnoreCase("last")) {
					local.setCoordinate(new Coordinate(world.get(), Preset.LAST_LOCATION));
				} else {
					try {
						local.setCoordinate(new Coordinate(world.get(), new Vector3d(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]))));
					} catch (Exception e) {
						throw new CommandException(Text.of(TextColors.RED, coords.toString(), " is not valid"), true);
					}
				}
			} else {
				local.setCoordinate(new Coordinate(world.get(), Preset.NONE));
			}

			if (args.hasAny("direction")) {
				local.setRotation(args.<Rotation>getOne("direction").get());
			}

			if (args.hasAny("f")) {
				local.setForce(true);
			}

			local.setProperties(properties);

			PortalListener.builders.put(player.getUniqueId(), new PortalBuilder(local));
		}
		
		player.sendMessage(Text.builder().color(TextColors.DARK_GREEN).append(Text.of("Begin building your portal frame, followed by ")).onClick(TextActions.runCommand("/pjp:portal save")).append(Text.of(TextColors.YELLOW, TextStyles.UNDERLINE, "/portal save")).build());
		return CommandResult.success();
	}

}
