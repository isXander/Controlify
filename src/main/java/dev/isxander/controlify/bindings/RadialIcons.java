package dev.isxander.controlify.bindings;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class RadialIcons {
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static final ResourceLocation EMPTY = new ResourceLocation("controlify", "empty");

    private static final Map<ResourceLocation, Icon> icons = Util.make(() -> {
        Map<ResourceLocation, Icon> map = new HashMap<>();

        map.put(EMPTY, (graphics, x, y) -> {});
        addItems(map);
        addPotionEffects(map);

        return map;
    });

    public static Map<ResourceLocation, Icon> getIcons() {
        return icons;
    }

    private static void addItems(Map<ResourceLocation, Icon> map) {
        BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
            ResourceKey<Item> key = entry.getKey();
            ItemStack stack = entry.getValue().getDefaultInstance();

            map.put(prefixLocation("item", key.location()), (graphics, x, y) -> {
                graphics.renderItem(stack, x, y);
            });
        });
    }

    private static void addPotionEffects(Map<ResourceLocation, Icon> map) {
        MobEffectTextureManager mobEffectTextureManager = minecraft.getMobEffectTextures();

        BuiltInRegistries.MOB_EFFECT.entrySet().forEach(entry -> {
            ResourceKey<MobEffect> key = entry.getKey();
            MobEffect effect = entry.getValue();

            TextureAtlasSprite sprite = mobEffectTextureManager.get(effect);
            map.put(prefixLocation("effect", key.location()), (graphics, x, y) -> {
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(0.88f, 0.88f, 1f);

                graphics.blit(0, 0, 0, 18, 18, sprite);

                graphics.pose().popPose();
            });
        });
    }

    private static ResourceLocation prefixLocation(String prefix, ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), prefix + "/" + location.getPath());
    }

    @FunctionalInterface
    public interface Icon {
        void draw(GuiGraphics graphics, int x, int y);
    }
}
