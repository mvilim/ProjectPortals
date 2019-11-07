package com.gmail.trentech.pjp.commands.portal;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.commands.CMDCreateBase;
import com.gmail.trentech.pjp.listeners.PortalListener;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.PortalBuilder;
import com.gmail.trentech.pjp.portal.features.Properties;

public class CMDCreate implements CMDCreateBase {

	@Override
	public Help getHelp()
	{
		return Help.get("portal create").get();
	}

	@Override
	public PortalType getType()
	{
		return PortalType.PORTAL;
	}

	@Override
	public void complete(Player player, Portal portal, String name)
	{
		portal.setProperties(new Properties());
		PortalListener.builders.put(player.getUniqueId(), new PortalBuilder(portal));
		player.sendMessage(Text.builder().color(TextColors.DARK_GREEN).append(Text.of("Begin building your portal frame, followed by ")).onClick(TextActions.runCommand("/pjp:portal save")).append(Text.of(TextColors.YELLOW, TextStyles.UNDERLINE, "/portal save")).build());
	}
}
