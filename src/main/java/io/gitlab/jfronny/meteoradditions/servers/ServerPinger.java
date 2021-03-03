package io.gitlab.jfronny.meteoradditions.servers;

import io.gitlab.jfronny.meteoradditions.MeteorAdditions;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerPinger {
    private static final AtomicInteger threadNumber = new AtomicInteger(0);
    private ServerInfo server;
    private boolean done = false;
    private boolean failed = false;

    public void ping(String ip)
    {
        ping(ip, 25565);
    }

    public void ping(String ip, int port)
    {
        server = new ServerInfo("", ip + ":" + port, false);

        new Thread(() -> pingInCurrentThread(ip, port),
                "Server Pinger #" + threadNumber.incrementAndGet()).start();
    }

    private void pingInCurrentThread(String ip, int port)
    {
        MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();
        MeteorAdditions.LOG.info("Pinging " + ip + ":" + port + "...");

        try
        {
            pinger.add(server, () -> {});
            MeteorAdditions.LOG.info("Ping successful: " + ip + ":" + port);

        }catch(UnknownHostException e)
        {
            MeteorAdditions.LOG.warn("Unknown host: " + ip + ":" + port);
            failed = true;

        }catch(Exception e2)
        {
            MeteorAdditions.LOG.warn("Ping failed: " + ip + ":" + port);
            failed = true;
        }

        pinger.cancel();
        done = true;
    }

    public boolean isStillPinging()
    {
        return !done;
    }

    public boolean isWorking()
    {
        return !failed;
    }

    public String getServerIP()
    {
        return server.address;
    }
}
