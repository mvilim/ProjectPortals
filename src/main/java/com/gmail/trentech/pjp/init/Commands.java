package com.gmail.trentech.pjp.init;

import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.ArrayList;

import com.gmail.trentech.pjp.commands.CMDObj;
import com.gmail.trentech.pjp.commands.elements.PortalElement;
import com.gmail.trentech.pjp.commands.home.CMDHome;
import com.gmail.trentech.pjp.commands.portal.CMDPortal;
import com.gmail.trentech.pjp.commands.portal.CMDSave;
import com.gmail.trentech.pjp.commands.portal.CommandBlock;
import com.gmail.trentech.pjp.commands.portal.CommandDirection;
import com.gmail.trentech.pjp.commands.portal.CommandParticle;
import com.gmail.trentech.pjp.commands.warp.CMDWarp;
import com.gmail.trentech.pjp.listeners.ButtonListener;
import com.gmail.trentech.pjp.listeners.DoorListener;
import com.gmail.trentech.pjp.listeners.LeverListener;
import com.gmail.trentech.pjp.listeners.PlateListener;
import com.gmail.trentech.pjp.listeners.SignListener;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.rotation.Rotation;

public class Commands {
	private static CommandElement[] createArgs(boolean named)
	{
		List<CommandElement> elements = new ArrayList<>();
		if (named)
		{
			elements.add(GenericArguments.string(Text.of("name")));
		}
		elements.add(GenericArguments.string(Text.of("destination")));
		elements.add(GenericArguments.flags().flag("b").flag("f")
			.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
			.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
			.valueFlag(GenericArguments.doubleNum(Text.of("price")), "p")
			.valueFlag(GenericArguments.string(Text.of("command")), "s")
			.valueFlag(GenericArguments.string(Text.of("permission")), "n")
			.buildWith(GenericArguments.none()));

		return elements.toArray(new CommandElement[elements.size()]);
	}

	private CommandSpec cmdWarpCreate = CommandSpec.builder()
		    .description(Text.of("Create a new warp point"))
		    .permission("pjp.cmd.warp.create")
			.arguments(createArgs(true))
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
			.arguments(createArgs(true))
		    .executor(new com.gmail.trentech.pjp.commands.portal.CMDCreate())
		    .build();

	private CommandSpec cmdPortalDestination = CommandSpec.builder()
		    .description(Text.of("Change a existing portals destination"))
		    .permission("pjp.cmd.portal.destination")
		    .arguments(
		    		GenericArguments.optional(new PortalElement(Text.of("name"), PortalType.PORTAL)),
		    		GenericArguments.optional(GenericArguments.string(Text.of("destination"))), 
		    		GenericArguments.flags().flag("b").flag("f")
		    		.valueFlag(GenericArguments.string(Text.of("x,y,z")), "c")
		    		.valueFlag(GenericArguments.enumValue(Text.of("direction"), Rotation.class), "d")
		    		.buildWith(GenericArguments.none()))
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
		    		GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("command"))))
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
		    .child(new CommandBlock(), "block", "b")
		    .child(new CommandDirection(), "direction", "dir")
		    .child(cmdPortalPrice, "price", "pr")
		    .child(cmdPortalPermission, "permission", "perm")
		    .child(cmdPortalList, "list", "ls")
		    .child(cmdSave, "save", "s")
		    .executor(new CMDPortal())
		    .build();
	
	
	public CommandSpec cmdButton = CommandSpec.builder()
		    .description(Text.of("Create a new button portal"))
		    .permission("pjp.cmd.button")
			.arguments(createArgs(false))
			.executor(new CMDObj("button", PortalType.BUTTON, ButtonListener.builders))
		    .build();

	public CommandSpec cmdDoor = CommandSpec.builder()
		    .description(Text.of("Create a new door portal"))
		    .permission("pjp.cmd.door")
			.arguments(createArgs(false))
			.executor(new CMDObj("door", PortalType.DOOR, DoorListener.builders))
		    .build();
	
	public CommandSpec cmdLever = CommandSpec.builder()
		    .description(Text.of("Create a new lever portal"))
		    .permission("pjp.cmd.lever")
			.arguments(createArgs(false))
			.executor(new CMDObj("lever", PortalType.LEVER, LeverListener.builders))
		    .build();
	
	public CommandSpec cmdPlate = CommandSpec.builder()
		    .description(Text.of("Create a new pressure plate portal"))
		    .permission("pjp.cmd.plate")
			.arguments(createArgs(false))
			.executor(new CMDObj("pressure plate", PortalType.PLATE, PlateListener.builders))
		    .build();

	public CommandSpec cmdSign = CommandSpec.builder()
		    .description(Text.of("Create a new sign portal"))
		    .permission("pjp.cmd.sign")
			.arguments(createArgs(false))
			.executor(new CMDObj("sign", PortalType.SIGN, SignListener.builders))
		    .build();
}
