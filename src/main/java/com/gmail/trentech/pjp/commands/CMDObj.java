package com.gmail.trentech.pjp.commands;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;

import com.gmail.trentech.pjp.listeners.ButtonListener;
import com.gmail.trentech.pjp.listeners.DoorListener;
import com.gmail.trentech.pjp.listeners.LeverListener;
import com.gmail.trentech.pjp.listeners.PlateListener;
import com.gmail.trentech.pjp.listeners.SignListener;
import com.gmail.trentech.pjp.portal.Portal;
import com.gmail.trentech.pjp.portal.Portal.Local;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.Portal.Server;
import com.gmail.trentech.pjp.portal.features.Command;
import com.gmail.trentech.pjp.portal.features.Coordinate;
import com.gmail.trentech.pjp.rotation.Rotation;

public class CMDObj {

	public static class Button extends CMDObjBase {

		public Button() {
			super("button");
		}

		@Override
		protected void init(Player player, String server, double price, Optional<String> permission, Optional<Command> command) {
			Server portal = new Portal.Server(name, PortalType.BUTTON, server);
			
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			ButtonListener.builders.put(player.getUniqueId(), portal);
		}

		@Override
		protected void init(Player player, Coordinate coordinate, Rotation rotation, double price, boolean force, Optional<String> permission, Optional<Command> command) {
			Local portal = new Portal.Local(name, PortalType.BUTTON);

			portal.setCoordinate(coordinate);
			portal.setRotation(rotation);
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			ButtonListener.builders.put(player.getUniqueId(), portal);
		}
	}

	public static class Door extends CMDObjBase {

		public Door() {
			super("door");
		}

		@Override
		protected void init(Player player, String server, double price, Optional<String> permission, Optional<Command> command) {
			Server portal = new Portal.Server(name, PortalType.DOOR, server);
			
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			DoorListener.builders.put(player.getUniqueId(), portal);
		}

		@Override
		protected void init(Player player, Coordinate coordinate, Rotation rotation, double price, boolean force, Optional<String> permission, Optional<Command> command) {
			Local portal = new Portal.Local(name, PortalType.DOOR);

			portal.setCoordinate(coordinate);
			portal.setRotation(rotation);
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			DoorListener.builders.put(player.getUniqueId(), portal);
		}
	}

	public static class Lever extends CMDObjBase {

		public Lever() {
			super("lever");
		}

		@Override
		protected void init(Player player, String server, double price, Optional<String> permission, Optional<Command> command) {
			Server portal = new Portal.Server(name, PortalType.LEVER, server);
			
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			LeverListener.builders.put(player.getUniqueId(), portal);
		}

		@Override
		protected void init(Player player, Coordinate coordinate, Rotation rotation, double price, boolean force, Optional<String> permission, Optional<Command> command) {
			Local portal = new Portal.Local(name, PortalType.LEVER);

			portal.setCoordinate(coordinate);
			portal.setRotation(rotation);
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			LeverListener.builders.put(player.getUniqueId(), portal);
		}
	}

	public static class Plate extends CMDObjBase {

		public Plate() {
			super("pressure plate");
		}

		@Override
		protected void init(Player player, String server, double price, Optional<String> permission, Optional<Command> command) {
			Server portal = new Portal.Server(name, PortalType.PLATE, server);
			
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			PlateListener.builders.put(player.getUniqueId(), portal);
		}

		@Override
		protected void init(Player player, Coordinate coordinate, Rotation rotation, double price, boolean force, Optional<String> permission, Optional<Command> command) {
			Local portal = new Portal.Local(name, PortalType.PLATE);

			portal.setCoordinate(coordinate);
			portal.setRotation(rotation);
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			PlateListener.builders.put(player.getUniqueId(), portal);
		}
	}

	public static class Sign extends CMDObjBase {

		public Sign() {
			super("sign");
		}

		@Override
		protected void init(Player player, String server, double price, Optional<String> permission, Optional<Command> command) {
			Server portal = new Portal.Server(name, PortalType.SIGN, server);
			
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			SignListener.builders.put(player.getUniqueId(), portal);
		}

		@Override
		protected void init(Player player, Coordinate coordinate, Rotation rotation, double price, boolean force, Optional<String> permission, Optional<Command> command) {
			Local portal = new Portal.Local(name, PortalType.SIGN);

			portal.setCoordinate(coordinate);
			portal.setRotation(rotation);
			portal.setPrice(price);
			
			if(permission.isPresent()) {
				portal.setPermission(permission.get());
			}
			
			if(command.isPresent()) {
				portal.setCommand(command.get());
			}
			
			SignListener.builders.put(player.getUniqueId(), portal);
		}
	}
}