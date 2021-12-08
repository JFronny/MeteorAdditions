package io.gitlab.jfronny.meteoradditions.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;

import java.util.List;

public class AdditionsItemGroup {
    public static void add(List<ItemStack> stacksForDisplay) {
        {
            ItemStack stack = new ItemStack(Items.CHEST);
            NbtCompound nbtCompound = new NbtCompound();
            NbtList nbtList = new NbtList();
            for(int i = 0; i < 40000; i++)
                nbtList.add(new NbtList());
            nbtCompound.put("nothingsuspicioushere", nbtList);
            stack.setNbt(nbtCompound);
            stack.setCustomName(new LiteralText("Chest of NBT"));
            stacksForDisplay.add(stack);
        }
        {
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
            stacksForDisplay.add(stack);
        }
        {
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
            stacksForDisplay.add(stack);
        }
        {
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
            stacksForDisplay.add(stack);
        }
    }

    private static void addEnchant(NbtList tag, String id) {
        addEnchant(tag, id, Short.MAX_VALUE);
    }

    private static void addEnchant(NbtList tag, String id, short v) {
        NbtCompound enchant = new NbtCompound();
        enchant.putShort("lvl", v);
        enchant.putString("id", id);
        tag.add(enchant);
    }
}
