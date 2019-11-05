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

public class Portal implements DataSerializable {

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
	private Optional<Coordinate> coordinate = Optional.empty();
	private Rotation rotation = Rotation.EAST;
	private boolean force = false;
	private double price = 0;
	private Optional<String> server = Optional.empty();
	private Optional<String> permission = Optional.empty();
	private Optional<Properties> properties = Optional.empty();
	private Optional<Command> command = Optional.empty();

	public Portal(String name, PortalType type) {
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

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Optional<String> getServer() {
		return server;
	}

	public void setServer(String server) {
		if (server == null)
		{
			throw new RuntimeException("fuck");
		}

		this.server = Optional.of(server);
	}

	public void unsetServer() {
		this.server = Optional.empty();
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

	@Override
	public DataContainer toContainer() {
		DataContainer container = DataContainer.createNew()
			.set(NAME, getName())
			.set(PORTAL_TYPE, getType().name())
			.set(ROTATION, getRotation().getName())
			.set(PRICE, getPrice())
			.set(FORCE, force());

		getCoordinate().ifPresent((coordinate) -> container.set(COORDINATE, coordinate));

		getServer().ifPresent((server) -> container.set(SERVER, server));

		getPermission().ifPresent((permission) -> container.set(PERMISSION, permission));

		getCommand().ifPresent((command) -> container.set(COMMAND, command));

		getProperties().ifPresent((properties) -> container.set(PROPERTIES, properties));

		return container;
	}

	@Override
	public int getContentVersion() {
		return 1;
	}

	public static class Builder extends AbstractDataBuilder<Portal> {

		public Builder() {
			super(Portal.class, 0);
		}

		@Override
		protected Optional<Portal> buildContent(DataView container) throws InvalidDataException {
			if (container.contains(NAME, PORTAL_TYPE)) {
				String name = container.getString(NAME).get();
				PortalType type = PortalType.valueOf(container.getString(PORTAL_TYPE).get());

				Portal portal = new Portal(name, type);

				// for backwards compatibility, we consider that some of these fields may be missing
				container.getSerializable(COORDINATE, Coordinate.class).ifPresent(portal::setCoordinate);

				container.getString(ROTATION).flatMap(Rotation::get).ifPresent(portal::setRotation);

				container.getBoolean(FORCE).ifPresent(portal::setForce);

				container.getString(SERVER).ifPresent(portal::setServer);

				container.getDouble(PRICE).ifPresent(portal::setPrice);

				container.getString(PERMISSION).ifPresent(portal::setPermission);

				container.getSerializable(COMMAND, Command.class).ifPresent(portal::setCommand);

				container.getSerializable(PROPERTIES, Properties.class).ifPresent(portal::setProperties);

				return Optional.of(portal);
			}

			return Optional.empty();
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


		try {
			return Sponge.getDataManager().deserialize(Portal.class, container).get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
