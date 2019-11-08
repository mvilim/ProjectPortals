package com.gmail.trentech.pjp.init;

import java.util.Optional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjc.core.SQLManager;
import com.gmail.trentech.pjc.help.Argument;
import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjc.help.Usage;
import com.gmail.trentech.pjp.Main;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class Common {

	public static void init() {
		initConfig();
		initHelp();
		initData();
	}
	
	public static void initData() {
		try {
			SQLManager sqlManager = SQLManager.get(Main.getPlugin());
			Connection connection = sqlManager.getDataSource().getConnection();

			PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + sqlManager.getPrefix("PORTALS") + " (Name TEXT, Data MEDIUMBLOB)");
			statement.executeUpdate();

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void createObjHelp(String name, Optional<String> displayName, ConfigurationNode modules, Usage usagePortal)
	{
		if (modules.getNode(name + "s").getBoolean()) {
			Help obj = new Help(name, name, "Use this command to create a " + displayName.orElse(name) + " that will teleport you to other worlds")
					.setPermission("pjp.cmd." + name)
					.setUsage(usagePortal)
					.addExample("/" + name + " MyWorld -c random")
					.addExample("/" + name + " MyWorld -c -100,65,254 -d south")
					.addExample("/" + name + " MyWorld -d southeast")
					.addExample("/" + name + " MyWorld -c bed")
					.addExample("/" + name + " MyWorld");
			Help.register(obj);
		}
	}

	private static String portalOrWarp(boolean isPortal)
	{
		return isPortal ? "portal" : "warp";
	}

	private static Argument name(boolean isPortal)
	{
		return Argument.of("<name>", String.format("Specifies the name of the new %s", portalOrWarp(isPortal)));
	}

	private static Argument destination()
	{
		return Argument.of("<destination>", "Specifies a world");
	}

	private static Argument bungee()
	{
		return Argument.of("[-b server]", "Specifies the bungee connected <server> to which to teleport");
	}

	private static Argument force()
	{
		return Argument.of("[-f]", "Skips safe location check. Has no effect with '-c random' or '-c bed'");
	}

	private static Argument coordinates()
	{
		return Argument.of("[-c <x,y,z>]", "Specifies the coordinates to set spawn to. Other valid arguments are \"random\",\"bed\" and \"last\". x and z must fall within the range -30,000,000 to 30,000,000 "
						+ ", and y must be within the range -4096 to 4096 inclusive.");
	}

	private static Argument direction()
	{
		return Argument.of("[-d <direction>]", "Specifies the direction player will face upon teleporting. The following can be used: NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST, NORTH_EAST");
	}

	private static Argument price(boolean isPortal)
	{
		return Argument.of("[-p <price>]", String.format("Specifies a price player will be charged for using this %s", portalOrWarp(isPortal)));
	}

	private static Argument command(boolean isPortal)
	{
		return Argument.of("[-s <command>]", String.format("Specifies a command to execute when using %s", portalOrWarp(isPortal)));
	}

	private static Argument permission(boolean isPortal)
	{
		return Argument.of("[n <permission>]", String.format("Allow you to assign a custom permission node to a %s. If no permission is provided everyone will have access.", portalOrWarp(isPortal)));
	}

	public static void initHelp() {
		ConfigurationNode modules = ConfigManager.get(Main.getPlugin()).getConfig().getNode("settings", "modules");
		
		Usage usagePortal = new Usage(destination())
				.addArgument(bungee())
				.addArgument(force())
				.addArgument(coordinates())
				.addArgument(direction());

		createObjHelp("button", Optional.empty(), modules, usagePortal);
		createObjHelp("door", Optional.empty(), modules, usagePortal);
		createObjHelp("plate", Optional.of("pressure plate"), modules, usagePortal);
		createObjHelp("sign", Optional.empty(), modules, usagePortal);
		createObjHelp("lever", Optional.empty(), modules, usagePortal);

		if (modules.getNode("portals").getBoolean()) {
			Usage usageCreate = new Usage(name(true))
				.addArgument(destination())
				.addArgument(bungee())
				.addArgument(force())
				.addArgument(coordinates())
				.addArgument(direction())
				.addArgument(price(true))
				.addArgument(command(true))
				.addArgument(permission(true));

			Help portalCreate = new Help("portal create", "create", "Use this command to create a portal that will teleport you to other worlds")
					.setPermission("pjp.cmd.portal.create")
					.setUsage(usageCreate)
					.addExample("/portal create MyPortal MyWorld -c -100,65,254")
					.addExample("/portal create MyPortal MyWorld -c C:random")
					.addExample("/portal create MyPortal MyWorld -c -100,65,254 -d south")
					.addExample("/portal create MyPortal MyWorld -d southeast")
					.addExample("/portal create MyPortal MyWorld");
			
			Usage usageDestination = new Usage(Argument.of("<name>", "Specifies the name of the targeted portal"))
					.addArgument(destination())
					.addArgument(bungee())
					.addArgument(force())
					.addArgument(coordinates())
					.addArgument(direction())
					.addArgument(price(true))
					.addArgument(command(true))
					.addArgument(permission(true));
			
			Help portalDestination = new Help("portal destination", "destination", "Change as existing portals destination")
					.setPermission("pjp.cmd.portal.destination")
					.setUsage(usageDestination)
					.addExample("/portal destination MyPortal MyWorld -c -100,65,254")
					.addExample("/portal destination MyPortal MyWorld -c C:random")
					.addExample("/portal destination MyPortal MyWorld -c -100,65,254 -d south")
					.addExample("/portal destination MyPortal MyWorld -d southeast")
					.addExample("/portal destination MyPortal MyWorld");
			
			Usage usageDirection = new Usage(Argument.of("<name>", "Specifies the name of the targeted portal"))
					.addArgument(Argument.of("<direction>", "Specifies the direction player will face upon teleporting. The following can be used: NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST, NORTH_EAST"));
			
			Help portalDirection = new Help("portal direction", "direction", "Change as existing portals spawn direction")
					.setPermission("simplyportals.cmd.portal.direction")
					.setUsage(usageDirection)
					.addExample("/portal direction Skyland NORTH");
			
			Help portalList = new Help("portal list", "list", "List all portals")
					.setPermission("pjp.cmd.portal.list");
			
			Usage usageParticle = new Usage(Argument.of("<name>", "Specifies the name of the targeted portal"))
					.addArgument(Argument.of("<particleType>", "Specifies the ParticleType"))
					.addArgument(Argument.of("<intensity>", "Specifies the intensity of the particles spawn. The lower the value the more particles spawn. WARNING: too low could produce client lag"))
					.addArgument(Argument.of("[<particleOption>", "Specifies a compatible particle option. color, block state, direction etc.."))
					.addArgument(Argument.of("<value>]", "Specifies the particle option value"));
			
			Help portalParticle = new Help("portal particle", "particle", "Change a portals particle effect.")
					.setPermission("pjp.cmd.portal.particle")
					.setUsage(usageParticle)
					.addExample("/portal particle MyPortal minecraft:redstone_dust 40 minecraft:color BLUE");
			
			Usage usageBlock = new Usage(Argument.of("<name>", "Specifies the name of the targeted portal"))
					.addArgument(Argument.of("<blockType>[:damageValue]", "Specifies the BlockType. Add unsafe damage value for block varients, such as colored wool."));
			
			Help portalBlock = new Help("portal block", "block", "Change a portals center block type. Useful for non-solid block types such as water, lava and portal blocks. This can be combined with particles")
					.setPermission("pjp.cmd.portal.block")
					.setUsage(usageBlock)
					.addExample("/portal block MyPortal minecraft:stained_glass_pane:1")
					.addExample("/portal block MyPortal minecraft:portal");
			
			Usage usagePrice = new Usage(Argument.of("<name>", "Specifies the name of the targeted portal"))
					.addArgument(Argument.of("<price>", "Specifies a price player will be charged for using portal"));
			
			Help portalPrice = new Help("portal price", "price", "Charge players for using portals. 0 to disable")
					.setPermission("pjp.cmd.portal.price")
					.setUsage(usagePrice)
					.addExample("/portal price MyPortal 0")
					.addExample("/portal price MyPortal 50");	
			
			Usage usageCommand = new Usage(Argument.of("<name>", "Specifies the name of the targeted portal"))
					.addArgument(Argument.of("<command>", "Specifies the command that will execute when using a portal, beginning with 'C:' or 'P:' to specify if command will run as Player or Console"));
			
			Help portalCommand = new Help("portal command", "command", "Run a command when using a portal.")
					.setPermission("pjp.cmd.portal.command")
					.setUsage(usageCommand)
					.addExample("/portal command MyPortal P:kill all")
					.addExample("/portal command MyPortal C:give Notch minecraft:apple");
			
			Usage usagePermission = new Usage(Argument.of("<name>", "Specifies the name of the targeted portal"))
					.addArgument(Argument.of("<permission>", "Specifies the permission that is required to use portal"));
			
			Help portalPermission = new Help("portal permission", "permission", "Sets a permission node that is required to use portal.")
					.setPermission("pjp.cmd.portal.permission")
					.setUsage(usagePermission)
					.addExample("/portal permission MyPortal perm.node");
			
			Usage usageRemove = new Usage(Argument.of("<name>", "Specifies the name of the targeted portal"));
					
			Help portalRemove = new Help("portal remove", "remove", "Remove an existing portal")
					.setPermission("pjp.cmd.portal.remove")
					.setUsage(usageRemove)
					.addExample("/portal remove MyPortal");
			
			Usage usageRename = new Usage(Argument.of("<oldName>", "Specifies the name of the targeted portal"))
					.addArgument(Argument.of("<newName>", "Specifies the new name of the portal"));
			
			Help portalRename = new Help("portal rename", "rename", "Rename portal")
					.setPermission("pjp.cmd.portal.rename")
					.setUsage(usageRename)
					.addExample("/portal rename MyPortal ThisPortal");
				
			Help portalSave = new Help("portal save", "save", "Saves generated portal")
					.setPermission("pjp.cmd.portal.save");
			
			Help portal = new Help("portal", "portal", " Top level portal command")
					.setPermission("pjp.cmd.portal")
					.addChild(portalSave)
					.addChild(portalPermission)
					.addChild(portalCommand)
					.addChild(portalDirection)
					.addChild(portalRename)
					.addChild(portalBlock)
					.addChild(portalRemove)
					.addChild(portalPrice)
					.addChild(portalParticle)
					.addChild(portalList)
					.addChild(portalDestination)
					.addChild(portalCreate);
			
			Help.register(portal);
		}
		
		if (modules.getNode("homes").getBoolean()) {
			Usage usageCreate = new Usage(Argument.of("<name>", "Specifies the name of the new home"))
					.addArgument(Argument.of("[-f]", "Skips safe location check. Has no effect with '-c random' or '-c bed'"));
			
			Help homeCreate = new Help("home create", "create", "Create a new home")
					.setPermission("pjp.cmd.home.create")
					.setUsage(usageCreate)
					.addExample("/home create MyHome");
			
			Help homeList = new Help("home list", "list", "List all homes")
					.setPermission("pjp.cmd.home.list");
			
			Usage usageRemove = new Usage(Argument.of("<name>", "Specifies the name of the targeted home"));
			
			Help homeRemove = new Help("home remove", "remove", "Remove an existing home")
					.setPermission("pjp.cmd.home.remove")
					.setUsage(usageRemove)
					.addExample("/home remove OldHome");
			
			Usage usageRename = new Usage(Argument.of("<oldName>", "Specifies the name of the targeted home"))
					.addArgument(Argument.of("<newName>", "Specifies the new name of the home"));
			
			Help homeRename = new Help("home rename", "rename", "Rename home")
					.setPermission("pjp.cmd.home.rename")
					.setUsage(usageRename)
					.addExample("/home rename MyHome Castle");
			
			Help home = new Help("home", "home", " Top level home command")
					.setPermission("pjp.cmd.home")
					.addChild(homeRename)
					.addChild(homeRemove)
					.addChild(homeList)
					.addChild(homeCreate);
			
			Help.register(home);
		}
		if (modules.getNode("warps").getBoolean()) {
			Usage usageCreate = new Usage(name(false))
				.addArgument(destination())
				.addArgument(bungee())
				.addArgument(force())
				.addArgument(coordinates())
				.addArgument(direction())
				.addArgument(price(false))
				.addArgument(command(false))
				.addArgument(permission(false));
			
			Help warpCreate = new Help("warp create", "create", "Use this command to create a warp that will teleport you to other worlds")
					.setPermission("pjp.cmd.warp.create")
					.setUsage(usageCreate)
					.addExample("/warp create Lobby MyWorld")
					.addExample("/warp create Lobby MyWorld -c -100,65,254")
					.addExample("/warp create Random MyWorld -c random")
					.addExample("/warp create Lobby MyWorld -c -100,65,254 -d south")
					.addExample("/warp create Lobby MyWorld -d southeast")
					.addExample("/warp create Lobby");
			
			Help warpList = new Help("warp list", "list", "List all warp points")
					.setPermission("pjp.cmd.warp.list");
			
			Usage usagePrice = new Usage(Argument.of("<name>", "Specifies the name of the targeted warp point"))
					.addArgument(Argument.of("<price>", "Specifies a price player will be charged for using this warp"));
			
			Help warpPrice = new Help("warp price", "price", "Charge players for using warps. 0 to disable")
					.setPermission("pjp.cmd.warp.price")
					.setUsage(usagePrice)
					.addExample("/warp price Lobby 0")
					.addExample("/warp price Lobby 50");
			
			Usage usageRemove = new Usage(Argument.of("<name>", "Specifies the name of the targeted warp point"));
					
			Help warpRemove = new Help("warp remove", "remove", "Remove an existing  warp point")
					.setPermission("pjp.cmd.warp.remove")
					.setUsage(usageRemove)
					.addExample("/warp remove OldSpawn");
			
			Usage usageRename = new Usage(Argument.of("<oldName>", "Specifies the name of the targeted warp point"))
					.addArgument(Argument.of("<newName>", "Specifies the new name of the warp point"));
			
			Help warpRename = new Help("warp rename", "rename", "Rename warp")
					.setPermission("pjp.cmd.warp.rename")
					.setUsage(usageRename)
					.addExample("/warp rename Spawn Lobby");
			
			Help warp = new Help("warp", "warp", " Top level warp command")
					.setPermission("pjp.cmd.warp")
					.addChild(warpRename)
					.addChild(warpRemove)
					.addChild(warpPrice)
					.addChild(warpList)
					.addChild(warpCreate);
			
			Help.register(warp);
		}
	}
	
	public static void initConfig() {
		ConfigManager configManager = ConfigManager.init(Main.getPlugin());
		CommentedConfigurationNode config = configManager.getConfig();

		if (config.getNode("options", "portal", "size").isVirtual()) {
			config.getNode("options", "portal", "size").setValue(100).setComment("Maximum number of blocks a portal can use");
		}
		if (config.getNode("options", "portal", "teleport_item").isVirtual()) {
			config.getNode("options", "portal", "teleport_item").setValue(true).setComment("Toggle if portals can teleport items");
		}
		if (config.getNode("options", "portal", "teleport_mob").isVirtual()) {
			config.getNode("options", "portal", "teleport_mob").setValue(true).setComment("Toggle if portals can teleport mobs");
		}
		if (config.getNode("options", "homes").isVirtual()) {
			config.getNode("options", "homes").setValue(5).setComment("Default number of homes a player can have");
		}
		if (config.getNode("options", "particles").isVirtual()) {
			config.getNode("options", "particles").setComment("Particle effect settings");
			config.getNode("options", "particles", "enable").setValue(true).setComment("Enable particle effects");
			config.getNode("options", "particles", "portal", "type").setValue("minecraft:witch_spell").setComment("Default particle type for portals");
			config.getNode("options", "particles", "portal", "option", "type").setValue("none").setComment("Default ParticleOption type if supported, otherwise set \"NONE\"");
			config.getNode("options", "particles", "portal", "option", "value").setValue("none").setComment("Default ParticleOption value if supported, otherwise set \"NONE\"");
			config.getNode("options", "particles", "teleport", "type").setValue("minecraft:redstone_dust").setComment("Default particle type when teleporting");
			config.getNode("options", "particles", "teleport", "option", "type").setValue("minecraft:color").setComment("Default ParticleOption type if supported, otherwise set \"NONE\"");
			config.getNode("options", "particles", "teleport", "option", "value").setValue("blue").setComment("Default ParticleOption value if supported, otherwise set \"NONE\"");
			config.getNode("options", "particles", "creation", "type").setValue("minecraft:witch_spell").setComment("Default particle type when creating any kind of portal");
			config.getNode("options", "particles", "creation", "option", "type").setValue("none").setComment("Default ParticleOption type if supported, otherwise set \"NONE\"");
			config.getNode("options", "particles", "creation", "option", "value").setValue("none").setComment("Default ParticleOption value if supported, otherwise set \"NONE\"");
		}
		if (config.getNode("options", "random_spawn_radius").isVirtual()) {
			config.getNode("options", "random_spawn_radius").setValue(5000).setComment("World radius for random spawn portals.");
		}
		if (config.getNode("options", "teleport_message").isVirtual()) {
			config.getNode("options", "teleport_message").setComment("Set message that displays when player teleports.");
			// UPDATE CONFIG
			if (config.getNode("options", "teleport_message", "enable").isVirtual()) {
				config.getNode("options", "teleport_message", "enable").setValue(true);
			}
			config.getNode("options", "teleport_message", "title").setValue("&2%WORLD%");
			config.getNode("options", "teleport_message", "sub_title").setValue("&bx: %X%, y: %Y%, z: %Z%");
		}
		if (config.getNode("options", "teleport_message", "bungee_title").isVirtual()) {
			config.getNode("options", "teleport_message", "bungee_title").setValue("&2%SERVER% - %WORLD%");
		}
		if (config.getNode("settings", "modules").isVirtual()) {
			config.getNode("settings", "modules").setComment("Toggle on and off specific features");
		}
		if (config.getNode("settings", "modules", "portals").isVirtual()) {
			config.getNode("settings", "modules", "portals").setValue(true);
		}
		if (config.getNode("settings", "modules", "buttons").isVirtual()) {
			config.getNode("settings", "modules", "buttons").setValue(false);
		}
		if (config.getNode("settings", "modules", "doors").isVirtual()) {
			config.getNode("settings", "modules", "doors").setValue(false);
		}
		if (config.getNode("settings", "modules", "plates").isVirtual()) {
			config.getNode("settings", "modules", "plates").setValue(false);
		}
		if (config.getNode("settings", "modules", "levers").isVirtual()) {
			config.getNode("settings", "modules", "levers").setValue(false);
		}
		if (config.getNode("settings", "modules", "signs").isVirtual()) {
			config.getNode("settings", "modules", "signs").setValue(false);
		}
		if (config.getNode("settings", "modules", "warps").isVirtual()) {
			config.getNode("settings", "modules", "warps").setValue(false);
		}
		if (config.getNode("settings", "modules", "homes").isVirtual()) {
			config.getNode("settings", "modules", "homes").setValue(false);
		}
		if (config.getNode("settings", "modules", "back").isVirtual()) {
			config.getNode("settings", "modules", "back").setValue(true);
		}
		if (config.getNode("settings", "sql", "database").isVirtual()) {
			config.getNode("settings", "sql", "database").setValue(Main.getPlugin().getId());
		}
		
		configManager.save();
	}
}
