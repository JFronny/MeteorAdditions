package io.gitlab.jfronny.meteoradditions.modules;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.util.registry.Registry;

import java.util.Random;

public class SpawnItems extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
            .name("speed")
            .description("The speed of drops. High speeds will cause major lag. Disable item rendering!")
            .defaultValue(1)
            .min(1)
            .max(36)
            .sliderMax(36)
            .build()
    );

    private final Setting<Integer> stackSize = sgGeneral.add(new IntSetting.Builder()
            .name("stack-size")
            .description("How many items to place in a stack.")
            .defaultValue(1)
            .min(1)
            .max(64)
            .sliderMax(64)
            .build()
    );

    private final Random random = new Random();

    public SpawnItems() {
        super(Categories.Misc, "spawn-items", "Spawns a lot of unwanted items");
    }

    @Override
    public void onActivate() {
        if(!mc.player.getAbilities().creativeMode)
        {
            ChatUtils.error("Creative mode only.");
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        int stacks = speed.get();
        int size = stackSize.get();
        for(int i = 9; i < 9 + stacks; i++) {
            mc.player.networkHandler.sendPacket(
                    new CreativeInventoryActionC2SPacket(i,
                            new ItemStack(Registry.ITEM.getRandom(random), size)));
        }

        for(int i = 9; i < 9 + stacks; i++) {
            InvUtils.drop().slot(i);
        }
    }
}
