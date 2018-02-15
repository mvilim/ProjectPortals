package com.gmail.trentech.pjp.commands.portal;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.listeners.PortalListener;
import com.gmail.trentech.pjp.portal.PortalBuilder;

public class CMDSave implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Help help = Help.get("portal save").get();
		
		if (args.hasAny("help")) {		
			help.execute(src);
			return CommandResult.empty();
		}
		
		if (!(src instanceof Player)) {
			throw new CommandException(Text.of(TextColors.RED, "Must be a player"), false);
		}
		Player player = (Player) src;

		if (!PortalListener.builders.containsKey(player.getUniqueId())) {
			throw new CommandException(Text.of(TextColors.DARK_GREEN, "Not in build mode"), false);
		}
		PortalBuilder builder = PortalListener.builders.get(player.getUniqueId());

		if (!builder.isFill()) {
			builder.setFill(true);
			player.sendMessage(Text.of(TextColors.DARK_GREEN, "Portal frame saved"));
			player.sendMessage(Text.builder().color(TextColors.DARK_GREEN).append(Text.of("Begin filling in portal frame, followed by ")).onClick(TextActions.runCommand("/pjp:portal save")).append(Text.of(TextColors.YELLOW, TextStyles.UNDERLINE, "/portal save")).build());
			return CommandResult.success();
		}

		if (builder.build(player)) {
			Sponge.getScheduler().createTaskBuilder().delayTicks(20).execute(t -> {
				PortalListener.builders.remove(player.getUniqueId());
			}).submit(Main.getPlugin());

			player.sendMessage(Text.of(TextColors.DARK_GREEN, "Portal ", builder.getPortal().getName(), " created successfully"));
		}

		return CommandResult.success();
	}
}
