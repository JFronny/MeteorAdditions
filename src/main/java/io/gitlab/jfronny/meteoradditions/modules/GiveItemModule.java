package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.meteoradditions.MeteorAdditions;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

public abstract class GiveItemModule extends Module {
    public GiveItemModule(String name, String description) {
        super(MeteorAdditions.category, name, description);
    }

    @Override
    public void doAction(boolean onActivateDeactivate) {
        if (mc.player == null) {
            MeteorAdditions.LOG.warn("GiveItem modules may only be used in a world");
            return;
        }

        if(!mc.player.abilities.creativeMode)
        {
            ChatUtils.error("Creative mode only.");
            return;
        }

        for(int i = 0; i < 9; i++)
        {
            if(!mc.player.inventory.getStack(i).isEmpty()) continue;

            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + i, getStack()));
            ChatUtils.info("Item created.");
            return;
        }

        ChatUtils.error("Please clear a slot in your hotbar.");
    }

    abstract ItemStack getStack();
}
