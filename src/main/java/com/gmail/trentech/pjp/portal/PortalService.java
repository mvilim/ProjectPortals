package com.gmail.trentech.pjp.portal;

import static com.gmail.trentech.pjp.data.Keys.BED_LOCATIONS;
import static com.gmail.trentech.pjp.data.Keys.LAST_LOCATIONS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.network.RawDataListener;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjc.core.BungeeManager;
import com.gmail.trentech.pjc.core.ConfigManager;
import com.gmail.trentech.pjc.core.SQLManager;
import com.gmail.trentech.pjc.core.TeleportManager;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.effects.PortalEffect;
import com.gmail.trentech.pjp.events.TeleportEvent;
import com.gmail.trentech.pjp.portal.Portal.PortalType;
import com.gmail.trentech.pjp.portal.features.Command;
import com.gmail.trentech.pjp.portal.features.Command.SourceType;
import com.gmail.trentech.pjp.portal.features.Coordinate;
import com.gmail.trentech.pjp.portal.features.Coordinate.Preset;
import com.gmail.trentech.pjp.portal.features.Properties;

public class PortalService {

	public static String BUNGEE_TELEPORT = "BungeeeTeleport";
	public static int TELEPORT_RETRY_MS = 200;
	public static int TELEPORT_MAX_RETRIES = 25;

	private static class WrappedOutputChannel
	{
		private ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		private DataOutputStream dostream = new DataOutputStream(ostream);
		private ChannelBuf buf;

		private WrappedOutputChannel(ChannelBuf buf)
		{
			this.buf = buf;
		}

		private DataOutputStream stream()
		{
			return dostream;
		}

		private void write(byte[] bytes) throws IOException
		{
			dostream.writeInt(bytes.length);
			for (byte b : bytes)
			{
				dostream.writeByte(b);
			}
		}

		private void flush() throws IOException
		{
			dostream.flush();
			ostream.flush();
			if (ostream.size() < Short.MAX_VALUE)
			{
				buf.writeShort((short) ostream.size());
				buf.writeBytes(ostream.toByteArray());
			}
			else {
				throw new IOException("Wrapped message size is too large");
			}
		}
	}

	private static class WrappedInputChannel
	{
		private DataInputStream distream;

		private WrappedInputChannel(ChannelBuf buf)
		{
			short len = buf.readShort();
			distream = new DataInputStream(new ByteArrayInputStream(buf.readBytes(len)));
		}

		private DataInputStream stream()
		{
			return distream;
		}
	}

	public void teleportWithRetry(UUID playerId, Portal portal, int counter)
	{
		if (counter < TELEPORT_MAX_RETRIES)
		{
			Sponge.getScheduler().createTaskBuilder().delay(TELEPORT_RETRY_MS, TimeUnit.MILLISECONDS).execute(() -> {
				Optional<Player> player = Sponge.getServer().getPlayer(playerId);
				if (player.isPresent())
				{
					execute(player.get(), portal, true);
				}
				else{
					teleportWithRetry(playerId, portal, counter + 1);
				}
			}).submit(Main.getPlugin());;
		}
		else
		{
			System.err.println("Could not find player");
		}
	}

