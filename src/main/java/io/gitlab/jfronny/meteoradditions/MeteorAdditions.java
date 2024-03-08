package io.gitlab.jfronny.meteoradditions;

import io.gitlab.jfronny.meteoradditions.modules.*;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MeteorAdditions extends MeteorAddon {
    public static final String MOD_ID = "meteor-additions";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
    public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(MOD_ID, "general"));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.TNT))
                        .displayName(Text.translatable("meteor-additions.item-group"))
                .entries(AdditionsItemGroup::register)
                .build());

        // The formatting here is intentionally weird to not meet the regex filter used by anticope.ml
        // Since the feature list is generated from this file, we abuse the filter through comments instead.
        Modules reg = Modules.get();
        reg.add( new AutoSpectre() );
        reg.add( new SpawnItems() );
        reg.add( new TranslatorModule() );
        Commands.add( new AdditionsItemGroupCommand() );

        SearchKeywords.configure();

        // Features: (for parsing by anticope.ml)
        // add(new ModMenu integration for MeteorClient())
        // add(new "Servers" option in the multiplayer menu with various tools including a server finder())
        // add(new "Spawn Items" module to create a lot of item entities in creative())
        // add(new Configurable Creative tab with several OP items, see README())
        // add(new AutoSpectre based on PR 1932())
        // add(new Translator module for chat translations())
        // add(new Keywords for module search())
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
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(ModContainer::getMetadata)
                .map(s -> s.getCustomValue("github:sha"))
                .map(CustomValue::getAsString)
                .flatMap(s -> s.isEmpty() ? Optional.empty() : Optional.of(s))
                .orElse(null);
    }
}
