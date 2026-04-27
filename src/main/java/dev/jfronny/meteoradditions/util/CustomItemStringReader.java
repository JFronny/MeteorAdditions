package dev.jfronny.meteoradditions.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.*;
import net.minecraft.IdentifierException;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public class CustomItemStringReader {
    public static String write(ItemStack stack) throws ItemSyntaxException {
        HolderLookup.Provider registries = Commands.createValidationContext(VanillaRegistries.createLookup());
        DynamicOps<Tag> nbtOps = registries.createSerializationContext(NbtOps.INSTANCE);

        StringBuilder sb = new StringBuilder(stack.getItem().toString());
        DataResult<Tag> changes = DataComponentPatch.CODEC.encode(stack.getComponentsPatch(), nbtOps, nbtOps.empty());
        StringTagVisitor stringNbtWriter = new StringTagVisitor();
        changes.getOrThrow(e -> new ItemSyntaxException("Invalid item NBT: " + e)).accept(stringNbtWriter);
        sb.append(stringNbtWriter.build());
        if (stack.getCount() != 1) sb.append('$').append(stack.getCount());
        return sb.toString();
    }

    public static ItemStack read(String desc) throws ItemSyntaxException {
        HolderLookup.Provider registries = new CustomHolderLookupProvider();
        DynamicOps<Tag> nbtOps = registries.createSerializationContext(NbtOps.INSTANCE); // This ensures that caches are not used, which would cause a crash since reference equality is assumed in some vanilla code

        StringReader reader = new StringReader(desc);
        Identifier identifier = readIdentifier(reader);
        Item item = registries.lookupOrThrow(Registries.ITEM)
                .get(ResourceKey.create(Registries.ITEM, identifier))
                .orElseThrow(() -> new ItemSyntaxException("Invalid item ID: " + identifier))
                .value();
        ItemStack stack = new ItemStack(item, 1);
        while (reader.canRead()) {
            switch (reader.read()) {
                case '{' -> {
                    try {
                        reader.setCursor(reader.getCursor() - 1);
                        DataResult<Dynamic<?>> datum = Codec.PASSTHROUGH.parse(nbtOps, TagParser.parseCompoundAsArgument(reader));
                        datum = datum.map(dt -> RegistryOps.injectRegistryContext(dt, registries));
                        DataResult<DataComponentPatch> changes = datum.flatMap(DataComponentPatch.CODEC::parse);
                        stack.applyComponentsAndValidate(changes.getOrThrow(e -> new ItemSyntaxException("Invalid item NBT: " + e)));
                    } catch (CommandSyntaxException e) {
                        throw new ItemSyntaxException("Could not parse NBT", e);
                    }
                }
                case '@' -> {
                    String special = readString(reader);
                    switch (special) {
                        case "bloat" -> {
                            stack.set(DataComponents.CUSTOM_DATA, stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(compound -> {
                                ListTag nbtList = new ListTag();
                                for (int i = 0; i < 40000; i++)
                                    nbtList.add(new ListTag());
                                compound.put("nothingsuspicioushere", nbtList);
                            }));
                        }
                        case "all_effects" -> {
                            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
                                    Optional.empty(),
                                    Optional.of(0xfff800f8),
                                    BuiltInRegistries.MOB_EFFECT
                                            .registryKeySet()
                                            .stream()
                                            .map(BuiltInRegistries.MOB_EFFECT::get)
                                            .flatMap(Optional::stream)
                                            .map(s -> new MobEffectInstance(
                                                    s,
                                                    Integer.MAX_VALUE,
                                                    Integer.MAX_VALUE
                                            )).toList(),
                                    Optional.empty()
                            ));
                        }
                        default -> throw new ItemSyntaxException("Could not resolve special NBT: " + special);
                    }
                }
                case '#' -> stack.set(DataComponents.CUSTOM_NAME, Component.literal(readStringLiteral(reader)));
                case '$' -> stack.setCount(readInt(reader));
            }
        }
        return stack;
    }

    private static Identifier readIdentifier(StringReader reader) throws ItemSyntaxException {
        String string = readString(reader);
        try {
            return Identifier.parse(string);
        } catch (IdentifierException var4) {
            throw new ItemSyntaxException("Could not read identifier: " + string);
        }
    }

    private static int readInt(StringReader reader) throws ItemSyntaxException {
        try {
            return Integer.parseInt(readString(reader));
        }
        catch (NumberFormatException e) {
            throw new ItemSyntaxException("Could not parse int", e);
        }
    }

    private static String readStringLiteral(StringReader reader) throws ItemSyntaxException {
        int i = reader.getCursor();
        while (reader.canRead() && !isCharKey(reader.peek())) reader.skip();
        return reader.getString().substring(i, reader.getCursor());
    }

    private static String readString(StringReader reader) throws ItemSyntaxException {
        int i = reader.getCursor();
        while (reader.canRead() && isCharValid(reader.peek())) reader.skip();
        return reader.getString().substring(i, reader.getCursor());
    }

    private static boolean isCharValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
    }

    private static boolean isCharKey(char c) {
        return c == '{' || c == '}' || c == '@' || c == '$' || c == '#';
    }

    public static class ItemSyntaxException extends Exception {
        public ItemSyntaxException(String message) {
            super(message);
        }

        public ItemSyntaxException(String message, Throwable cause) {
            super(message, cause);
        }

        public ItemSyntaxException(Throwable cause) {
            super(cause);
        }

        protected ItemSyntaxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
