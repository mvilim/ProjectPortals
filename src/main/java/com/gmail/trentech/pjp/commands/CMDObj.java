package com.gmail.trentech.pjp.commands;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;

public class CMDObj implements CMDCreateBase {
	String name;
	PortalType type;
	Map<UUID, Portal> builders;

	public CMDObj(String name, PortalType type, Map<UUID, Portal> builders) {
		this.name = name;
		this.type = type;
		this.builders = builders;
	}

	@Override
	public String getName(CommandContext args) throws CommandException
	{
		return name;
	}

	@Override
	public PortalType getType() {
		return type;
	}

	@Override
	public Help getHelp()
	{
		return Help.get(name).get();
	}

	@Override
	public void complete(Player player, Portal portal, String name)
	{
		builders.put(player.getUniqueId(), portal);
		player.sendMessage(Text.of(TextColors.DARK_GREEN, "Place " + name + " to create " + name + " portal"));
	}
}
