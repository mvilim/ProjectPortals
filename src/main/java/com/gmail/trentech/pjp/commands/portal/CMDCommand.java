package com.gmail.trentech.pjp.commands.portal;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.gmail.trentech.pjc.help.Help;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.features.Command;
import com.gmail.trentech.pjp.portal.features.Command.SourceType;

public class CMDCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Help help = Help.get("portal command").get();
		
		if (args.hasAny("help")) {		
			help.execute(src);
			return CommandResult.empty();
		}
		
		if (!args.hasAny("name")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		Portal portal = args.<Portal>getOne("name").get();

		if (!args.hasAny("command")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		
		String rawCommand = args.<String>getOne("command").get();
		String source = rawCommand.substring(0, 2);
		
		if(rawCommand.length() < 2) {
			throw new CommandException(Text.of(TextColors.RED, "Did not specify command source. P: for player or C: for console. Example \"P:say hello world\""), false);
		}
		
		if(source.equalsIgnoreCase("P:")) {
			portal.setCommand(new Command(SourceType.PLAYER, rawCommand.substring(2)));
		} else if(source.equalsIgnoreCase("C:")) {
			portal.setCommand(new Command(SourceType.CONSOLE, rawCommand.substring(2)));
		} else {
			throw new CommandException(Text.of(TextColors.RED, "Did not specify command source. P: for player or C: for console. Example \"P:say hello world\""), false);
		}

		Sponge.getServiceManager().provide(PortalService.class).get().update(portal);

		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Set command of portal ", portal.getName()));

		return CommandResult.success();
	}
}
