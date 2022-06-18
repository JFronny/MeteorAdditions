package io.gitlab.jfronny.meteoradditions.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

public class CustomItemStringReader {
    public static ItemStack read(String desc) throws ItemSyntaxException {
        StringReader reader = new StringReader(desc);
        Identifier identifier = readIdentifier(reader);
        Item item = Registry.ITEM.getOrEmpty(identifier).orElseThrow(() -> new ItemSyntaxException("Invalid item ID: " + identifier));
        ItemStack stack = new ItemStack(item, 1);
        while (reader.canRead()) {
            switch (reader.read()) {
                case '{' -> {
                    try {
                        reader.setCursor(reader.getCursor() - 1);
                        stack.setNbt(new StringNbtReader(reader).parseCompound());
                    } catch (CommandSyntaxException e) {
                        throw new ItemSyntaxException("Could not parse NBT", e);
                    }
                }
                case '@' -> {
                    String special = readString(reader);
                    switch (special) {
                        case "bloat" -> {
                            NbtCompound nbtCompound = new NbtCompound();
                            NbtList nbtList = new NbtList();
                            for (int i = 0; i < 40000; i++)
                                nbtList.add(new NbtList());
                            nbtCompound.put("nothingsuspicioushere", nbtList);
                            stack.setNbt(nbtCompound);
                        }
                        default -> throw new ItemSyntaxException("Could not resolve special NBT: " + special);
                    }
                }
                case '#' -> stack.setCustomName(Text.literal(readStringLiteral(reader)));
                case '$' -> stack.setCount(readInt(reader));
            }
        }
        return stack;
    }

    private static Identifier readIdentifier(StringReader reader) throws ItemSyntaxException {
        String string = readString(reader);
        try {
            return new Identifier(string);
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
