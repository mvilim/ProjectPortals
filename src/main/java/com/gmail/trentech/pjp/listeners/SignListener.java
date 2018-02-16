package com.gmail.trentech.pjp.listeners;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.pjp.data.Keys;
import com.gmail.trentech.pjp.data.mutable.SignPortalData;
import com.gmail.trentech.pjp.effects.PortalEffect;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.PortalService;
import com.gmail.trentech.pjp.utils.Timings;

public class SignListener {

	public static ConcurrentHashMap<UUID, Portal> builders = new ConcurrentHashMap<>();

	private Timings timings;

	public SignListener(Timings timings) {
		this.timings = timings;
	}

	@Listener
	public void onChangeSignEvent(ChangeSignEvent event, @Root Player player) {
		timings.onChangeSignEvent().startTiming();

		try {
			if (!builders.containsKey(player.getUniqueId())) {
				return;
			}
			Portal portal = builders.get(player.getUniqueId());

			if (!player.hasPermission("pjp.sign.place")) {
				player.sendMessage(Text.of(TextColors.DARK_RED, "You do not have permission to place sign portals"));
				event.setCancelled(true);
				return;
			}

			event.getTargetTile().offer(new SignPortalData(portal));
			PortalEffect.create(event.getTargetTile().getLocation());
			
			player.sendMessage(Text.of(TextColors.DARK_GREEN, "New sign portal created"));

			builders.remove(player.getUniqueId());
		} finally {
			timings.onChangeSignEvent().stopTiming();
		}
	}

	@Listener
	public void onInteractBlockEventSecondary(InteractBlockEvent.Secondary event, @Root Player player) {
		timings.onChangeSignEvent().startTiming();

		try {
			BlockSnapshot snapshot = event.getTargetBlock();
			if (!(snapshot.getState().getType().equals(BlockTypes.WALL_SIGN) || snapshot.getState().getType().equals(BlockTypes.STANDING_SIGN))) {
				return;
			}

			Location<World> location = snapshot.getLocation().get();

			Optional<Portal> optionalPortal = location.get(Keys.PORTAL);

			if (!optionalPortal.isPresent()) {
				return;
			}
			Portal portal = optionalPortal.get();

			Sponge.getServiceManager().provide(PortalService.class).get().execute(player, portal);
		} finally {
			timings.onChangeSignEvent().stopTiming();
		}
	}

	@Listener
	public void onChangeBlockEventBreak(ChangeBlockEvent.Break event, @Root Player player) {
		timings.onChangeBlockEventBreak().startTiming();

		try {
			for (Transaction<BlockSnapshot> blockTransaction : event.getTransactions()) {
				BlockSnapshot snapshot = blockTransaction.getOriginal();

				BlockType blockType = snapshot.getState().getType();

				if (!blockType.equals(BlockTypes.WALL_SIGN) && !blockType.equals(BlockTypes.STANDING_SIGN)) {
					continue;
				}

				Optional<Portal> optionalPortal = snapshot.get(Keys.PORTAL);

				if (!optionalPortal.isPresent()) {
					continue;
				}

				if (!player.hasPermission("pjp.sign.break")) {
					player.sendMessage(Text.of(TextColors.DARK_RED, "You do not have permission to break sign portal"));
					event.setCancelled(true);
				} else {
					player.sendMessage(Text.of(TextColors.DARK_GREEN, "Broke sign portal"));
				}
			}
		} finally {
			timings.onChangeBlockEventBreak().stopTiming();
		}
	}
}
