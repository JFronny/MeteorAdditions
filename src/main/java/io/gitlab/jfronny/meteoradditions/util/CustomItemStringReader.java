package io.gitlab.jfronny.meteoradditions.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import io.gitlab.jfronny.commons.throwable.Coerce;
import io.gitlab.jfronny.commons.throwable.ThrowingFunction;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.nbt.visitor.StringNbtWriter;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.Optional;

public class CustomItemStringReader {
    private static final CustomWrapperLookup LOOKUP = new CustomWrapperLookup();
    private static final ThrowingFunction<NbtElement, ComponentChanges, ItemSyntaxException> parser = Coerce
            .<NbtElement, DataResult<ComponentChanges>, ItemSyntaxException>function(LOOKUP.getOps(NbtOps.INSTANCE).withParser(ComponentChanges.CODEC)::apply)
            .andThen(Coerce.function(result -> result.getOrThrow(e -> new ItemSyntaxException("Invalid item NBT: " + e))));
    private static final ThrowingFunction<ComponentChanges, NbtElement, ItemSyntaxException> encoder = Coerce
            .<ComponentChanges, DataResult<NbtElement>, ItemSyntaxException>function(LOOKUP.getOps(NbtOps.INSTANCE).withEncoder(ComponentChanges.CODEC)::apply)
            .andThen(Coerce.function(result -> result.getOrThrow(e -> new ItemSyntaxException("Invalid item NBT: " + e))));

    public static String write(ItemStack stack) throws ItemSyntaxException {
        StringBuilder sb = new StringBuilder(stack.getItem().toString());
        sb.append(new StringNbtWriter().apply(encoder.apply(stack.getComponentChanges())));
        if (stack.getCount() != 1) sb.append('$').append(stack.getCount());
        return sb.toString();
    }

    public static ItemStack read(String desc) throws ItemSyntaxException {
        StringReader reader = new StringReader(desc);
        Identifier identifier = readIdentifier(reader);
        Item item = Registries.ITEM.getOrEmpty(identifier).orElseThrow(() -> new ItemSyntaxException("Invalid item ID: " + identifier));
        ItemStack stack = new ItemStack(item, 1);
        while (reader.canRead()) {
            switch (reader.read()) {
                case '{' -> {
                    try {
                        reader.setCursor(reader.getCursor() - 1);
                        stack.applyChanges(parser.apply(new StringNbtReader(reader).parseCompound()));
                    } catch (CommandSyntaxException e) {
                        throw new ItemSyntaxException("Could not parse NBT", e);
                    }
                }
                case '@' -> {
                    String special = readString(reader);
                    switch (special) {
                        case "bloat" -> {
                            stack.set(DataComponentTypes.CUSTOM_DATA, stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).apply(compound -> {
                                NbtList nbtList = new NbtList();
                                for (int i = 0; i < 40000; i++)
                                    nbtList.add(new NbtList());
                                compound.put("nothingsuspicioushere", nbtList);
                            }));
                        }
                        case "all_effects" -> {
                            stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(
                                    Optional.empty(),
                                    Optional.of(-524040),
                                    Registries.STATUS_EFFECT
                                            .getKeys()
                                            .stream()
                                            .map(Registries.STATUS_EFFECT::getEntry)
                                            .flatMap(Optional::stream)
                                            .map(s -> new StatusEffectInstance(
                                                    s,
                                                    Integer.MAX_VALUE,
                                                    Integer.MAX_VALUE
                                            )).toList()
                            ));
                        }
                        default -> throw new ItemSyntaxException("Could not resolve special NBT: " + special);
                    }
                }
                case '#' -> stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(readStringLiteral(reader)));
                case '$' -> stack.setCount(readInt(reader));
            }
        }
        return stack;
    }

    private static Identifier readIdentifier(StringReader reader) throws ItemSyntaxException {
        String string = readString(reader);
        try {
            return Identifier.of(string);
        } catch (InvalidIdentifierException var4) {
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
