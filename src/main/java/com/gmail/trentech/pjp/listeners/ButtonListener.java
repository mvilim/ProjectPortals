package com.gmail.trentech.pjp.listeners;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.effects.PortalEffect;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.utils.Timings;

public class ButtonListener {

	public static ConcurrentHashMap<UUID, Portal> builders = new ConcurrentHashMap<>();

	private Timings timings;

	public ButtonListener(Timings timings) {
		this.timings = timings;
	}

	@Listener
	public void onChangeBlockEventModify(ChangeBlockEvent.Modify event, @Root Player player) {
		timings.onChangeBlockEventModify().startTiming();

		try {
			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				BlockSnapshot snapshot = transaction.getFinal();
				BlockState block = snapshot.getExtendedState();
				BlockType blockType = block.getType();

				if (!blockType.equals(BlockTypes.STONE_BUTTON) && !blockType.equals(BlockTypes.WOODEN_BUTTON)) {
					continue;
				}

				if (!block.get(Keys.POWERED).isPresent()) {
					continue;
				}

				if (!block.get(Keys.POWERED).get()) {
					continue;
				}

				Location<World> location = snapshot.getLocation().get();

				PortalService portalService = Sponge.getServiceManager().provide(PortalService.class).get();
				
				Optional<Portal> optionalPortal = portalService.get(location, PortalType.BUTTON);

				if (!optionalPortal.isPresent()) {
					continue;
				}
				Portal portal = optionalPortal.get();

				portalService.execute(player, portal);
			}
		} finally {
			timings.onChangeBlockEventModify().stopTiming();
		}
	}

	@Listener
	public void onChangeBlockEventBreak(ChangeBlockEvent.Break event, @Root Player player) {
		timings.onChangeBlockEventBreak().startTiming();

		try {
			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				Location<World> location = transaction.getFinal().getLocation().get();

				PortalService portalService = Sponge.getServiceManager().provide(PortalService.class).get();
				Optional<Portal> optionalPortal = portalService.get(location, PortalType.BUTTON);

				if (!optionalPortal.isPresent()) {
					continue;
				}
				Portal portal = optionalPortal.get();

				if (!player.hasPermission("pjp.button.break")) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to break button portals"));
					event.setCancelled(true);
				} else {
					portalService.remove(portal);
					player.sendMessage(Text.of(TextColors.DARK_GREEN, "Broke button portal"));
				}
			}
		} finally {
			timings.onChangeBlockEventBreak().stopTiming();
		}
	}

	@Listener
	public void onChangeBlockEventPlace(ChangeBlockEvent.Place event, @Root Player player) {
		timings.onChangeBlockEventPlace().startTiming();

		try {
			if (!builders.containsKey(player.getUniqueId())) {
				return;
			}

			for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				BlockType blockType = transaction.getFinal().getState().getType();

				if (!blockType.equals(BlockTypes.STONE_BUTTON) && !blockType.equals(BlockTypes.WOODEN_BUTTON)) {
					continue;
				}

				Location<World> location = transaction.getFinal().getLocation().get();

				if (!player.hasPermission("pjp.button.place")) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "you do not have permission to place button portals"));
					builders.remove(player.getUniqueId());
					event.setCancelled(true);
					return;
				}

				Portal portal = builders.get(player.getUniqueId());
				
				Sponge.getServiceManager().provide(PortalService.class).get().create(portal, location);
				PortalEffect.create(location);
				
				player.sendMessage(Text.of(TextColors.DARK_GREEN, "New button portal created"));

				builders.remove(player.getUniqueId());
			}
		} finally {
			timings.onChangeBlockEventPlace().stopTiming();
		}
	}
}
