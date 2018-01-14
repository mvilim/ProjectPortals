package com.gmail.trentech.pjp.commands.portal;

import java.util.Optional;

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
import com.gmail.trentech.pjp.effects.Particle;
import com.gmail.trentech.pjp.effects.ParticleColor;
import com.gmail.trentech.pjp.effects.Particles;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.features.Properties;

public class CMDParticle implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Help help = Help.get("portal particle").get();
		
		if (args.hasAny("help")) {		
			help.execute(src);
			return CommandResult.empty();
		}
		
		if (!args.hasAny("name")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		Portal portal = args.<Portal>getOne("name").get();

		if (!args.hasAny("particle")) {
			throw new CommandException(Text.builder().onClick(TextActions.executeCallback(help.execute())).append(help.getUsageText()).build(), false);
		}
		Particle particle = args.<Particles>getOne("particle").get().getParticle();

		Optional<ParticleColor> color = Optional.empty();

		if (args.hasAny("color")) {
			if (particle.isColorable()) {
				color = Optional.of(args.<ParticleColor>getOne("color").get());
			} else {
				src.sendMessage(Text.of(TextColors.YELLOW, "Colors currently only works with REDSTONE and MOB_SPELL"));
			}
		}

		Properties properties = portal.getProperties().get();
		properties.setParticle(particle);
		properties.setParticleColor(color);

		portal.setProperties(properties);
		Sponge.getServiceManager().provide(PortalService.class).get().update(portal);

		return CommandResult.success();
	}

}
