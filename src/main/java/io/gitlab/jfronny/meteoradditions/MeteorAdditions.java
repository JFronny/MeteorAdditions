package io.gitlab.jfronny.meteoradditions;

import io.gitlab.jfronny.meteoradditions.modules.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class MeteorAdditions extends MeteorAddon {
    public static final String MOD_ID = "meteor-additions";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "general"))
            .icon(() -> new ItemStack(Items.TNT))
            .appendItems(AdditionsItemGroup::register)
            .build();

    @Override
    public void onInitialize() {
        // The formatting here is intentionally weird to not meet the regex filter used by anticope.ml
        // Since the feature list is generated from this file, we abuse the filter through comments instead.
        Modules reg = Modules.get();
        reg.add( new AutoSpectre() );
        reg.add( new SpawnItems() );
        Commands.get().add( new AdditionsItemGroupCommand() );

        // Features: (for parsing by anticope.ml)
        // add(new ModMenu integration for MeteorClient())
        // add(new "Servers" option in the multiplayer menu with various tools including a server finder())
        // add(new "Spawn Items" module to create a lot of item entities in creative())
        // add(new Configurable Creative tab with several OP items, see README())
        // add(new AutoSpectre based on PR 1932())
        // add(new Translator module for chat translations())
    }

    @Override
    public String getPackage() {
        return "io.gitlab.jfronny.meteoradditions";
    }

    @Override
    public String getWebsite() {
        return "https://github.com/JFronny/MeteorAdditions";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("JFronny", "MeteorAdditions");
    }

    @Override
    public String getCommit() {
        String commit = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getCustomValue("github:sha").getAsString();
        return commit.isEmpty() ? null : commit;
    }
}