	public class BungeeTeleportListener implements RawDataListener {
		public void handlePayload(ChannelBuf data, RemoteConnection connection, Platform.Type side)
		{
			String subChannel = data.readUTF();
			if (subChannel.equals(BUNGEE_TELEPORT))
			{
				WrappedInputChannel wrapped = new WrappedInputChannel(data);

				try {
					UUID playerId = UUID.fromString(wrapped.stream().readUTF());
					int nbytes = wrapped.stream().readInt();
					byte[] pdata = new byte[nbytes];
					wrapped.stream().readFully(pdata);

					Portal portal = Portal.deserialize(pdata);

					teleportWithRetry(playerId, portal, 0);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	private static ConcurrentHashMap<String, Portal> cache = new ConcurrentHashMap<>();
	
	public Optional<Portal> get(String name, PortalType type) {
		if (cache.containsKey(name)) {
			Portal portal = cache.get(name);

			if (portal.getType().equals(type)) {
				return Optional.of(cache.get(name));
			}
		}

		return Optional.empty();
	}

	public Optional<Portal> get(Location<World> location, PortalType type) {
		if (type.equals(PortalType.PORTAL)) {
			for (Entry<String, Portal> entry : cache.entrySet()) {
				Portal portal = entry.getValue();

				if (!portal.getType().equals(type)) {
					continue;
				}

				Properties properties = portal.getProperties().get();

				List<Location<World>> frame = properties.getFrame();

				if (!frame.get(0).getExtent().equals(location.getExtent())) {
					continue;
				}

				for (Location<World> loc : frame) {
					if (loc.getBlockPosition().equals(location.getBlockPosition())) {
						return Optional.of(portal);
					}
				}

				for (Location<World> loc : properties.getFill()) {
					if (loc.getBlockPosition().equals(location.getBlockPosition())) {
						return Optional.of(portal);
					}
				}
			}
		}

		return get(location.getExtent().getName() + ":" + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ(), type);
	}

	public List<Portal> all(PortalType type) {
		List<Portal> list = new ArrayList<>();

		for (Entry<String, Portal> entry : cache.entrySet()) {
			Portal portal = entry.getValue();

			if (portal.getType().equals(type)) {
				list.add(portal);
			}
		}

		return list;
	}

	public void init() {
		try {
			SQLManager sqlManager = SQLManager.get(Main.getPlugin());
			Connection connection = sqlManager.getDataSource().getConnection();

			PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + sqlManager.getPrefix("PORTALS"));

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				String name = result.getString("Name");

				Portal portal;
				try {
					portal = Portal.deserialize(result.getBytes("Data"));

					cache.put(name, portal);

					Optional<Properties> optionalProperties = portal.getProperties();
					
					if (optionalProperties.isPresent()) {
						PortalEffect.activate(portal);
					}
				} catch(Exception e) {
					e.printStackTrace();
					portal = new Portal(name, null);
					remove(portal);
				}		
			}

			connection.close();

			BungeeTeleportListener listener = new BungeeTeleportListener();

			ChannelBinding.RawDataChannel channel = Sponge.getChannelRegistrar().getOrCreateRaw(Main.getPlugin(), "BungeeCord");
			channel.addListener(Platform.Type.SERVER, listener);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void create(Portal portal) {
		try {
			SQLManager sqlManager = SQLManager.get(Main.getPlugin());
			Connection connection = sqlManager.getDataSource().getConnection();

			PreparedStatement statement = connection.prepareStatement("INSERT into " + sqlManager.getPrefix("PORTALS") + " (Name, Data) VALUES (?, ?)");

			statement.setString(1, portal.getName());
			statement.setBytes(2, Portal.serialize(portal));

			statement.executeUpdate();

			connection.close();

			cache.put(portal.getName(), portal);

			Optional<Properties> optionalProperties = portal.getProperties();
			
			if (optionalProperties.isPresent()) {
				PortalEffect.activate(portal);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void create(Portal portal, Location<World> location) {
		portal.setName(location.getExtent().getName() + ":" + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ());

		create(portal);
	}

	public void update(Portal portal) {
		try {
			SQLManager sqlManager = SQLManager.get(Main.getPlugin());
			Connection connection = sqlManager.getDataSource().getConnection();

			PreparedStatement statement = connection.prepareStatement("UPDATE " + sqlManager.getPrefix("PORTALS") + " SET Data = ? WHERE Name = ?");

			statement.setBytes(1, Portal.serialize(portal));
			statement.setString(2, portal.getName());

			statement.executeUpdate();

			connection.close();

			cache.put(portal.getName(), portal);

			Optional<Properties> optionalProperties = portal.getProperties();
			
			if (optionalProperties.isPresent()) {
				for (Task task : Sponge.getScheduler().getScheduledTasks()) {
					if (task.getName().equals(portal.getName())) {
						task.cancel();
						break;
					}
				}
				PortalEffect.deactivate(portal);
				PortalEffect.activate(portal);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void remove(Portal portal) {
		if(cache.containsKey(portal.getName())) {
			cache.remove(portal.getName());
		}
		
		try {
			SQLManager sqlManager = SQLManager.get(Main.getPlugin());
			Connection connection = sqlManager.getDataSource().getConnection();

			PreparedStatement statement = connection.prepareStatement("DELETE from " + sqlManager.getPrefix("PORTALS") + " WHERE Name = ?");

			statement.setString(1, portal.getName());
			statement.executeUpdate();

			connection.close();

			Optional<Properties> optionalProperties = portal.getProperties();
			
			if (optionalProperties.isPresent()) {
				for (Task task : Sponge.getScheduler().getScheduledTasks()) {
					if (task.getName().equals(portal.getName())) {
						task.cancel();
						break;
					}
				}
				PortalEffect.deactivate(portal);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean execute(Player player, Portal portal) {
		return execute(player, portal, false);
	}

	public boolean execute(Player player, Portal portal, boolean fromBungee) {
		AtomicReference<Boolean> bool = new AtomicReference<>(false);

		Optional<Command> optionalCommand = portal.getCommand();
		
		if(optionalCommand.isPresent()) {
			Command command = optionalCommand.get();
			
			if(command.getSrcType().equals(SourceType.CONSOLE)) {
				command.execute();
			} else {
				command.execute(player);
			}
		}
		
		if (portal.getServer().isPresent() && !fromBungee) {

			Consumer<String> consumer = (serverName) -> {
				Task.builder().execute(() -> {
					TeleportEvent.Server teleportEvent = new TeleportEvent.Server(player, serverName, portal.getServer().get(), portal.getPrice(), portal.getPermission(), Cause.of(EventContext.builder().add(EventContextKeys.PLAYER, player).build(), portal));

					if (!Sponge.getEventManager().post(teleportEvent)) {
						PortalEffect.teleport(player.getLocation());

						ChannelBinding.RawDataChannel bungeeChannel = Sponge.getChannelRegistrar().getOrCreateRaw(Main.getPlugin(), "BungeeCord");

						bungeeChannel.sendTo(player, buffer -> {
							buffer.writeUTF("Forward")
								.writeUTF(teleportEvent.getDestination())
								.writeUTF(BUNGEE_TELEPORT);
							try {
								// the bungeecord forwarding protocol requires that we nest data streams in this way (as it allows only a single variable length byte array)
								WrappedOutputChannel wrapped = new WrappedOutputChannel(buffer);
								wrapped.stream().writeUTF(player.getUniqueId().toString());
								wrapped.write(Portal.serialize(portal));
								wrapped.flush();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						});

						player.setLocation(player.getWorld().getSpawnLocation());

						BungeeManager.connect(player, teleportEvent.getDestination());

						bool.set(true);
					}
				}).submit(Main.instance());
			};

			BungeeManager.getServer(consumer, player);
		} else {
			Optional<Coordinate> optionalCoodinate = portal.getCoordinate();
			
			if(optionalCoodinate.isPresent()) {
				Coordinate coordinate = optionalCoodinate.get();
				
				Optional<Location<World>> optionalSpawnLocation = Optional.empty();

				if(coordinate.getPreset().equals(Preset.BED)) {
					Map<String, Coordinate> list = new HashMap<>();

					Optional<Map<String, Coordinate>> optionalList = player.get(BED_LOCATIONS);

					if (optionalList.isPresent()) {
						list = optionalList.get();
					}
					
					if (coordinate.getOptionalWorld().isPresent())
					{
						String worldUuid = coordinate.getOptionalWorld().get().getUniqueId().toString();

						if(list.containsKey(worldUuid)) {
							optionalSpawnLocation = list.get(worldUuid).getLocation();
						}else {
							optionalSpawnLocation = coordinate.getLocation();
						}
					};
				} else if(coordinate.getPreset().equals(Preset.LAST_LOCATION)) {
					Map<String, Coordinate> list = new HashMap<>();

					Optional<Map<String, Coordinate>> optionalList = player.get(LAST_LOCATIONS);

					if (optionalList.isPresent()) {
						list = optionalList.get();
					}
					
					if (coordinate.getOptionalWorld().isPresent())
					{
						String worldUuid = coordinate.getOptionalWorld().get().getUniqueId().toString();

						if(list.containsKey(worldUuid)) {
							optionalSpawnLocation = list.get(worldUuid).getLocation();
						}else {
							optionalSpawnLocation = coordinate.getLocation();
						}
					};
					
				} else if(coordinate.getPreset().equals(Preset.RANDOM)) {

					if (coordinate.getOptionalWorld().isPresent())
					{
						optionalSpawnLocation = TeleportManager.getRandomLocation(coordinate.getOptionalWorld().get(), ConfigManager.get(Main.getPlugin()).getConfig().getNode("options", "random_spawn_radius").getInt());
					};
				} else {
					optionalSpawnLocation = coordinate.getLocation();
				}

				if (optionalSpawnLocation.isPresent()) {
					Location<World> spawnLocation = optionalSpawnLocation.get();
		
					TeleportEvent.Local teleportEvent = new TeleportEvent.Local(player, player.getLocation(), spawnLocation, portal.getPrice(), portal.force(), portal.getPermission(), portal.getServer(), Cause.of(EventContext.builder().add(EventContextKeys.PLAYER, player).build(), portal));
		
					if (!Sponge.getEventManager().post(teleportEvent)) {
						spawnLocation = teleportEvent.getDestination();
		
						Vector3d rotation = portal.getRotation().toVector3d();

						PortalEffect.teleport(player.getLocation());
						player.setLocationAndRotation(spawnLocation, rotation);
						PortalEffect.teleport(spawnLocation);
						
						return true;
					}
				} else {
					player.sendMessage(Text.of(TextColors.RED, "Could not find location"));
				}
			}
		}

		return bool.get();
	}
}
