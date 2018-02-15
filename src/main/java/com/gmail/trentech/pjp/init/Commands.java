package com.gmail.trentech.pjp.init;

import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.gmail.trentech.pjp.commands.CMDObj;
import com.gmail.trentech.pjp.commands.elements.PortalElement;
import com.gmail.trentech.pjp.commands.home.CMDHome;
import com.gmail.trentech.pjp.commands.portal.CMDPortal;
import com.gmail.trentech.pjp.commands.portal.CMDSave;
import com.gmail.trentech.pjp.commands.portal.CommandParticle;
import com.gmail.trentech.pjp.commands.warp.CMDWarp;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.rotation.Rotation;

public class Commands {

	private CommandSpec cmdWarpCreate = CommandSpec.builder()
		    .description(Text.of("Create a new warp point"))
		    .permission("pjp.cmd.warp.create")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("name"))), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.flags().flag("b").flag("f")
		    		.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
		    		.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
    				.valueFlag(GenericArguments.doubleNum(Text.of("price")), "p")
    				.valueFlag(GenericArguments.string(Text.of("command")), "s")
    				.valueFlag(GenericArguments.string(Text.of("permission")), "n").buildWith(GenericArguments.none()))
		    .executor(new com.gmail.trentech.pjp.commands.warp.CMDCreate())
		    .build();
	
	private CommandSpec cmdWarpRemove = CommandSpec.builder()
		    .description(Text.of("Remove an existing warp point"))
		    .permission("pjp.cmd.warp.remove")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.WARP)))
		    .executor(new com.gmail.trentech.pjp.commands.warp.CMDRemove())
		    .build();
	
	private CommandSpec cmdWarpRename = CommandSpec.builder()
		    .description(Text.of("Rename an existing warp point"))
		    .permission("pjp.cmd.warp.rename")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.WARP)), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("newName"))))
		    .executor(new com.gmail.trentech.pjp.commands.warp.CMDRename())
		    .build();
	
	private CommandSpec cmdWarpPrice = CommandSpec.builder()
		    .description(Text.of("Set price of an existing warp point"))
		    .permission("pjp.cmd.warp.price")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.WARP)), 
		    		GenericArguments.optional(GenericArguments.doubleNum(Text.of("price"))))
		    .executor(new com.gmail.trentech.pjp.commands.warp.CMDPrice())
		    .build();
	
	private CommandSpec cmdWarpList = CommandSpec.builder()
		    .description(Text.of("List all available warp points"))
		    .permission("pjp.cmd.warp.list")
		    .executor(new com.gmail.trentech.pjp.commands.warp.CMDList())
		    .build();
	
	public CommandSpec cmdWarp = CommandSpec.builder()
		    .description(Text.of("Top level warp command"))
		    .permission("pjp.cmd.warp")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.WARP)), 
		    		GenericArguments.optional(GenericArguments.player(Text.of("player"))))
		    .child(cmdWarpCreate, "create", "c")
		    .child(cmdWarpRemove, "remove", "r")
		    .child(cmdWarpRename, "rename", "rn")
		    .child(cmdWarpList, "list", "ls")
		    .child(cmdWarpPrice, "price", "p")
		    .executor(new CMDWarp())
		    .build();
	
	
	private CommandSpec cmdHomeCreate = CommandSpec.builder()
		    .description(Text.of("Create a new home"))
		    .permission("pjp.cmd.home.create")
		    .arguments(
		    		GenericArguments.string(Text.of("name")), 
		    		GenericArguments.flags().flag("f").buildWith(GenericArguments.none()))
		    .executor(new com.gmail.trentech.pjp.commands.home.CMDCreate())
		    .build();
	
	private CommandSpec cmdHomeRemove = CommandSpec.builder()
		    .description(Text.of("Remove an existing home"))
		    .permission("pjp.cmd.home.remove")
		    .arguments(
		    		GenericArguments.string(Text.of("name")))
		    .executor(new com.gmail.trentech.pjp.commands.home.CMDRemove())
		    .build();
	
	private CommandSpec cmdHomeRename = CommandSpec.builder()
		    .description(Text.of("Rename an existing home"))
		    .permission("pjp.cmd.home.rename")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("oldName"))), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("newName"))))
		    .executor(new com.gmail.trentech.pjp.commands.home.CMDRename())
		    .build();
	
	private CommandSpec cmdHomeList = CommandSpec.builder()
		    .description(Text.of("List all homes"))
		    .permission("pjp.cmd.home.list")
		    .executor(new com.gmail.trentech.pjp.commands.home.CMDList())
		    .build();

	public CommandSpec cmdHome = CommandSpec.builder()
		    .description(Text.of("Top level home command"))
		    .permission("pjp.cmd.home")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("name"))), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("player"))))
		    .child(cmdHomeCreate, "create", "c")
		    .child(cmdHomeRemove, "remove", "r")
		    .child(cmdHomeRename, "rename", "rn")
		    .child(cmdHomeList, "list", "ls")
		    .executor(new CMDHome())
		    .build();

	private CommandSpec cmdPortalCreate = CommandSpec.builder()
		    .description(Text.of("Create a new portal"))
		    .permission("pjp.cmd.portal.create")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("name"))), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.flags().flag("b").flag("f")
		    		.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
		    		.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
