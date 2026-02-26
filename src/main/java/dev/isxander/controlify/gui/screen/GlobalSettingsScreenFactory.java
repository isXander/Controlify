package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.config.settings.GlobalSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.reacharound.ReachAroundMode;
import dev.isxander.controlify.server.ServerPolicies;
import dev.isxander.controlify.server.ServerPolicy;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.DebugDump;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalSettingsScreenFactory {
    public static Screen createGlobalSettingsScreen(Screen parent) {
        var globalSettings = Controlify.instance().config().getSettings().globalSettings();
        AtomicReference<ListOption<String>> whitelist = new AtomicReference<>();
        AtomicReference<Option<String>> controllerSelector = new AtomicReference<>();

        List<String> controllerUids = new ArrayList<>();
        controllerUids.add("");
        Controlify.instance().getControllerManager().ifPresent(cm -> {
            for (ControllerEntity c : cm.getConnectedControllers()) {
                controllerUids.add(c.uid());
            }
        });

        boolean is12106OrLater = /*? if >=1.21.6 {*/ true /*?} else {*/ /*false *//*?}*/;;

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("controlify.gui.global_settings.title"))
                .save(() -> Controlify.instance().config().saveSafely())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("controlify.gui.global_settings.title"))
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("controlify.gui.open_issue_tracker"))
                                .action((screen, button) -> Util.getPlatform().openUri("https://github.com/isxander/controlify/issues"))
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
                                                .text(ServerPolicies.REACH_AROUND.getPolicy() == ServerPolicy.DISALLOWED ? Component.translatable("controlify.gui.server_controlled").withStyle(ChatFormatting.GOLD) : Component.empty())
                                                .build())
                                        .binding(GlobalSettings.defaults().reachAround, () -> globalSettings.reachAround, v -> globalSettings.reachAround = v)
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(ReachAroundMode.class)
                                                .formatValue(mode -> switch (ServerPolicies.REACH_AROUND.getPolicy()) {
                                                    case UNSET, ALLOWED -> mode.getDisplayName();
                                                    case DISALLOWED -> CommonComponents.OPTION_OFF;
                                                }))
                                        .available(ServerPolicies.REACH_AROUND.get())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.allow_server_rumble"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.allow_server_rumble.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.defaults().allowServerRumble, () -> globalSettings.allowServerRumble, v -> globalSettings.allowServerRumble = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .addListener((opt, val) -> {
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
                                        .binding(GlobalSettings.defaults().alwaysKeyboardMovement, () -> globalSettings.alwaysKeyboardMovement, v -> globalSettings.alwaysKeyboardMovement = v)
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
                                    .binding(GlobalSettings.defaults().keyboardMovementWhitelist, () -> globalSettings.keyboardMovementWhitelist, v -> globalSettings.keyboardMovementWhitelist = v)
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
                                                .text(is12106OrLater ? Component.literal("This setting is currently broken on 1.21.6+").withStyle(ChatFormatting.RED) : Component.empty())
                                                .text(Component.translatable("controlify.gui.ingame_button_guide_scale.tooltip"))
                                                .text(val != 1f ? Component.translatable("controlify.gui.ingame_button_guide_scale.tooltip.warning").withStyle(ChatFormatting.RED) : Component.empty())
                                                .build())
                                        .binding(GlobalSettings.defaults().ingameButtonGuideScale, () -> is12106OrLater ? 1f : globalSettings.ingameButtonGuideScale, v -> globalSettings.ingameButtonGuideScale = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.5f, 1.5f)
                                                .step(0.05f)
                                                .formatValue(v -> Component.literal(String.format("%.0f%%", v*100))))
                                        .available(!is12106OrLater)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.ui_sounds"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.ui_sounds.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.defaults().extraUiSounds, () -> globalSettings.extraUiSounds, v -> globalSettings.extraUiSounds = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.out_of_focus_input"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.out_of_focus_input.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.defaults().outOfFocusInput, () -> globalSettings.outOfFocusInput, v -> globalSettings.outOfFocusInput = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.notify_low_battery"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.notify_low_battery.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.defaults().notifyLowBattery, () -> globalSettings.notifyLowBattery, v -> globalSettings.notifyLowBattery = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.mixed_input"))
                                        .description(OptionDescription.of(Component.translatable("controlify.gui.mixed_input.tooltip")))
                                        .binding(GlobalSettings.defaults().mixedInput, () -> globalSettings.mixedInput, v -> globalSettings.mixedInput = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.auto_switch_controllers"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.auto_switch_controllers.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.defaults().autoSwitchControllers, () -> globalSettings.autoSwitchControllers, v -> globalSettings.autoSwitchControllers = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .addListener((opt, event) -> {
                                            var selector = controllerSelector.get();
                                            if (selector != null) selector.setAvailable(!opt.pendingValue());
                                        })
                                        .build())
                                .option(Util.make(() -> {
                                    var opt = Option.<String>createBuilder()
                                            .name(Component.translatable("controlify.gui.current_controller"))
                                            .description(OptionDescription.createBuilder()
                                                    .text(Component.translatable("controlify.gui.current_controller.tooltip"))
                                                    .build())
                                            .binding(
                                                    "",
                                                    () -> Controlify.instance().getCurrentController().map(ControllerEntity::uid).orElse(""),
                                                    uid -> {
                                                        if (uid.isEmpty()) {
                                                            Controlify.instance().setCurrentController(null, true);
                                                        } else {
                                                            Controlify.instance().getControllerManager().ifPresent(cm ->
                                                                    cm.getConnectedControllers().stream()
                                                                            .filter(c -> c.uid().equals(uid))
                                                                            .findFirst()
                                                                            .ifPresent(c -> Controlify.instance().setCurrentController(c, true)));
                                                        }
                                                        globalSettings.preferredControllerUid = uid;
                                                    }
                                            )
                                            .controller(o -> CyclingListControllerBuilder.create(o)
                                                    .values(controllerUids)
                                                    .formatValue(uid -> {
                                                        if (uid.isEmpty()) return Component.translatable("controlify.gui.carousel.entry.keyboard_mouse");
                                                        return Controlify.instance().getControllerManager()
                                                                .map(cm -> cm.getConnectedControllers().stream()
                                                                        .filter(c -> c.uid().equals(uid))
                                                                        .findFirst()
                                                                        .map(c -> (Component) Component.literal(c.name()))
                                                                        .orElse(Component.literal(uid)))
                                                                .orElse(Component.literal(uid));
                                                    }))
                                            .available(!globalSettings.autoSwitchControllers)
                                            .build();
                                    controllerSelector.set(opt);
                                    return opt;
                                }))
                                .optionIf(SteamDeckUtil.IS_STEAM_DECK, Option.<Boolean>createBuilder()
                                        .name(Component.translatable("controlify.gui.use_enhanced_steam_deck_driver"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Component.translatable("controlify.gui.use_enhanced_steam_deck_driver.tooltip"))
                                                .build())
                                        .binding(GlobalSettings.defaults().useEnhancedSteamDeckDriver, () -> globalSettings.useEnhancedSteamDeckDriver, v -> globalSettings.useEnhancedSteamDeckDriver = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .flag(OptionFlag.GAME_RESTART)
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

    private static Identifier screenshot(String filename) {
        return CUtil.rl("textures/screenshots/" + filename);
    }
}
