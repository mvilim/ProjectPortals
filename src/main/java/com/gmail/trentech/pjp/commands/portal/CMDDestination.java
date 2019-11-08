package com.gmail.trentech.pjp.commands.portal;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.commands.CMDCreateBase;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalService;

public class CMDDestination implements CMDCreateBase {

	@Override
	public Help getHelp() {
		return Help.get("portal destination").get();
	}

	@Override
	public PortalType getType() {
		return PortalType.PORTAL;
	}

	@Override
	public void checkNameExistence(String name) throws CommandException
	{
		if (!getPortal(name).isPresent())
		{
			throw new CommandException(Text.of(TextColors.RED, name + " portal does not exist"), false);
		}
	}

	@Override
	public void complete(Player player, Portal portal, String name) {
		getPortal(name).get().getProperties().ifPresent(portal::setProperties);

		Sponge.getServiceManager().provideUnchecked(PortalService.class).update(portal);

		player.sendMessage(Text.of(TextColors.DARK_GREEN, "Changed portal destination"));
	}
}