//		    		.valueFlag(GenericArguments.seq(GenericArguments.enumValue(Text.of("particle"), Particles.class), GenericArguments.optional(GenericArguments.enumValue(Text.of("color"), ParticleColor.class))), "e")
//    				.valueFlag(GenericArguments.string(Text.of("price")), "p")
//    				.valueFlag(GenericArguments.string(Text.of("command")), "s")
//    				.valueFlag(GenericArguments.string(Text.of("permission")), "n")
		    		.buildWith(GenericArguments.none()))
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDCreate())
		    .build();

	private CommandSpec cmdPortalDestination = CommandSpec.builder()
		    .description(Text.of("Change a existing portals destination"))
		    .permission("pjp.cmd.portal.destination")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.PORTAL)), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("x,y,z"))))
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDDestination())
		    .build();
	
	private CommandSpec cmdPortalRemove = CommandSpec.builder()
		    .description(Text.of("Remove an existing portal"))
		    .permission("pjp.cmd.portal.remove")
		    .arguments(
		    		new PortalElement(Text.of("name"), PortalType.PORTAL))
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDRemove())
		    .build();
	
	private CommandSpec cmdPortalRename = CommandSpec.builder()
		    .description(Text.of("Rename an existing portal"))
		    .permission("pjp.cmd.portal.rename")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.PORTAL)), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("newName"))))
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDRename())
		    .build();
	
