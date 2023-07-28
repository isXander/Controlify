package dev.isxander.controlify.bindings;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class RadialIcons {
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static final ResourceLocation EMPTY = new ResourceLocation("controlify", "empty");
    public static final ResourceLocation FABRIC_ICON = new ResourceLocation("fabricloader", "icon");

    private static final Map<ResourceLocation, Icon> icons = Util.make(() -> {
        Map<ResourceLocation, Icon> map = new HashMap<>();

        map.put(EMPTY, (graphics, x, y) -> {});
        map.put(FABRIC_ICON, ((graphics, x, y) -> {
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(0.5f, 0.5f, 1f);
            graphics.blit(FABRIC_ICON, 0, 0, 0, 0, 32, 32);
            graphics.pose().popPose();
        }));
        addItems(map);
        addPotionEffects(map);

        return map;
    });

    public static Map<ResourceLocation, Icon> getIcons() {
        return icons;
    }

    public static ResourceLocation getItem(Item item) {
        return prefixLocation("item", BuiltInRegistries.ITEM.getKey(item));
    }

    public static ResourceLocation getEffect(MobEffect effect) {
        return prefixLocation("effect", BuiltInRegistries.MOB_EFFECT.getKey(effect));
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
