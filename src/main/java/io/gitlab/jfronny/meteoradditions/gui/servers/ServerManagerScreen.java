package io.gitlab.jfronny.meteoradditions.gui.servers;

import io.gitlab.jfronny.meteoradditions.mixin.MultiplayerScreenAccessor;
import io.gitlab.jfronny.meteoradditions.mixin.ServerListAccessor;
import io.gitlab.jfronny.meteoradditions.util.IPAddress;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.TranslatableText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerManagerScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;
    public ServerManagerScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen) {
        super(theme, "Manage Servers");
        this.parent = multiplayerScreen;
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();
        addButton(l, "Find Servers (new)", () -> new ServerFinderScreen(theme, multiplayerScreen, this));
        addButton(l, "Find Servers (legacy)", () -> new LegacyServerFinderScreen(theme, multiplayerScreen, this));
        addButton(l, "Clean Up", () -> new CleanUpScreen(theme, multiplayerScreen, this));
        l = add(theme.horizontalList()).expandX().widget();
        l.add(theme.button("Save IPs")).expandX().widget().action = () -> {
            int newIPs = 0;

            Path filePath = FabricLoader.getInstance().getGameDir().resolve("servers.txt");
            Set<IPAddress> hashedIPs = new HashSet<>();
            if (Files.exists(filePath)) {
                try {
                    List<String> ips = Files.readAllLines(filePath);
                    for (String ip: ips) {
                        IPAddress parsedIP = IPAddress.fromText(ip);
                        if (parsedIP != null)
                            hashedIPs.add(parsedIP);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ServerList servers = multiplayerScreen.getServerList();
            for (int i = 0; i < servers.size(); i++) {
                ServerInfo info = servers.get(i);
                IPAddress addr = IPAddress.fromText(info.address);
                if (addr != null && hashedIPs.add(addr))
                    newIPs++;
            }

            StringBuilder fileOutput = new StringBuilder();
            for (IPAddress ip : hashedIPs) {
                String stringIP = ip.toString();
                if (stringIP != null)
                    fileOutput.append(stringIP).append("\n");
            }
            try {
                Files.writeString(filePath, fileOutput.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            SystemToast.add(client.getToastManager(),
                    SystemToast.Type.WORLD_BACKUP,
                    new TranslatableText("meteor-additions.saved-ip-success"),
                    new TranslatableText(newIPs == 1 ? "meteor-additions.saved-ip" : "meteor-additions.saved-ips", newIPs));
        };
        l.add(theme.button("Load IPs from clipboard")).expandX().widget().action = () -> {
            String[] split = MinecraftClient.getInstance().keyboard.getClipboard().split("[\r\n]+");
            List<ServerInfo> servers = ((ServerListAccessor) multiplayerScreen.getServerList()).getServers();
            Set<String> presentAddresses = new HashSet<>();
            int newIPs = 0;
            for (ServerInfo server : servers) presentAddresses.add(server.address);
            for (int i = 0; i < split.length; i++) {
                if (!presentAddresses.contains(split[i])) {
                    servers.add(new ServerInfo("Server discovery #" + i, split[i], false));
                    newIPs++;
                }
            }
            multiplayerScreen.getServerList().saveFile();
            ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().setSelected(null);
            ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().setServers(multiplayerScreen.getServerList());
            SystemToast.add(client.getToastManager(),
                    SystemToast.Type.WORLD_BACKUP,
                    new TranslatableText("meteor-additions.loaded-ip-success"),
                    new TranslatableText(newIPs == 1 ? "meteor-additions.loaded-ip" : "meteor-additions.loaded-ips", newIPs));
        };
    }

    private void addButton(WContainer c, String text, IGetter<Screen> action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = () -> client.setScreen(action.get());
    }
}
