package io.gitlab.jfronny.meteoradditions.modules;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.gitlab.jfronny.meteoradditions.MeteorAdditions;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.io.IOException;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class AdditionsItemGroupCommand extends Command {
    public AdditionsItemGroupCommand() {
        super("meteoradditions-item-group-add", "Add the held item to the MeteorAdditions item group", "item-group-add", "save-item");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            try {
                ItemStack stack = MeteorClient.mc.player.getInventory().getMainHandStack();
                if (stack.getItem().equals(Items.AIR)) {
                    error("You must hold an item to add it");
                    return SINGLE_SUCCESS;
                }
                info("Saved item: " + AdditionsItemGroup.addItem(stack));
            } catch (IOException e) {
                MeteorAdditions.LOG.error("Could not add item", e);
                error("Could not add item");
            }
            return SINGLE_SUCCESS;
        });
    }
}
