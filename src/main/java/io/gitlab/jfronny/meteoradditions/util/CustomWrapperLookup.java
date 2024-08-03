package io.gitlab.jfronny.meteoradditions.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;
import java.util.stream.Stream;

public class CustomWrapperLookup implements RegistryWrapper.WrapperLookup {
    @Override
    public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
        return Registries.REGISTRIES.stream().map(Registry::getKey);
    }

    @Override
    public <T> Optional<RegistryWrapper.Impl<T>> getOptionalWrapper(RegistryKey<? extends Registry<? extends T>> registryRef) {
        return Optional.ofNullable(MinecraftClient.getInstance().player)
                .map(Entity::getRegistryManager)
                .flatMap(s -> s.getOptional(registryRef))
                .map(Registry::getReadOnlyWrapper);
    }
}
