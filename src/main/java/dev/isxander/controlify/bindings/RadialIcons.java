package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public final class RadialIcons {
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static final ResourceLocation EMPTY = CUtil.rl("empty");
    public static final ResourceLocation FABRIC_ICON = CUtil.rl("fabric-resource-loader-v0", "icon.png");

    private static Map<ResourceLocation, RadialIcon> icons = null;
    private static Queue<Runnable> deferredRegistrations = new ArrayDeque<>();

    public static Map<ResourceLocation, RadialIcon> getIcons() {
        if (icons == null) {
            icons = registerIcons();
            deferredRegistrations.forEach(Runnable::run);
            deferredRegistrations = null;
        }
        return icons;
    }

    public static void registerIcon(ResourceLocation location, RadialIcon icon) {
        if (icons == null) {
            deferredRegistrations.add(() -> registerIcon(location, icon));
            return;
        }
        icons.put(location, icon);
    }

    public static ResourceLocation getItem(Item item) {
        return prefixLocation("item", BuiltInRegistries.ITEM.getKey(item));
    }

    public static ResourceLocation getEffect(
            /*? if >1.20.4 {*/
            Holder<MobEffect> effectHolder
            /*?} else {*/
            /*MobEffect effect
            *//*?}*/
    ) {
        /*? if >1.20.4 {*/
        MobEffect effect = effectHolder.value();
        /*?}*/
        return prefixLocation("effect", BuiltInRegistries.MOB_EFFECT.getKey(effect));
    }

    private static void addItems(Map<ResourceLocation, RadialIcon> map) {
        BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
            ResourceKey<Item> key = entry.getKey();
            ItemStack stack = entry.getValue().getDefaultInstance();

            map.put(prefixLocation("item", key.location()), (graphics, x, y, tickDelta) -> {
                graphics.renderItem(stack, x, y);
            });
        });
    }

    private static void addPotionEffects(Map<ResourceLocation, RadialIcon> map) {
        MobEffectTextureManager mobEffectTextureManager = minecraft.getMobEffectTextures();

        BuiltInRegistries.MOB_EFFECT.entrySet().forEach(entry -> {
            ResourceKey<MobEffect> key = entry.getKey();

            /*? if >1.20.4 {*/
            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(entry.getValue());
            /*?} else {*/
            /*MobEffect effect = entry.getValue();
            *//*?}*/

            TextureAtlasSprite sprite = mobEffectTextureManager.get(effect);

            if (sprite != null) {
                map.put(prefixLocation("effect", key.location()), (graphics, x, y, tickDelta) -> {
                    if (sprite.atlasLocation() != null) { // weird ass needed for some mods
                        graphics.pose().pushPose();
                        graphics.pose().translate(x, y, 0);
                        graphics.pose().scale(0.88f, 0.88f, 1f);

                        graphics.blit(0, 0, 0, 18, 18, sprite);

                        graphics.pose().popPose();
                    }
                });
            }
        });
    }

    private static ResourceLocation prefixLocation(String prefix, ResourceLocation location) {
        return CUtil.rl(location.getNamespace(), prefix + "/" + location.getPath());
    }

    private static Map<ResourceLocation, RadialIcon> registerIcons() {
        Map<ResourceLocation, RadialIcon> map = new Object2ObjectOpenHashMap<>();

        map.put(EMPTY, (graphics, x, y, tickDelta) -> {});
        map.put(FABRIC_ICON, (graphics, x, y, tickDelta) -> {
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(0.5f, 0.5f, 1f);
            graphics.blit(FABRIC_ICON, 0, 0, 0, 0, 32, 32, 32, 32);
            graphics.pose().popPose();
        });
        addItems(map);
        addPotionEffects(map);

        return map;
    }
}
