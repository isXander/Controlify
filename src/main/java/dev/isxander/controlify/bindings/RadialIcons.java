package dev.isxander.controlify.bindings;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public final class RadialIcons {
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static final Identifier EMPTY = CUtil.rl("empty");
    private static final Identifier FABRIC_ICON = Identifier.fromNamespaceAndPath("fabric-resource-loader-v0", "icon.png");

    private static Map<Identifier, RadialIcon> icons = null;
    private static Queue<Runnable> deferredRegistrations = new ArrayDeque<>();

    public static Map<Identifier, RadialIcon> getIcons() {
        if (icons == null) {
            icons = registerIcons();
            deferredRegistrations.forEach(Runnable::run);
            deferredRegistrations = null;
        }
        return icons;
    }

    public static void registerIcon(Identifier location, RadialIcon icon) {
        if (icons == null) {
            deferredRegistrations.add(() -> registerIcon(location, icon));
            return;
        }
        icons.put(location, icon);
    }

    public static Identifier getItem(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).withPrefix("item/");
    }

    public static Identifier getEffect(Holder<MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.getKey(effect.value()).withPrefix("effect/");
    }

    private static void addItems(Map<Identifier, RadialIcon> map) {
        BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
            ResourceKey<Item> key = entry.getKey();
            ItemStack stack = entry.getValue().getDefaultInstance();

            map.put(key.identifier().withPrefix("item/"), (graphics, x, y, a) -> {
                graphics.item(stack, x, y);
            });
        });
    }

    private static void addPotionEffects(Map<Identifier, RadialIcon> map) {
        BuiltInRegistries.MOB_EFFECT.entrySet().forEach(entry -> {
            ResourceKey<MobEffect> key = entry.getKey();

            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(entry.getValue());

            Identifier sprite = Gui.getMobEffectSprite(effect);

            map.put(key.identifier().withPrefix("effect/"), (graphics, x, y, a) -> {
                var pose = graphics.pose().pushMatrix();
                pose.translate(x, y);
                pose.scale(0.88f, 0.88f);

                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        sprite,
                        0, 0,
                        18, 18
                );

                pose.popMatrix();
            });
        });
    }

    private static Map<Identifier, RadialIcon> registerIcons() {
        Map<Identifier, RadialIcon> map = new Object2ObjectOpenHashMap<>();
        final Identifier modLoaderIcon = getModLoaderIcon();

        map.put(EMPTY, (graphics, x, y, tickDelta) -> {});
        map.put(modLoaderIcon, (graphics, x, y, tickDelta) -> {
            var pose = graphics.pose().pushMatrix();
            pose.translate(x, y);
            pose.scale(0.5f, 0.5f);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    modLoaderIcon,
                    0, 0,
                    0, 0,
                    32, 32,
                    32, 32
            );
            pose.popMatrix();
        });
        addItems(map);
        addPotionEffects(map);

        return map;
    }
    
    public static @NotNull Identifier getModLoaderIcon() {
        //? if fabric {
        return FABRIC_ICON;
        //?} else {
        /*return getItem(net.minecraft.world.item.Items.BOOK);
        *///?}
    }
}
