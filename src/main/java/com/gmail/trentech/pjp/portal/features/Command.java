package com.gmail.trentech.pjp.portal.features;

import static org.spongepowered.api.data.DataQuery.of;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.Player;

public class Command implements DataSerializable {

	private static final DataQuery SRCTYPE = of("src");
	private static final DataQuery COMMAND = of("command");
	
	private String command;
	private SourceType srcType;
	
	public Command(SourceType srcType, String command) {
		this.srcType = srcType;
		this.command = command;
	}
	
	public String getCommand() {
		return command;
	}

	public SourceType getSrcType() {
		return srcType;
	}

	public void execute() {
		Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), command);
	}
	
	public void execute(Player player) {
		Sponge.getGame().getCommandManager().process(player, command);
	}
	
	public enum SourceType {
		PLAYER,CONSOLE
	}

	@Override
	public int getContentVersion() {
		return 0;
	}

	@Override
	public DataContainer toContainer() {
		return DataContainer.createNew().set(SRCTYPE, srcType.name()).set(COMMAND, command);
	}
	
	public static class Builder extends AbstractDataBuilder<Command> {

		public Builder() {
			super(Command.class, 0);
		}

		@Override
		protected Optional<Command> buildContent(DataView container) throws InvalidDataException {
			if (container.contains(SRCTYPE, COMMAND)) {
				SourceType srcType = SourceType.valueOf(container.getString(SRCTYPE).get());		
				String command = container.getString(COMMAND).get();

				return Optional.of(new Command(srcType, command));
			}

			return Optional.empty();
		}
	}
}
