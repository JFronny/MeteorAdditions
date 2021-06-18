package io.gitlab.jfronny.meteoradditions.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.*;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.concurrent.atomic.AtomicInteger;

public class AutoExtinguish extends Module {
    private static final StatusEffect FIRE_RESISTANCE = Registry.STATUS_EFFECT.get(new Identifier("fire_resistance"));

    public AutoExtinguish() {
        super(Categories.World, "auto-extinguish", "Automatically extinguishes fire around you");
    }

    private final SettingGroup dgWorld = settings.createGroup("World");
    private final SettingGroup sgSelf = settings.createGroup("Self");

    private final Setting<Boolean> enableWorldExtinguish = dgWorld.add(new BoolSetting.Builder()
            .name("enable")
            .description("Removes fire blocks in the world around you")
            .defaultValue(true)
            .build()
    );
    private final Setting<Integer> horizontalRadius = dgWorld.add(new IntSetting.Builder()
            .name("horizontal-radius")
            .description("Horizontal radius in which to search for fire.")
            .defaultValue(4)
            .min(0)
            .sliderMax(6)
            .visible(enableWorldExtinguish::get)
            .build()
    );
    private final Setting<Integer> verticalRadius = dgWorld.add(new IntSetting.Builder()
            .name("vertical-radius")
            .description("Vertical radius in which to search for fire.")
            .defaultValue(4)
            .min(0)
            .sliderMax(6)
            .visible(enableWorldExtinguish::get)
            .build()
    );
    private final Setting<Integer> maxBlockPerTick = dgWorld.add(new IntSetting.Builder()
            .name("block-per-tick")
            .description("Maximum amount of Blocks to extinguish per tick.")
            .defaultValue(5)
            .min(1)
            .sliderMax(50)
            .visible(enableWorldExtinguish::get)
            .build()
    );

    private final Setting<Boolean> enableSelfExtinguish = sgSelf.add(new BoolSetting.Builder()
            .name("enable")
            .description("Automatically places water when you are on fire (and don't have fire resistance).")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> center = sgSelf.add(new BoolSetting.Builder()
            .name("center")
            .description("Automatically centers you when placing water.")
            .defaultValue(false)
            .visible(enableSelfExtinguish::get)
            .build()
    );
    private final Setting<Boolean> onGround = sgSelf.add(new BoolSetting.Builder()
            .name("on-ground")
            .description("Only place when you are on ground.")
            .defaultValue(false)
            .visible(enableSelfExtinguish::get)
            .build()
    );


    private boolean hasPlacedWater = false;
    private boolean doesWaterBucketWork = true;


    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world.getDimension().isRespawnAnchorWorking()) {
            if (doesWaterBucketWork) {
                ChatUtils.warning("Water Buckets don't work in this dimension!");
                doesWaterBucketWork = false;

            }
        } else {
            if (!doesWaterBucketWork) {
                ChatUtils.warning("Enabled Water Buckets!");
                doesWaterBucketWork = true;
            }
        }

        if (enableSelfExtinguish.get() && doesWaterBucketWork && (!onGround.get() || mc.player.isOnGround())) {
            if (hasPlacedWater) {
                final FindItemResult slot = InvUtils.find(Items.BUCKET);
                place(slot);
                hasPlacedWater = false;

            } else if (!mc.player.hasStatusEffect(FIRE_RESISTANCE) && mc.player.isOnFire()) {
                BlockPos blockPos = mc.player.getBlockPos();
                final FindItemResult slot = InvUtils.find(Items.WATER_BUCKET);
                if (mc.world.getBlockState(blockPos).getBlock() == Blocks.FIRE || mc.world.getBlockState(blockPos).getBlock() == Blocks.SOUL_FIRE) {
                    float yaw = mc.gameRenderer.getCamera().getYaw() % 360;
                    float pitch = mc.gameRenderer.getCamera().getPitch() % 360;
                    if (center.get()) {
                        PlayerUtils.centerPlayer();
                    }
                    Rotations.rotate(yaw, 90);
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));

                    Rotations.rotate(yaw, pitch);
                }
                place(slot);
                hasPlacedWater = true;
            }
        }

        if (enableWorldExtinguish.get()) {
            AtomicInteger blocksPerTick = new AtomicInteger();
            BlockIterator.register(horizontalRadius.get(), verticalRadius.get(), (blockPos, blockState) -> {
                if (blocksPerTick.get() <= maxBlockPerTick.get()) {
                    if (blockState.getBlock() == Blocks.FIRE || mc.world.getBlockState(blockPos).getBlock() == Blocks.SOUL_FIRE) {
                        extinguishFire(blockPos);
                        blocksPerTick.getAndIncrement();
                    }
                }
            });
        }
    }

    private void place(FindItemResult slot) {
        if (slot.found() && slot.isHotbar()) {
            final int preSlot = mc.player.getInventory().selectedSlot;
            if (center.get()) {
                PlayerUtils.centerPlayer();
            }
            mc.player.getInventory().selectedSlot = slot.getSlot();
            float yaw = mc.gameRenderer.getCamera().getYaw() % 360;
            float pitch = mc.gameRenderer.getCamera().getPitch() % 360;

            Rotations.rotate(yaw, 90);
            mc.interactionManager.interactItem(mc.player, mc.player.world, Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = preSlot;
            Rotations.rotate(yaw, pitch);
        }
    }

    private void extinguishFire(BlockPos blockPos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, net.minecraft.util.math.Direction.UP));
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, net.minecraft.util.math.Direction.UP));
    }
}
