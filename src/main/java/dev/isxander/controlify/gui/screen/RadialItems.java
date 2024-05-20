package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.RadialIcons;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class RadialItems {
    public static final RadialMenuScreen.RadialItem EMPTY_ACTION = new RadialItemRecord(Component.empty(), RadialIcon.EMPTY, () -> false, RadialIcons.EMPTY);

    public static RadialMenuScreen.RadialItem[] createBindings(ControllerEntity controller) {
        RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[8];

        for (int i = 0; i < 8; i++) {
            ResourceLocation bindingId = controller.input().orElseThrow().confObj().radialActions[i];

            items[i] = getItemForBinding(bindingId, controller);
        }

        return items;
    }

    public static RadialMenuScreen.RadialItem[] createGameModes() {
        RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[4];

        items[0] = new GameModeItem(GameType.CREATIVE);
        items[1] = new GameModeItem(GameType.SURVIVAL);
        items[2] = new GameModeItem(GameType.ADVENTURE);
        items[3] = new GameModeItem(GameType.SPECTATOR);

        return items;
    }

    public static RadialMenuScreen.RadialItem[] createHotbarSave() {
        Minecraft mc = Minecraft.getInstance();
        RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[9];

        for (int i = 0; i < 9; i++) {
            int j = i;
            items[i] = new RadialItemRecord(
                    Component.translatable("controlify.radial.hotbar", Component.literal(Integer.toString(j + 1))),
                    getIconForHotbar(i, true),
                    () -> {
                        CreativeModeInventoryScreen.handleHotbarLoadOrSave(mc, j, false, true);
                        return true;
                    },
                    new ResourceLocation("controlify", "hotbar_save/" + j)
            );
        }

        return items;
    }

    public static RadialMenuScreen.RadialItem[] createHotbarLoad() {
        Minecraft mc = Minecraft.getInstance();
        RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[9];

        for (int i = 0; i < items.length; i++) {
            int j = i;
            items[i] = new RadialItemRecord(
                    Component.translatable("controlify.radial.hotbar", Component.literal(Integer.toString(j + 1))),
                    getIconForHotbar(i, true),
                    () -> {
                        CreativeModeInventoryScreen.handleHotbarLoadOrSave(mc, j, true, false);
                        return true;
                    },
                    new ResourceLocation("controlify", "hotbar_load/" + j)
            );
        }

        return items;
    }

    public static RadialMenuScreen.RadialItem[] createHotbarItemSelect() {
        Minecraft mc = Minecraft.getInstance();
        RadialMenuScreen.RadialItem[] items = new RadialMenuScreen.RadialItem[9];

        for (int i = 0; i < items.length; i++) {
            int j = i;
            items[i] = new RadialItemRecord(
                    Component.translatable("controlify.radial.hotbar", Component.literal(Integer.toString(j + 1))),
                    (graphics, x, y, tickDelta) -> {
                        graphics.renderItem(mc.player.getInventory().getItem(j), x, y);
                    },
                    () -> {
                        mc.player.getInventory().selected = j;
                        return true;
                    },
                    new ResourceLocation("controlify", "hotbar_item_select/" + j)
            );
        }

        return items;
    }

    private static RadialIcon getIconForHotbar(int hotbarIndex, boolean showNumbers) {
        Minecraft mc = Minecraft.getInstance();
        Hotbar hotbar = mc.getHotbarManager().get(hotbarIndex);

        /*? if >1.20.4 {*/
        List<ItemStack> hotbarItems = hotbar.load(mc.player.registryAccess());
        /*? } else {*//*
        List<ItemStack> hotbarItems = hotbar;
        *//*?}*/

        for (int i = 0; i < 9; i++) {
            ItemStack stack = hotbarItems.get(i);

            if (!stack.is(Items.AIR)) {
                return (graphics, x, y, tickDelta) -> {
                    graphics.renderItem(stack, x, y);

                    if (showNumbers) {
                        graphics.pose().pushPose();
                        graphics.pose().translate(0, 0, 1000);
                        graphics.drawString(mc.font, Integer.toString(hotbarIndex + 1), x, y, -1);
                        graphics.pose().popPose();
                    }
                };
            }
        }

        return (graphics, x, y, tickDelta) -> {
            if (showNumbers) {
                graphics.drawString(mc.font, Integer.toString(hotbarIndex + 1), x, y, -1);
            }
        };
    }

    private static RadialMenuScreen.RadialItem getItemForBinding(ResourceLocation id, ControllerEntity controller) {
        InputBinding binding = controller.input().orElseThrow().getBinding(id);

        if (binding == null || binding.radialIcon().isEmpty()) {
            CUtil.LOGGER.warn("Binding {} does not exist or is not a radial candidate", binding);
            return EMPTY_ACTION;
        }

        RadialIcon icon = RadialIcons.getIcons().get(binding.radialIcon().get());
        return new RadialItemRecord(
                binding.name(),
                icon,
                () -> {
                    binding.fakePress();
                    return true;
                },
                id
        );
    }

    private record RadialItemRecord(Component name, RadialIcon icon, Supplier<Boolean> action, ResourceLocation id) implements RadialMenuScreen.RadialItem {
        @Override
        public boolean playAction() {
            return action.get();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof RadialItemRecord record) {
                return id.equals(record.id);
            }
            return false;
        }
    }

    private static class GameModeItem implements RadialMenuScreen.RadialItem {
        private final GameType gameType;
        private final Component name;
        private final RadialIcon icon;
        private final String command;

        public GameModeItem(GameType gameType) {
            this.gameType = gameType;
            this.name = gameType.getShortDisplayName();
            ResourceLocation iconId = switch (gameType) {
                case CREATIVE -> RadialIcons.getItem(Items.GRASS_BLOCK);
                case SURVIVAL -> RadialIcons.getItem(Items.IRON_SWORD);
                case ADVENTURE -> RadialIcons.getItem(Items.MAP);
                case SPECTATOR -> RadialIcons.getItem(Items.ENDER_EYE);
            };
            this.icon = RadialIcons.getIcons().get(iconId);
            this.command = switch (gameType) {
                case CREATIVE -> "gamemode creative";
                case SURVIVAL -> "gamemode survival";
                case ADVENTURE -> "gamemode adventure";
                case SPECTATOR -> "gamemode spectator";
            };
        }

        @Override
        public Component name() {
            return name;
        }

        @Override
        public RadialIcon icon() {
            return icon;
        }

        @Override
        public boolean playAction() {
            Minecraft client = Minecraft.getInstance();
            if (client.gameMode != null && client.player != null) {
                if (client.player.hasPermissions(2) && client.gameMode.getPlayerMode() != gameType) {
                    client.player.connection.sendUnsignedCommand(command);
                    return true;
                }
            }

            return false;
        }
    }

    public static class BindingEditMode implements RadialMenuScreen.EditMode {

        private final ControllerEntity controller;

        public BindingEditMode(ControllerEntity controller) {
            this.controller = controller;
        }

        @Override
        public void setRadialItem(int index, RadialMenuScreen.RadialItem item) {
            controller.input().orElseThrow().confObj().radialActions[index] = ((RadialItemRecord) item).id();
        }

        @Override
        public List<RadialMenuScreen.RadialItem> getEditCandidates() {
            List<RadialMenuScreen.RadialItem> items = new ArrayList<>();

            controller.input().orElseThrow().getAllBindings().forEach(binding -> {
                binding.radialIcon().ifPresent(icon -> {
                    items.add(new RadialItemRecord(binding.name(), RadialIcons.getIcons().get(icon), () -> false, binding.id()));
                });
            });

            return items;
        }
    }
}
