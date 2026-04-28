package dev.jfronny.meteoradditions.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import net.minecraft.client.multiplayer.LegacyServerPinger;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.*;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ServerListPinger {
    private static final Splitter ZERO_SPLITTER = Splitter.on('\u0000').limit(6);
    private static final Logger LOGGER = LogManager.getLogger();
    private final EventLoopGroupHolder backend = EventLoopGroupHolder.remote(false);
    private final List<Connection> clientConnections = Collections.synchronizedList(Lists.newArrayList());
    private final ArrayList<IServerFinderDisconnectListener> disconnectListeners = new ArrayList<>();
    private boolean notifiedDisconnectListeners = false;
    private boolean failedToConnect = true;

    public void addServerFinderDisconnectListener(IServerFinderDisconnectListener listener) {
        disconnectListeners.add(listener);
    }

    private void notifyDisconnectListeners() {
        synchronized(this) {
            if (!notifiedDisconnectListeners) {
                notifiedDisconnectListeners = true;
                for (IServerFinderDisconnectListener l : disconnectListeners) {
                    if (l != null) {
                        if (failedToConnect) {
                            l.onServerFailed();
                        }
                        else {
                            l.onServerDisconnect();
                        }
                    }
                }
            }
        }
    }

    public void add(final MServerData entry) throws UnknownHostException {
        ServerAddress serverAddress = ServerAddress.parseString(entry.address);
        Optional<InetSocketAddress> address = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
        if (address.isEmpty()) {
            return;
        }
        final Connection clientConnection = Connection.connectToServer(address.get(), backend, null);
        failedToConnect = false;
        this.clientConnections.add(clientConnection);
        entry.label = "multiplayer.status.pinging";
        entry.ping = -1L;
        ClientStatusPacketListener listener = new ClientStatusPacketListener() {
            private boolean sentQuery;
            private boolean received;
            private long startTime;

            @Override
            public void handleStatusResponse(@NonNull ClientboundStatusResponsePacket packet) {
                if (this.received) {
                    clientConnection.disconnect(Component.translatable("multiplayer.status.unrequested"));
                    return;
                }
                this.received = true;
                ServerStatus serverMetadata = packet.status();
                if (serverMetadata.description() != null) {
                    entry.label = serverMetadata.description().getString();
                } else {
                    entry.label = "";
                }

                entry.version = serverMetadata.version().map(ServerStatus.Version::name).orElse("multiplayer.status.old");

                serverMetadata.players().ifPresentOrElse(players -> {
                    entry.playerCountLabel = ServerListPinger.getPlayerCountLabel(players.online(), players.max());
                    entry.playerCount = players.online();
                    entry.playercountMax = players.max();
                }, () -> {
                    entry.playerCountLabel = "multiplayer.status.unknown";
                });

                this.startTime = Util.getMillis();
                clientConnection.send(new ServerboundPingRequestPacket(this.startTime));
                this.sentQuery = true;
                notifyDisconnectListeners();
            }

            @Override
            public void handlePongResponse(@NonNull ClientboundPongResponsePacket packet) {
                long l = this.startTime;
                long m = Util.getMillis();
                entry.ping = m - l;
                clientConnection.disconnect(Component.translatable("multiplayer.status.finished"));
            }

            @Override
            public void onDisconnect(@NonNull DisconnectionDetails info) {
                if (!this.sentQuery) {
                    ServerListPinger.LOGGER.error("Can't ping {}: {}", entry.address, info.reason().getString());
                    entry.label = "multiplayer.status.cannot_connect";
                    entry.playerCountLabel = "";
                    entry.playerCount = 0;
                    entry.playercountMax = 0;
                    ServerListPinger.this.ping(entry);
                }
                notifyDisconnectListeners();
            }

            @Override
            public boolean isAcceptingMessages() {
                return clientConnection.isConnected();
            }
        };

        try {
            clientConnection.initiateServerboundStatusConnection(serverAddress.getHost(), serverAddress.getPort(), listener);
            clientConnection.send(ServerboundStatusRequestPacket.INSTANCE);
        } catch (Throwable var6) {
            LOGGER.error("Couldn't send handshake", var6);
        }
    }

    private void ping(final MServerData serverInfo) {
        final ServerAddress serverAddress = ServerAddress.parseString(serverInfo.address);
        new Bootstrap().group(backend.eventLoopGroup()).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                try {
                    ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException ignored) {
                }

                ch.pipeline().addLast(new LegacyServerPinger(serverAddress, ((protocolVersion, version, label, currentPlayers, maxPlayers) -> {
                    serverInfo.version = version;
                    serverInfo.label = label;
                    serverInfo.playerCountLabel = ServerListPinger.getPlayerCountLabel(currentPlayers, maxPlayers);
                })));
            }
        });
    }

    private static String getPlayerCountLabel(int i, int j) {
        return i + "/" + j;
    }

    public void tick() {
        synchronized(this.clientConnections) {
            Iterator<Connection> iterator = this.clientConnections.iterator();

            while(iterator.hasNext()) {
                Connection clientConnection = iterator.next();
                if (clientConnection.isConnected()) {
                    clientConnection.tick();
                } else {
                    iterator.remove();
                    clientConnection.handleDisconnection();
                }
            }
        }
    }

    public void cancel() {
        synchronized(this.clientConnections) {
            Iterator<Connection> iterator = this.clientConnections.iterator();

            while(iterator.hasNext()) {
                Connection clientConnection = iterator.next();
                if (clientConnection.isConnected()) {
                    iterator.remove();
                    clientConnection.disconnect(Component.translatable("multiplayer.status.cancelled"));
                }
            }
        }
    }
}
