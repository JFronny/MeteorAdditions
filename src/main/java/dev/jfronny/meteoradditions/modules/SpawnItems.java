package dev.jfronny.meteoradditions.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    private final RandomSource random = RandomSource.create();

    public SpawnItems() {
        super(Categories.Misc, "spawn-items", "Spawns a lot of unwanted items");
    }

    @Override
    public void onActivate() {
        assert mc.player != null;
        if(!mc.player.getAbilities().instabuild) {
            error("Creative mode only.");
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        int stacks = speed.get();
        int size = stackSize.get();
        assert mc.player != null;
        for(int i = 9; i < 9 + stacks; i++) {
            mc.player.connection.send(new ServerboundSetCreativeModeSlotPacket(i, new ItemStack(BuiltInRegistries.ITEM.getRandom(random).map(Holder.Reference::value).orElse(Items.DIRT), size)));
        }

        for(int i = 9; i < 9 + stacks; i++) {
            InvUtils.drop().slot(i);
        }
    }
}
