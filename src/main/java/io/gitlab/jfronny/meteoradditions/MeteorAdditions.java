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
        MeteorClient.EVENT_BUS.registerLambdaFactory("io.gitlab.jfronny.meteoradditions", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        Modules reg = Modules.get();
        reg.add(new AutoExtinguish());
        reg.add(new AutoSpectre());
        reg.add(new SpawnItems());
        reg.add(new TranslaterModule());
        Commands.get().add(new AdditionsItemGroupCommand());
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
