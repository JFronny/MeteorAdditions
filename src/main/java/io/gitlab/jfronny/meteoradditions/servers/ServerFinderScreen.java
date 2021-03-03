package io.gitlab.jfronny.meteoradditions.servers;

import io.gitlab.jfronny.meteoradditions.IMultiplayerScreen;
import minegame159.meteorclient.gui.screens.WindowScreen;
import minegame159.meteorclient.gui.widgets.*;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ServerFinderScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;
    private ServerFinderState state;

    private final WTextBox ipBox;
    private final WIntTextBox maxThreadsBox;
    private final WButton searchButton;
    private final WLabel stateLabel;
    private final WLabel checkedLabel;
    private final WLabel workingLabel;

    private int maxThreads;
    private int checked;
    private int working;

    public ServerFinderScreen(MultiplayerScreen multiplayerScreen) {
        super("Server Discovery", true);
        this.multiplayerScreen = multiplayerScreen;
        WTable table = add(new WTable()).getWidget();
        table.add(new WLabel("This will search for servers with similar IPs"));
        table.row();
        table.add(new WLabel("to the IP you type into the field below."));
        table.row();
        table.add(new WLabel("The servers it finds will be added to your server list."));
        table.row();
        table.add(new WLabel("Server address:"));
        ipBox = table.add(new WTextBox("127.0.0.1", 200)).fillX().expandX().getWidget();
        table.row();
        table.add(new WLabel("Max. Threads:"));
        maxThreadsBox = table.add(new WIntTextBox(128, 200)).getWidget();
        table.row();
        stateLabel = table.add(new WLabel("")).getWidget();
        table.row();
        checkedLabel = table.add(new WLabel("")).getWidget();
        table.row();
        workingLabel = table.add(new WLabel("")).getWidget();
        table.row();
        searchButton = table.add(new WButton("Search")).fillX().expandX().getWidget();
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
        maxThreads = maxThreadsBox.getValue();
        checked = 0;
        working = 0;

        new Thread(this::findServers, "Server Discovery").start();
    }

    private void findServers()
    {
        try
        {
            InetAddress addr =
                    InetAddress.getByName(ipBox.getText().split(":")[0].trim());

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
        searchButton.setText(state.isRunning() ? "Cancel" : "Search");
        if (state.isRunning()) {
            ipBox.setFocused(false);
            maxThreadsBox.setFocused(false);
        }
        stateLabel.setText(state.toString());
        checkedLabel.setText("Checked: " + checked + " / 1792");
        workingLabel.setText("Working: " + working);
        searchButton.visible = !ipBox.getText().isEmpty();
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
