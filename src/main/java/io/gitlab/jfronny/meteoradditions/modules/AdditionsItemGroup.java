package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.meteoradditions.MeteorAdditions;
import io.gitlab.jfronny.meteoradditions.util.CustomItemStringReader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdditionsItemGroup {
    private static final String DEFAULT_ITEMS = """
            minecraft:chest@bloat#Chest of NBT
            minecraft:netherite_sword{enchantments:{"minecraft:sharpness": 255, "minecraft:knockback": 255, "minecraft:fire_aspect": 255, "minecraft:looting": 10, "minecraft:sweeping_edge": 3, "minecraft:unbreaking": 255, "minecraft:mending": 1, "minecraft:vanishing_curse": 1}}#Bonk
            minecraft:splash_potion{potion_contents:{custom_effects:[{amplifier:125,duration:2000,id:"instant_health"}]}}#§rSplash Potion of §4§lINSTANT DEATH
            minecraft:splash_potion@all_effects#§rSplash Potion of Trolling
            """;
    private static final Path LIST_FILE = FabricLoader.getInstance().getGameDir().resolve("meteor-client").resolve("additions_items.txt");

    public static void register(ItemGroup.DisplayContext displayContext, ItemGroup.Entries entries) {
        try {
            for (String s : read()) {
                s = s.trim();
                if (s.isEmpty()) continue;
                try {
                    entries.add(CustomItemStringReader.read(s));
                } catch (CustomItemStringReader.ItemSyntaxException e) {
                    MeteorAdditions.LOG.error("Could not parse item for additions group (\"{}\")", s, e);
                }
            }
        } catch (IOException e) {
            MeteorAdditions.LOG.error("Could not read items for additions group", e);
        }
    }

    public static Optional<String> addItem(ItemStack stack) throws IOException {
        String st;
        try {
            st = CustomItemStringReader.write(stack);
        } catch (CustomItemStringReader.ItemSyntaxException e) {
            MeteorAdditions.LOG.error("Could not write item", e);
            return Optional.empty();
        }
        List<String> newItems = new ArrayList<>(read());
        newItems.add(st);
        Files.write(LIST_FILE, newItems);
        return Optional.of(st);
    }

    private static List<String> read() throws IOException {
        if (!Files.exists(LIST_FILE)) {
            Files.writeString(LIST_FILE, DEFAULT_ITEMS);
        }
        return Files.readAllLines(LIST_FILE);
    }
}
