package io.gitlab.jfronny.meteoradditions.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.*;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.stream.Stream;

public class CustomWrapperLookup implements RegistryWrapper.WrapperLookup {
    @Override
    public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
        return getRegistryManager().stream().flatMap(DynamicRegistryManager::streamAllRegistryKeys);
    }

    @Override
    public <T> Optional<? extends RegistryWrapper.Impl<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef) {
        return getRegistryManager().flatMap(s -> s.getOptional(registryRef));
    }

    private Optional<DynamicRegistryManager> getRegistryManager() {
        return Optional.ofNullable(MinecraftClient.getInstance().world)
                .map(World::getRegistryManager);
    }
}
