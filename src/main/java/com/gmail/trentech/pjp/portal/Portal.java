package com.gmail.trentech.pjp.portal;

import static org.spongepowered.api.data.DataQuery.of;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.InvalidDataFormatException;

import com.gmail.trentech.pjp.portal.features.Command;
import com.gmail.trentech.pjp.portal.features.Coordinate;
import com.gmail.trentech.pjp.portal.features.Properties;
import com.gmail.trentech.pjp.rotation.Rotation;

public abstract class Portal implements DataSerializable {

	private static final DataQuery NAME = of("name");
	private static final DataQuery FORCE = of("force");
	private static final DataQuery PROPERTIES = of("properties");
	private static final DataQuery PORTAL_TYPE = of("type");
	private static final DataQuery SERVER = of("server");
	private static final DataQuery COORDINATE = of("coordinate");
	private static final DataQuery ROTATION = of("rotation");
	private static final DataQuery PRICE = of("price");
	private static final DataQuery PERMISSION = of("permission");
	private static final DataQuery COMMAND = of("command");
	
	private final PortalType type;
	private String name;
	private double price = 0;
	private Optional<String> permission = Optional.empty();	
	private Optional<Properties> properties = Optional.empty();
	private Optional<Command> command = Optional.empty();
	
	protected Portal(String name, PortalType type) {
		this.name = name;
		this.type = type;
	}

	public PortalType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Optional<Command> getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = Optional.of(command);
	}
	
	public Optional<String> getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		if(permission.equalsIgnoreCase("none")) {
			this.permission = Optional.empty();
		}
		
		this.permission = Optional.of(permission);
	}

	public Optional<Properties> getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = Optional.of(properties);
	}

	public static class Server extends Portal {

		private String server;

		public Server(String name, PortalType type, String server) {
			super(name, type);
			
			this.server = server;
		}

		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		@Override
		public int getContentVersion() {
			return 0;
		}

		@Override
		public DataContainer toContainer() {
			DataContainer container = DataContainer.createNew().set(NAME, getName()).set(PORTAL_TYPE, getType().name()).set(SERVER, getServer()).set(PRICE, getPrice());

			if (getPermission().isPresent()) {
				container.set(PERMISSION, getPermission().get());
			}
			
			if (getCommand().isPresent()) {
				container.set(COMMAND, getCommand().get());
			}
			
			if (getProperties().isPresent()) {
				container.set(PROPERTIES, getProperties().get());
			}

			return container;
		}

		public static class Builder extends AbstractDataBuilder<Server> {

			public Builder() {
				super(Server.class, 0);
			}

			@Override
			protected Optional<Server> buildContent(DataView container) throws InvalidDataException {
				if (container.contains(NAME, PORTAL_TYPE, SERVER, PRICE)) {
					String name = container.getString(NAME).get();
					PortalType type = PortalType.valueOf(container.getString(PORTAL_TYPE).get());
					String server = container.getString(SERVER).get();
					Double price = container.getDouble(PRICE).get();

					Portal.Server portal = new Portal.Server(name, type, server);
					
					portal.setPrice(price);
					
					if(container.contains(PERMISSION)) {
						portal.setPermission(container.getString(PERMISSION).get());
					}

					if (container.contains(COMMAND)) {
						portal.setCommand(container.getSerializable(COMMAND, Command.class).get());
					}
					
					if (container.contains(PROPERTIES)) {
						portal.setProperties(container.getSerializable(PROPERTIES, Properties.class).get());
					}

					return Optional.of(portal);
				}

				return Optional.empty();
			}
		}
	}

	public static class Local extends Portal {

		private Optional<Coordinate> coordinate;
		private Rotation rotation = Rotation.EAST;
		private boolean force = false;
		
		public Local(String name, PortalType type) {
			super(name, type);
		}


		public Rotation getRotation() {
			return rotation;
		}

		public void setRotation(Rotation rotation) {
			this.rotation = rotation;
		}
		
		public Optional<Coordinate> getCoordinate() {
			return coordinate;
		}
		
		public void setCoordinate(Coordinate coordinate) {
			this.coordinate = Optional.of(coordinate);
		}
		
		public boolean force() {
			return force;
		}
		
		public void setForce(boolean force) {
			this.force = force;
		}

		@Override
		public int getContentVersion() {
			return 0;
		}

		@Override
		public DataContainer toContainer() {
			DataContainer container = DataContainer.createNew().set(NAME, getName()).set(PORTAL_TYPE, getType().name()).set(ROTATION, getRotation().getName()).set(PRICE, getPrice()).set(FORCE, force());

			if (getPermission().isPresent()) {
				container.set(PERMISSION, getPermission().get());
			}
			
			if (getProperties().isPresent()) {
				container.set(PROPERTIES, getProperties().get());
			}

			if (getCoordinate().isPresent()) {
				container.set(COORDINATE, getCoordinate().get());
			}
			
			if (getCommand().isPresent()) {
				container.set(COMMAND, getCommand().get());
			}
			return container;
		}

		public static class Builder extends AbstractDataBuilder<Local> {

			public Builder() {
				super(Local.class, 0);
			}

			@Override
			protected Optional<Local> buildContent(DataView container) throws InvalidDataException {
				if (container.contains(NAME, PORTAL_TYPE)) {
					String name = container.getString(NAME).get();
					PortalType type = PortalType.valueOf(container.getString(PORTAL_TYPE).get());
					Rotation rotation = Rotation.get(container.getString(ROTATION).get()).get();
					Double price = container.getDouble(PRICE).get();
					boolean force = container.getBoolean(FORCE).get();

					Portal.Local portal = new Portal.Local(name, type);
					
					portal.setRotation(rotation);
					portal.setPrice(price);
					portal.setForce(force);

					if(container.contains(PERMISSION)) {
						portal.setPermission(container.getString(PERMISSION).get());
					}

					if (container.contains(COMMAND)) {
						portal.setCommand(container.getSerializable(COMMAND, Command.class).get());
					}
					
					if (container.contains(COORDINATE)) {
						portal.setCoordinate(container.getSerializable(COORDINATE, Coordinate.class).get());
					}
					
					if (container.contains(PROPERTIES)) {
						portal.setProperties(container.getSerializable(PROPERTIES, Properties.class).get());
					}

					return Optional.of(portal);
				}

				return Optional.empty();
			}
		}
	}

	public enum PortalType {
		BUTTON,
		DOOR,
		HOME,
		LEVER,
		PLATE,
		PORTAL,
		SIGN,
		WARP;
	}

	public static byte[] serialize(Portal portal) {
		try {
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			GZIPOutputStream gZipOutStream = new GZIPOutputStream(byteOutStream);
			DataFormats.NBT.writeTo(gZipOutStream, portal.toContainer());
			gZipOutStream.close();
			return byteOutStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
	}

	public static Portal deserialize(byte[] bytes) {
		DataContainer container;
		try {
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
			GZIPInputStream gZipInputSteam = new GZIPInputStream(byteInputStream);
			container = DataFormats.NBT.readFrom(gZipInputSteam);
		} catch (InvalidDataFormatException | IOException e1) {
			e1.printStackTrace();
			return null;
		}

		if(container.contains(SERVER)) {
			try {
				return Sponge.getDataManager().deserialize(Server.class, container).get();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			try {
				return Sponge.getDataManager().deserialize(Local.class, container).get();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
