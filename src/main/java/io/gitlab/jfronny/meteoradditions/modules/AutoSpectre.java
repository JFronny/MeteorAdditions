package io.gitlab.jfronny.meteoradditions.modules;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoSpectre extends Module {
    public AutoSpectre() {
        super(Categories.Player, "auto-spectre", "Allows you to keep playing after you die");
    }

    private boolean wasTriggered = false;

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        if (wasTriggered) {
            wasTriggered = false;
            warning("You are no longer in a god like mode!");
            if (mc.player != null && this.mc.player.networkHandler != null) {
                mc.player.requestRespawn();
                info("Respawn request has been sent to the server.");
            }
        }
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        this.wasTriggered = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (wasTriggered && mc.player != null) {
            if (mc.player.getHealth() < 1f)
                mc.player.setHealth(20f);
            if (mc.player.getHungerManager().getFoodLevel() < 20)
                mc.player.getHungerManager().setFoodLevel(20);
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof DeathScreen) {
            event.cancel();
            if (!wasTriggered) {
                wasTriggered = true;
                info("""
                        You are now in a god like mode.
                        From now on you'll have to reconnect to break/place blocks,
                        you can't collect xp or items from ground and
                        you'll be invisible on some servers too."""
                );
            }
        }
    }
}
