package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.meteoradditions.MeteorAdditions;
import io.gitlab.jfronny.meteoradditions.util.CustomItemStringReader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.*;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AdditionsItemGroup {
    private static final String DEFAULT_ITEMS = """
            minecraft:chest@bloat#Chest of NBT
            minecraft:splash_potion{CustomPotionEffects:[{Amplifier:125,Duration:2000,Id:6}]}#\u00a7rSplash Potion of \u00a74\u00a7lINSTANT DEATH
            minecraft:netherite_sword{Enchantments:[{id:"minecraft:sharpness", lvl:32767}, {id:"minecraft:knockback", lvl:32767}, {id:"minecraft:fire_aspect", lvl:32767}, {id:"minecraft:looting", lvl:10}, {id:"minecraft:sweeping", lvl:3}, {id:"minecraft:unbreaking", lvl:32767}, {id:"minecraft:mending", lvl:1}, {id:"minecraft:vanishing_curse", lvl:1}]}#Bonk
            minecraft:splash_potion{CustomPotionEffects:[{Amplifier:2147483647, Duration:2147483647, Id:1}, {Amplifier:2147483647, Duration:2147483647, Id:2}, {Amplifier:2147483647, Duration:2147483647, Id:3}, {Amplifier:2147483647, Duration:2147483647, Id:4}, {Amplifier:2147483647, Duration:2147483647, Id:5}, {Amplifier:2147483647, Duration:2147483647, Id:6}, {Amplifier:2147483647, Duration:2147483647, Id:7}, {Amplifier:2147483647, Duration:2147483647, Id:8}, {Amplifier:2147483647, Duration:2147483647, Id:9}, {Amplifier:2147483647, Duration:2147483647, Id:10}, {Amplifier:2147483647, Duration:2147483647, Id:11}, {Amplifier:2147483647, Duration:2147483647, Id:12}, {Amplifier:2147483647, Duration:2147483647, Id:13}, {Amplifier:2147483647, Duration:2147483647, Id:14}, {Amplifier:2147483647, Duration:2147483647, Id:15}, {Amplifier:2147483647, Duration:2147483647, Id:16}, {Amplifier:2147483647, Duration:2147483647, Id:17}, {Amplifier:2147483647, Duration:2147483647, Id:18}, {Amplifier:2147483647, Duration:2147483647, Id:19}, {Amplifier:2147483647, Duration:2147483647, Id:20}, {Amplifier:2147483647, Duration:2147483647, Id:21}, {Amplifier:2147483647, Duration:2147483647, Id:22}, {Amplifier:2147483647, Duration:2147483647, Id:23}]}#\u00a7rSplash Potion of Trolling
            """;
    private static final Path LIST_FILE = FabricLoader.getInstance().getGameDir().resolve("meteor-client").resolve("additions_items.txt");

    public static void register(FeatureSet enabledFeatures, ItemGroup.Entries entries, boolean operatorEnabled) {
        try {
            for (String s : read()) {
                s = s.trim();
                if (s.isEmpty()) continue;
                try {
                    entries.add(CustomItemStringReader.read(s));
                } catch (CustomItemStringReader.ItemSyntaxException e) {
                    MeteorAdditions.LOG.error("Could not parse item for additions group (\"" + s + "\")", e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String addItem(ItemStack stack) throws IOException {
        StringBuilder sb = new StringBuilder(stack.getItem().toString());
        if (stack.getNbt() != null) sb.append(stack.getNbt());
        if (stack.getCount() != 1) sb.append('$').append(stack.getCount());
        List<String> newItems = new ArrayList<>(read());
        newItems.add(sb.toString());
        Files.write(LIST_FILE, newItems);
        return sb.toString();
    }

    private static List<String> read() throws IOException {
        if (!Files.exists(LIST_FILE)) {
            Files.writeString(LIST_FILE, DEFAULT_ITEMS);
        }
        return Files.readAllLines(LIST_FILE);
    }
}
