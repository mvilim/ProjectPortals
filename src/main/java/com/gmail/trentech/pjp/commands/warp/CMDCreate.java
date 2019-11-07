package com.gmail.trentech.pjp.commands.warp;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.commands.CMDCreateBase;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalService;

public class CMDCreate implements CMDCreateBase {

	@Override
	public Help getHelp()
	{
		return Help.get("warp create").get();
	}

	@Override
	public PortalType getType()
	{
		return PortalType.WARP;
	}

	@Override
	public void complete(Player player, Portal portal, String name)
	{
		Sponge.getServiceManager().provide(PortalService.class).get().create(portal);
		player.sendMessage(Text.of(TextColors.DARK_GREEN, "Warp ", name, " create"));
	}
}
