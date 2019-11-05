package com.gmail.trentech.pjp.events;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class TeleportEvent extends AbstractEvent implements Cancellable {

	private final Player player;
	private final Cause cause;
	private double price;
	private Optional<String> permission;
	private boolean cancelled = false;

	public TeleportEvent(Player player, Cause cause, double price, Optional<String> permission) {
		this.player = player;
		this.cause = cause;
		this.price = price;
		this.permission = permission;
	}

	public Player getPlayer() {
		return player;
	}

	public double getPrice() {
		return price;
	}

	public Optional<String> getPermission() {
		return permission;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public Cause getCause() {
		return cause;
	}

	public static class Local extends TeleportEvent {

		private final Location<World> source;
		private Location<World> destination;
		private Optional<String> server;
		private boolean force;
		
		public Local(Player player, Location<World> source, Location<World> destination, double price, boolean force, Optional<String> permission, Optional<String> server, Cause cause) {
			super(player, cause, price, permission);
			
			this.source = source;
			this.setDestination(destination);
			this.server = server;
			this.setForce(force);
		}

		public Location<World> getDestination() {
			return destination;
		}

		public void setDestination(Location<World> destination) {
			this.destination = destination;
		}

		public Location<World> getSource() {
			return source;
		}

		public boolean force() {
			return force;
		}

		public void setForce(boolean force) {
			this.force = force;
		}

		public Optional<String> server() {
			return server;
		}
	}

	public static class Server extends TeleportEvent {

		private final String source;
		private String destination;

		public Server(Player player, String source, String destination, double price, Optional<String> permission, Cause cause) {
			super(player, cause, price, permission);

			this.source = source;
			this.destination = destination;
		}

		public String getDestination() {
			return destination;
		}

		public void setDestination(String destination) {
			this.destination = destination;
		}

		public String getSource() {
			return source;
		}

	}

}
