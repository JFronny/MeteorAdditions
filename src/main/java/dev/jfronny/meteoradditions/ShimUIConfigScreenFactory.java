package dev.jfronny.meteoradditions;

import dev.jfronny.libjf.config.api.v2.ConfigInstance;
import dev.jfronny.libjf.config.api.v2.Naming;
import dev.jfronny.libjf.config.api.v2.ui.ConfigScreenFactory;
import dev.jfronny.meteoradditions.util.ShimUi;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.NonNull;

public class ShimUIConfigScreenFactory implements ConfigScreenFactory<ShimUi.ShimUiScreen, ShimUIConfigScreenFactory.Built> {
    @Override
    public @NonNull Built create(@NonNull ConfigInstance config, @NonNull Naming naming, Screen screen) {
        return new Built(new ShimUi.ShimUiScreen(screen, GuiThemes.get(), config, naming));
    }

    @Override
    public int getPriority() {
        return -5;
    }

    public static class Built implements ConfigScreenFactory.Built<ShimUi.ShimUiScreen> {
        private final ShimUi.ShimUiScreen screen;

        public Built(ShimUi.ShimUiScreen screen) {
            this.screen = screen;
        }

        @Override
        public ShimUi.@NonNull ShimUiScreen get() {
            return screen;
        }

        @Override
        public void onSave(@NonNull Runnable runnable) {
            screen.onSave(runnable);
        }
    }
}
