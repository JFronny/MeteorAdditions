package dev.jfronny.meteoradditions.gui.servers;

import com.mojang.blaze3d.Blaze3D;
import dev.jfronny.commons.throwable.Try;
import dev.jfronny.meteoradditions.MeteorAdditions;
import dev.jfronny.meteoradditions.mixin.JoinMultiplayerScreenAccessor;
import dev.jfronny.meteoradditions.mixin.ServerListAccessor;
import dev.jfronny.meteoradditions.util.IPAddress;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerManagerScreen extends WindowScreen {
    private final JoinMultiplayerScreen multiplayerScreen;
    private static final SDL_DialogFileFilter.Buffer saveFileFilters;

    static {
        MemoryStack stack = MemoryStack.stackPush();
        saveFileFilters = SDL_DialogFileFilter.malloc(1, stack);
        saveFileFilters.name(stack.UTF8("Text Files")).pattern(stack.UTF8("txt"));
    }

    public ServerManagerScreen(GuiTheme theme, JoinMultiplayerScreen multiplayerScreen) {
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
        l.add(theme.button("Save IPs")).expandX().widget().action = () -> SDLDialog.SDL_ShowSaveFileDialog((_, file, _) -> Try.handle(() -> {
            if (file == 0) return;
            long filePointer = MemoryUtil.memGetAddress(file);
            if (filePointer == 0) return;
            String targetPath = MemoryUtil.memUTF8(filePointer);
            if (!targetPath.endsWith(".txt")) targetPath += ".txt";
            Path filePath = Path.of(targetPath);

            int newIPs = 0;

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

            ServerList servers = multiplayerScreen.getServers();
            for (int i = 0; i < servers.size(); i++) {
                ServerData info = servers.get(i);
                IPAddress addr = IPAddress.fromText(info.ip);
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

            toast("meteor-additions.saved-ip-success", newIPs == 1 ? "meteor-additions.saved-ip" : "meteor-additions.saved-ips", newIPs);
        }, e -> {
            MeteorAdditions.LOG.error("Could not save IPs", e);
            toast("meteor-additions.error", "meteor-additions.saved-ip-failed");
        }), 0, 0, saveFileFilters, "");
        l.add(theme.button("Load IPs")).expandX().widget().action = () -> SDLDialog.SDL_ShowOpenFileDialog((_, file, _) -> Try.handle(() -> {
            if (file == 0) return;
            long filePointer = MemoryUtil.memGetAddress(file);
            if (filePointer == 0) return;
            Path filePath = Path.of(MemoryUtil.memUTF8(filePointer));
            if (!Files.exists(filePath)) return;

            List<ServerData> servers = ((ServerListAccessor) multiplayerScreen.getServers()).getServers();
            Set<String> presentAddresses = new HashSet<>();
            int newIPs = 0;
            for (ServerData server : servers) presentAddresses.add(server.ip);
            for (String addr : Minecraft.getInstance().keyboardHandler.getClipboard().split("[\r\n]+")) {
                if (presentAddresses.add(addr = addr.split(" ")[0])) {
                    servers.add(new ServerData("Server discovery #" + presentAddresses.size(), addr, ServerData.Type.OTHER));
                    newIPs++;
                }
            }
            multiplayerScreen.getServers().save();
            ((JoinMultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().setSelected(null);
            ((JoinMultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().updateOnlineServers(multiplayerScreen.getServers());
            toast("meteor-additions.loaded-ip-success", newIPs == 1 ? "meteor-additions.loaded-ip" : "meteor-additions.loaded-ips", newIPs);
        }, e -> {
            MeteorAdditions.LOG.error("Could not load IPs", e);
            toast("meteor-additions.error", "meteor-additions.loaded-ip-failed");
        }), 0L, 0, saveFileFilters, "", false);
        l.add(theme.button("Mass scan ↗")).expandX().widget().action = () -> {
            Blaze3D.openUri(URI.create("https://github.com/JFronny/MeteorAdditions/tree/master/masscan"));
        };
    }

    private void toast(String titleKey, String descriptionKey, Object... params) {
        SystemToast.add(minecraft.gui.toastManager(), SystemToast.SystemToastId.WORLD_BACKUP, Component.translatable(titleKey), Component.translatable(descriptionKey, params));
    }

    private void addButton(WContainer c, String text, IGetter<Screen> action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = () -> minecraft.gui.setScreen(action.get());
    }
}
