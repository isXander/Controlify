package dev.isxander.controlify.compatibility.simplevoicechat;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import dev.isxander.controlify.api.bind.BindingSupplier;
import dev.isxander.controlify.api.bind.ControlifyBindingsApi;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.EmptyBind;
import dev.isxander.controlify.mixins.compat.simplevoicechat.KeyEventsAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SimpleVoiceChatCompat {
    private static BindingSupplier pttHoldSupplier, pttToggleSupplier;
    private static BindingSupplier whisperHoldSupplier, whisperToggleSupplier;

    private static boolean pttDown, whisperDown;

    public static void init() {
        ControlifyBindingsApi.get().excludeVanillaBind(KeyEvents.KEY_PTT);
        ControlifyBindingsApi.get().excludeVanillaBind(KeyEvents.KEY_WHISPER);

        Component category = Component.translatable("key.categories.voicechat");
        pttHoldSupplier = ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "ptt_hold"), builder -> builder
                .name(Component.translatable("key.push_to_talk").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.hold")))
                .category(category)
                .defaultBind(new EmptyBind<>()));
        pttToggleSupplier = ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "ptt_toggle"), builder -> builder
                .name(Component.translatable("key.push_to_talk").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.toggle")))
                .category(category)
                .defaultBind(new EmptyBind<>()));
        whisperHoldSupplier = ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "whisper_hold"), builder -> builder
                .name(Component.translatable("key.whisper").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.hold")))
                .category(category)
                .defaultBind(new EmptyBind<>()));
        whisperToggleSupplier = ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "whisper_toggle"), builder -> builder
                .name(Component.translatable("key.whisper").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.toggle")))
                .category(category)
                .defaultBind(new EmptyBind<>()));

        ControlifyEvents.ACTIVE_CONTROLLER_TICKED.register(controller -> {
            var pttHold = pttHoldSupplier.onController(controller);
            var pttToggle = pttToggleSupplier.onController(controller);
            var whisperHold = whisperHoldSupplier.onController(controller);
            var whisperToggle = whisperToggleSupplier.onController(controller);

            if (pttToggle.justPressed()) {
                pttDown = !pttDown;
                checkConnected();
            }
            if (whisperToggle.justPressed()) {
                whisperDown = !whisperDown;
                checkConnected();
            }

            if (pttHold.justPressed() || whisperHold.justPressed()) {
                checkConnected();
            }
            if (pttHold.held()) {
                pttDown = true;
            } else if (pttHold.justReleased()) {
                pttDown = false;
            }
            if (whisperHold.held()) {
                whisperDown = true;
            } else if (whisperHold.justReleased()) {
                whisperDown = false;
            }
        });
    }

    public static boolean isPTTDown() {
        return pttDown;
    }

    public static boolean isWhisperDown() {
        return whisperDown;
    }

    private static void checkConnected() {
        ((KeyEventsAccessor) ClientManager.instance().getKeyEvents()).invokeCheckConnected();
    }
}
