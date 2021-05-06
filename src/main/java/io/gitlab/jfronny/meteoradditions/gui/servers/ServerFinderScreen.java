package io.gitlab.jfronny.meteoradditions.gui.servers;

import io.gitlab.jfronny.meteoradditions.mixininterface.IMultiplayerScreen;
import io.gitlab.jfronny.meteoradditions.util.ServerPinger;
import minegame159.meteorclient.gui.GuiTheme;
import minegame159.meteorclient.gui.WindowScreen;
import minegame159.meteorclient.gui.widgets.pressable.WButton;
import minegame159.meteorclient.gui.widgets.input.WTextBox;
import minegame159.meteorclient.gui.widgets.input.WIntEdit;
import minegame159.meteorclient.gui.widgets.containers.WTable;
import minegame159.meteorclient.gui.widgets.WLabel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ServerFinderScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;
    private ServerFinderState state;

    private final WTextBox ipBox;
    private final WIntEdit maxThreadsBox;
    private final WButton searchButton;
    private final WLabel stateLabel;
    private final WLabel checkedLabel;
    private final WLabel workingLabel;

    private int maxThreads;
    private int checked;
    private int working;

    public ServerFinderScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen, Screen parent) {
        super(theme, "Server Discovery");
        this.multiplayerScreen = multiplayerScreen;
        this.parent = parent;
        WTable table = add(new WTable()).widget();
        table.add(theme.label("This will search for servers with similar IPs"));
        table.row();
        table.add(theme.label("to the IP you type into the field below."));
        table.row();
        table.add(theme.label("The servers it finds will be added to your server list."));
        table.row();
        table.add(theme.label("Server address:"));
        ipBox = table.add(theme.textBox("127.0.0.1")).expandX().widget();
        table.row();
        table.add(theme.label("Max. Threads:"));
        maxThreadsBox = table.add(theme.intEdit(128, 1, 256)).widget();
        table.row();
        stateLabel = table.add(theme.label("")).widget();
        table.row();
        checkedLabel = table.add(theme.label("")).widget();
        table.row();
        workingLabel = table.add(theme.label("")).widget();
        table.row();
        searchButton = table.add(theme.button("Search")).expandX().widget();
        searchButton.action = this::searchOrCancel;
        state = ServerFinderState.NOT_RUNNING;
    }

    private void searchOrCancel() {
        if(state.isRunning())
        {
            state = ServerFinderState.CANCELLED;
            return;
        }

        state = ServerFinderState.RESOLVING;
        maxThreads = maxThreadsBox.get();
        checked = 0;
        working = 0;

        new Thread(this::findServers, "Server Discovery").start();
    }

    private void findServers()
    {
        try
        {
            InetAddress addr =
                    InetAddress.getByName(ipBox.get().split(":")[0].trim());

            int[] ipParts = new int[4];
            for(int i = 0; i < 4; i++)
                ipParts[i] = addr.getAddress()[i] & 0xff;

            state = ServerFinderState.SEARCHING;
            ArrayList<ServerPinger> pingers = new ArrayList<>();
            int[] changes = {0, 1, -1, 2, -2, 3, -3};
            for(int change : changes)
                for(int i2 = 0; i2 <= 255; i2++)
                {
                    if(state == ServerFinderState.CANCELLED)
                        return;

                    int[] ipParts2 = ipParts.clone();
                    ipParts2[2] = ipParts[2] + change & 0xff;
                    ipParts2[3] = i2;
                    String ip = ipParts2[0] + "." + ipParts2[1] + "."
                            + ipParts2[2] + "." + ipParts2[3];

                    ServerPinger pinger = new ServerPinger();
                    pinger.ping(ip);
                    pingers.add(pinger);
                    while(pingers.size() >= maxThreads)
                    {
                        if(state == ServerFinderState.CANCELLED)
                            return;

                        updatePingers(pingers);
                    }
                }
            while(pingers.size() > 0)
            {
                if(state == ServerFinderState.CANCELLED)
                    return;

                updatePingers(pingers);
            }
            state = ServerFinderState.DONE;

        } catch(UnknownHostException e) {
            state = ServerFinderState.UNKNOWN_HOST;

        } catch(Exception e) {
            e.printStackTrace();
            state = ServerFinderState.ERROR;
        }
    }

    @Override
    public void tick()
    {
        searchButton.set(state.isRunning() ? "Cancel" : "Search");
        if (state.isRunning()) {
            ipBox.setFocused(false);
            maxThreadsBox.set(128);
        }
        stateLabel.set(state.toString());
        checkedLabel.set("Checked: " + checked + " / 1792");
        workingLabel.set("Working: " + working);
        searchButton.visible = !ipBox.get().isEmpty();
    }

    private boolean isServerInList(String ip)
    {
        for(int i = 0; i < multiplayerScreen.getServerList().size(); i++)
            if(multiplayerScreen.getServerList().get(i).address.equals(ip))
                return true;

        return false;
    }

    private void updatePingers(ArrayList<ServerPinger> pingers)
    {
        for(int i = 0; i < pingers.size(); i++)
            if(!pingers.get(i).isStillPinging())
            {
                checked++;
                if(pingers.get(i).isWorking())
                {
                    working++;

                    if(!isServerInList(pingers.get(i).getServerIP()))
                    {
                        multiplayerScreen.getServerList()
                                .add(new ServerInfo("Server discovery " + working,
                                        pingers.get(i).getServerIP(), false));
                        multiplayerScreen.getServerList().saveFile();
                        ((IMultiplayerScreen)multiplayerScreen).getServerListWidget()
                                .setSelected(null);
                        ((IMultiplayerScreen)multiplayerScreen).getServerListWidget()
                                .setServers(multiplayerScreen.getServerList());
                    }
                }
                pingers.remove(i);
            }
    }

    @Override
    public void onClose()
    {
        state = ServerFinderState.CANCELLED;
        super.onClose();
    }

    enum ServerFinderState
    {
        NOT_RUNNING(""),
        SEARCHING("Searching..."),
        RESOLVING("Resolving..."),
        UNKNOWN_HOST("Unknown Host!"),
        CANCELLED("Cancelled!"),
        DONE("Done!"),
        ERROR("An error occurred!");

        private final String name;

        private ServerFinderState(String name)
        {
            this.name = name;
        }

        public boolean isRunning()
        {
            return this == SEARCHING || this == RESOLVING;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
