package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.config.GlobalSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.driver.SDL3NativesManager;
import dev.isxander.controlify.gui.controllers.FormattableStringController;
import dev.isxander.controlify.reacharound.ReachAroundMode;
import dev.isxander.controlify.server.ServerPolicies;
import dev.isxander.controlify.server.ServerPolicy;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.DebugDump;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicReference;

public class GlobalSettingsScreenFactory {
    public static Screen createGlobalSettingsScreen(Screen parent) {
        var globalSettings = Controlify.instance().config().globalSettings();
        AtomicReference<ListOption<String>> whitelist = new AtomicReference<>();
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("controlify.gui.global_settings.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("controlify.gui.global_settings.title"))
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("controlify.gui.open_issue_tracker"))
                                .action((screen, button) -> Util.getPlatform().openUri("https://github.com/isxander/controlify/issues"))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("controlify.gui.natives"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.load_vibration_natives"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.load_vibration_natives.tooltip"))
                                                .text(Component.translatable("controlify.gui.load_vibration_natives.tooltip.warning").withStyle(ChatFormatting.RED))
                                                .build())
                                        .binding(true, () -> globalSettings.loadVibrationNatives, v -> globalSettings.loadVibrationNatives = v)
                                        .controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter())
                                        .flag(OptionFlag.GAME_RESTART)
                                        .available(SDL3NativesManager.isSupportedOnThisPlatform())
                                        .build())
                                .option(Option.<String>createBuilder()
                                        .name(Component.translatable("controlify.gui.custom_natives_path"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.custom_natives_path.tooltip"))
                                                .text(Component.translatable("controlify.gui.custom_natives_path.tooltip.warning").withStyle(ChatFormatting.RED))
                                                .build())
                                        .binding("", () -> globalSettings.customVibrationNativesPath, v -> globalSettings.customVibrationNativesPath = v)
                                        .customController(opt -> new FormattableStringController(opt, s -> {
                                            if (s.isEmpty())
                                                return Component.translatable("controlify.gui.custom_natives_path.none");
                                            return Component.literal(s);
                                        }))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("controlify.gui.server_options"))
                                .option(Option.<ReachAroundMode>createBuilder()
                                        .name(Component.translatable("controlify.gui.reach_around"))
                                        .description(state -> OptionDescription.createBuilder()
                                                .webpImage(screenshot("reach-around-placement.webp"))
                                                .text(Component.translatable("controlify.gui.reach_around.tooltip"))
                                                .text(Component.translatable("controlify.gui.reach_around.tooltip.parity").withStyle(ChatFormatting.GRAY))
                                                .text(state == ReachAroundMode.EVERYWHERE ? Component.translatable("controlify.gui.reach_around.tooltip.warning").withStyle(ChatFormatting.RED) : Component.empty())
                                                .text(ServerPolicies.REACH_AROUND.get() != ServerPolicy.DISALLOWED ? Component.translatable("controlify.gui.server_controlled").withStyle(ChatFormatting.GOLD) : Component.empty())
                                                .build())
                                        .binding(GlobalSettings.DEFAULT.reachAround, () -> globalSettings.reachAround, v -> globalSettings.reachAround = v)
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(ReachAroundMode.class)
                                                .formatValue(mode -> switch (ServerPolicies.REACH_AROUND.get()) {
                                                    case UNSET, ALLOWED -> mode.getDisplayName();
                                                    case DISALLOWED -> CommonComponents.OPTION_OFF;
                                                }))
                                        .available(ServerPolicies.REACH_AROUND.get().isAllowed())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.allow_server_rumble"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.allow_server_rumble.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.DEFAULT.allowServerRumble, () -> globalSettings.allowServerRumble, v -> globalSettings.allowServerRumble = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .listener((opt, val) -> {
                                            ControlifyApi.get().getCurrentController()
                                                    .flatMap(ControllerEntity::rumble)
                                                    .ifPresent(rumble -> rumble.rumbleManager().clearEffects());
                                        })
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.keyboard_movement"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.keyboard_movement.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.DEFAULT.alwaysKeyboardMovement, () -> globalSettings.alwaysKeyboardMovement, v -> globalSettings.alwaysKeyboardMovement = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Component.translatable("controlify.gui.add_server_to_keyboard_move_whitelist"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.add_server_to_keyboard_move_whitelist.tooltip"))
                                                .build())
                                        .action((screen, button) -> {
                                            ServerData server = Minecraft.getInstance().getCurrentServer();
                                            if (server != null) {
                                                whitelist.get().insertNewEntry().requestSet(server.ip);
                                            }
                                        })
                                        .available(Minecraft.getInstance().getCurrentServer() != null)
                                        .build())
                                .build())
                        .group(Util.make(() -> {
                            var list = ListOption.<String>createBuilder()
                                    .name(Component.translatable("controlify.gui.keyboard_movement_whitelist"))
                                    .description(OptionDescription.createBuilder()
                                            .text(Component.translatable("controlify.gui.keyboard_movement_whitelist.tooltip"))
                                            .build())
                                    .binding(GlobalSettings.DEFAULT.keyboardMovementWhitelist, () -> globalSettings.keyboardMovementWhitelist, v -> globalSettings.keyboardMovementWhitelist = v)
                                    .controller(StringControllerBuilder::create)
                                    .initial("Server IP here")
                                    .build();
                            whitelist.set(list);
                            return list;
                        }))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("controlify.gui.miscellaneous"))
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("controlify.gui.ingame_button_guide_scale"))
                                        .description(val  -> OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.ingame_button_guide_scale.tooltip"))
                                                .text(val != 1f ? Component.translatable("controlify.gui.ingame_button_guide_scale.tooltip.warning").withStyle(ChatFormatting.RED) : Component.empty())
                                                .build())
                                        .binding(GlobalSettings.DEFAULT.ingameButtonGuideScale, () -> globalSettings.ingameButtonGuideScale, v -> globalSettings.ingameButtonGuideScale = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.5f, 1.5f)
                                                .step(0.05f)
                                                .formatValue(v -> Component.literal(String.format("%.0f%%", v*100))))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.ui_sounds"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.ui_sounds.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.DEFAULT.uiSounds, () -> globalSettings.uiSounds, v -> globalSettings.uiSounds = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.out_of_focus_input"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.out_of_focus_input.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.DEFAULT.outOfFocusInput, () -> globalSettings.outOfFocusInput, v -> globalSettings.outOfFocusInput = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.notify_low_battery"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.notify_low_battery.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.DEFAULT.notifyLowBattery, () -> globalSettings.notifyLowBattery, v -> globalSettings.notifyLowBattery = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Component.translatable("controlify.gui.copy_debug_dump"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.copy_debug_dump.tooltip"))
                                                .build())
                                        .action((screen, btn) -> {
                                            String dump = DebugDump.dumpDebug();
                                            String formatted = """
                                                    Here's my Controlify debug dump
                                                    ```
                                                    %s
                                                    ```
                                                    """.formatted(dump).stripIndent();

                                            Minecraft.getInstance().keyboardHandler.setClipboard(formatted);
                                        })
                                        .build())
                                .build())
                        .build())
                .build().generateScreen(parent);
    }

    private static ResourceLocation screenshot(String filename) {
        return CUtil.rl("textures/screenshots/" + filename);
    }
}