//	private CommandSpec cmdPortalParticle = CommandSpec.builder()
//		    .description(Text.of("Set particle effects of an existing portal"))
//		    .permission("pjp.cmd.portal.particle")
//		    .arguments(
//		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.PORTAL)), 
//		    		GenericArguments.optional(GenericArguments.catalogedElement(Text.of("particleType"), ParticleType.class)),
//		    		GenericArguments.optional(GenericArguments.catalogedElement(Text.of("intensity"), ParticleType.class)),
//		    		GenericArguments.optional(GenericArguments.catalogedElement(Text.of("optionType"), ParticleOption.class)),
//		    		GenericArguments.optional(new ParticleOptionValueElement(Text.of("option"))))
//		    .executor(new CommandParticle())
//		    .build();
	
	private CommandSpec cmdPortalPrice = CommandSpec.builder()
		    .description(Text.of("Set price of an existing portal"))
		    .permission("pjp.cmd.portal.price")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.PORTAL)), 
		    		GenericArguments.optional(GenericArguments.doubleNum(Text.of("price"))))
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDPrice())
		    .build();
	
	private CommandSpec cmdPortalPermission = CommandSpec.builder()
		    .description(Text.of("Set permission of an existing portal"))
		    .permission("pjp.cmd.portal.permission")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.PORTAL)), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("permission"))))
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDPrice())
		    .build();
	
	private CommandSpec cmdPortalCommand = CommandSpec.builder()
		    .description(Text.of("Set command of an existing portal"))
		    .permission("pjp.cmd.portal.command")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.PORTAL)), 
		    		GenericArguments.optional(GenericArguments.string(Text.of("command"))))
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDPrice())
		    .build();
	
	private CommandSpec cmdPortalList = CommandSpec.builder()
		    .description(Text.of("List all portals"))
		    .permission("pjp.cmd.portal.list")
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDList())
		    .build();

	private CommandSpec cmdSave = CommandSpec.builder()
		    .description(Text.of("Saves generated portal"))
		    .permission("pjp.cmd.portal.create")
		    .executor(new CMDSave())
		    .build();
	
	public CommandSpec cmdPortal = CommandSpec.builder()
		    .description(Text.of("Top level portal command"))
		    .permission("pjp.cmd.portal")
		    .child(cmdPortalCreate, "create", "c")
		    .child(cmdPortalCommand, "command", "cmd")
		    .child(cmdPortalDestination, "destination", "d")
		    .child(cmdPortalRemove, "remove", "r")
		    .child(cmdPortalRename, "rename", "rn")
		    .child(new CommandParticle(), "particle", "p")
		    .child(cmdPortalPrice, "price", "pr")
		    .child(cmdPortalPermission, "permission", "perm")
		    .child(cmdPortalList, "list", "ls")
		    .child(cmdSave, "save", "s")
		    .executor(new CMDPortal())
		    .build();
	
	
	public CommandSpec cmdButton = CommandSpec.builder()
		    .description(Text.of("Create a new button portal"))
		    .permission("pjp.cmd.button")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.flags().flag("b").flag("f")
		    		.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
		    		.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
		    		.valueFlag(GenericArguments.doubleNum(Text.of("price")), "p")
		    		.valueFlag(GenericArguments.string(Text.of("command")), "s")
		    		.valueFlag(GenericArguments.string(Text.of("permission")), "n").buildWith(GenericArguments.none()))
		    .executor(new CMDObj.Button())
		    .build();

	public CommandSpec cmdDoor = CommandSpec.builder()
		    .description(Text.of("Create a new door portal"))
		    .permission("pjp.cmd.door")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.flags().flag("b").flag("f")
		    		.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
		    		.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
		    		.valueFlag(GenericArguments.doubleNum(Text.of("price")), "p")
		    		.valueFlag(GenericArguments.string(Text.of("command")), "s")
		    		.valueFlag(GenericArguments.string(Text.of("permission")), "n").buildWith(GenericArguments.none()))
		    .executor(new CMDObj.Door())
		    .build();
	
	public CommandSpec cmdLever = CommandSpec.builder()
		    .description(Text.of("Create a new lever portal"))
		    .permission("pjp.cmd.lever")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.flags().flag("b").flag("f")
		    		.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
		    		.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
		    		.valueFlag(GenericArguments.doubleNum(Text.of("price")), "p")
		    		.valueFlag(GenericArguments.string(Text.of("command")), "s")
		    		.valueFlag(GenericArguments.string(Text.of("permission")), "n").buildWith(GenericArguments.none()))
		    .executor(new CMDObj.Lever())
		    .build();
	
	public CommandSpec cmdPlate = CommandSpec.builder()
		    .description(Text.of("Create a new pressure plate portal"))
		    .permission("pjp.cmd.plate")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.flags().flag("b").flag("f")
		    		.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
		    		.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
		    		.valueFlag(GenericArguments.doubleNum(Text.of("price")), "p")
		    		.valueFlag(GenericArguments.string(Text.of("command")), "s")
		    		.valueFlag(GenericArguments.string(Text.of("permission")), "n").buildWith(GenericArguments.none()))
		    .executor(new CMDObj.Plate())
		    .build();

	public CommandSpec cmdSign = CommandSpec.builder()
		    .description(Text.of("Create a new sign portal"))
		    .permission("pjp.cmd.sign")
		    .arguments(
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.flags().flag("b").flag("f")
		    		.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
		    		.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
		    		.valueFlag(GenericArguments.string(Text.of("price")), "p")
		    		.valueFlag(GenericArguments.string(Text.of("command")), "s")
		    		.valueFlag(GenericArguments.string(Text.of("permission")), "n").buildWith(GenericArguments.none()))
		    .executor(new CMDObj.Sign())
		    .build();
}
