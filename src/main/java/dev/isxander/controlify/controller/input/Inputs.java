package dev.isxander.controlify.controller.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Inputs {
    private static final Map<SpriteCacheKey, Optional<ResourceLocation>> CACHED_SPRITES = new HashMap<>();

    private Inputs() {
    }

    public static Optional<ResourceLocation> getThemedSprite(ResourceLocation input, String theme) {
        return CACHED_SPRITES.computeIfAbsent(new SpriteCacheKey(input, theme), key -> {
            var spriteLocation = new ResourceLocation(input.getNamespace(), "inputs/" + theme + "/" + input.getPath());

            TextureAtlasSprite sprite = Minecraft.getInstance().getGuiSprites().getSprite(spriteLocation);
            if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                return Optional.empty();
            }

            return Optional.of(spriteLocation);
        });
    }

    public static MutableComponent getInputComponent(ResourceLocation input) {
        return Component.translatable("controlify.input." + input.getNamespace() + "." + input.getPath());
    }

    private record SpriteCacheKey(ResourceLocation input, String theme) {
    }
}
