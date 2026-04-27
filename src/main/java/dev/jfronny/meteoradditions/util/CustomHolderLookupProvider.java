package dev.jfronny.meteoradditions.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.stream.Stream;

public class CustomHolderLookupProvider implements HolderLookup.Provider {
    @Override
    public @NonNull Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
        return getRegistryManager().stream().flatMap(RegistryAccess::listRegistryKeys);
    }

    @Override
    public <T> @NonNull Optional<? extends HolderLookup.RegistryLookup<T>> lookup(@NonNull ResourceKey<? extends Registry<? extends T>> key) {
        return getRegistryManager().flatMap(s -> s.lookup(key));
    }

    private Optional<RegistryAccess> getRegistryManager() {
        return Optional.ofNullable(Minecraft.getInstance().level)
                .map(Level::registryAccess);
    }
}
