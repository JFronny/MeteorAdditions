package io.gitlab.jfronny.meteoradditions.servers;

import io.gitlab.jfronny.meteoradditions.IMultiplayerScreen;
import minegame159.meteorclient.gui.GuiTheme;
import minegame159.meteorclient.gui.WindowScreen;
import minegame159.meteorclient.gui.widgets.pressable.WCheckbox;
import minegame159.meteorclient.gui.widgets.containers.WTable;
import minegame159.meteorclient.utils.render.color.Color;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;

public class CleanUpScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;
    private final WCheckbox removeAll;
    private final WCheckbox removeFailed;
    private final WCheckbox removeOutdated;
    private final WCheckbox removeUnknown;
    private final WCheckbox removeGriefMe;
    private final WCheckbox rename;

    public CleanUpScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen, Screen parent) {
        super(theme, "Clean Up");
        this.multiplayerScreen = multiplayerScreen;
        this.parent = parent;
        WTable table = add(new WTable()).widget();
        table.add(theme.label("Remove:"));
        table.row();
        table.add(theme.label("Unknown Hosts:")).widget().tooltip = "";
        removeUnknown = table.add(theme.checkbox(true)).widget();
        table.row();
        table.add(theme.label("Outdated Servers:"));
        removeOutdated = table.add(theme.checkbox(false)).widget();
        table.row();
        table.add(theme.label("Failed Ping:"));
        removeFailed = table.add(theme.checkbox(true)).widget();
        table.row();
        table.add(theme.label("\"Server discovery\" Servers:"));
        removeGriefMe = table.add(theme.checkbox(false)).widget();
        table.row();
        table.add(theme.label("Everything:")).widget().color = new Color(255, 0, 0);
        removeAll = table.add(theme.checkbox(false)).widget();
        table.row();
        table.add(theme.label("Rename all Servers:"));
        rename = table.add(theme.checkbox(true)).widget();
        table.row();
        table.add(theme.button("Execute!")).expandX().widget().action = this::cleanUp;
    }

    private void cleanUp()
    {
        for(int i = multiplayerScreen.getServerList().size() - 1; i >= 0; i--)
        {
            ServerInfo server = multiplayerScreen.getServerList().get(i);

            if(removeAll.checked || shouldRemove(server))
                multiplayerScreen.getServerList().remove(server);
        }

        if(rename.checked)
            for(int i = 0; i < multiplayerScreen.getServerList().size(); i++)
            {
                ServerInfo server = multiplayerScreen.getServerList().get(i);
                server.name = "Server discovery " + (i + 1);
            }

        saveServerList();
        client.openScreen(parent);
    }

    private boolean shouldRemove(ServerInfo server)
    {
        if(server == null)
            return false;

        if(removeUnknown.checked && isUnknownHost(server))
            return true;

        if(removeOutdated.checked && !isSameProtocol(server))
            return true;

        if(removeFailed.checked && isFailedPing(server))
            return true;

        if(removeGriefMe.checked && isGriefMeServer(server))
            return true;

        return false;
    }

    private boolean isUnknownHost(ServerInfo server)
    {
        if(server.label == null)
            return false;

        if(server.label.getString() == null)
            return false;

        return server.label.getString()
                .equals("\u00a74Can\'t resolve hostname");
    }

    private boolean isSameProtocol(ServerInfo server)
    {
        return server.protocolVersion == SharedConstants.getGameVersion()
                .getProtocolVersion();
    }

    private boolean isFailedPing(ServerInfo server)
    {
        return server.ping != -2L && server.ping < 0L;
    }

    private boolean isGriefMeServer(ServerInfo server)
    {
        return server.name != null && server.name.startsWith("Server discovery ");
    }

    private void saveServerList()
    {
        multiplayerScreen.getServerList().saveFile();

        MultiplayerServerListWidget serverListSelector =
                ((IMultiplayerScreen)multiplayerScreen).getServerListWidget();

        serverListSelector.setSelected(null);
        serverListSelector.setServers(multiplayerScreen.getServerList());
    }
}
