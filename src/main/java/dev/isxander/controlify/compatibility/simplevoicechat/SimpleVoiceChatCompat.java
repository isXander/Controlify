/*? if simple-voice-chat {*/
package dev.isxander.controlify.compatibility.simplevoicechat;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import dev.isxander.controlify.api.bind.BindingSupplier;
import dev.isxander.controlify.api.bind.ControlifyBindingsApi;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.v2.input.EmptyInput;
import dev.isxander.controlify.mixins.compat.simplevoicechat.KeyEventsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SimpleVoiceChatCompat {
    private static BindingSupplier pttHoldSupplier, pttToggleSupplier;
    private static BindingSupplier whisperHoldSupplier, whisperToggleSupplier;

    private static boolean pttDown, whisperDown;

    public static void init() {
        ResourceLocation muteIcon = registerIcon16x(new ResourceLocation("voicechat", "textures/icons/microphone_off.png"));
        ResourceLocation pttIcon = registerIcon16x(new ResourceLocation("voicechat", "textures/icons/microphone.png"));
        ResourceLocation whisperIcon = registerIcon16x(new ResourceLocation("voicechat", "textures/icons/microphone_whisper.png"));

        ControlifyBindingsApi.get().excludeVanillaBind(KeyEvents.KEY_PTT);
        ControlifyBindingsApi.get().excludeVanillaBind(KeyEvents.KEY_WHISPER);
        ControlifyBindingsApi.get().excludeVanillaBind(KeyEvents.KEY_MUTE);

        Component category = Component.translatable("key.categories.voicechat");
        pttHoldSupplier = ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "ptt_hold"), builder -> builder
                .name(Component.translatable("key.push_to_talk").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.hold")))
                .category(category)
                .defaultBind(new EmptyInput()));
        pttToggleSupplier = ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "ptt_toggle"), builder -> builder
                .name(Component.translatable("key.push_to_talk").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.toggle")))
                .category(category)
                .defaultBind(new EmptyInput())
                .radialCandidate(pttIcon));
        whisperHoldSupplier = ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "whisper_hold"), builder -> builder
                .name(Component.translatable("key.whisper").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.hold")))
                .category(category)
                .defaultBind(new EmptyInput()));
        whisperToggleSupplier = ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "whisper_toggle"), builder -> builder
                .name(Component.translatable("key.whisper").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.toggle")))
                .category(category)
                .defaultBind(new EmptyInput())
                .radialCandidate(whisperIcon));
        ControlifyBindingsApi.get().registerBind(new ResourceLocation("voicechat", "mute_microphone"), builder -> builder
                .name(Component.translatable("key.mute_microphone"))
                .category(category)
                .defaultBind(new EmptyInput())
                .vanillaOverride(KeyEvents.KEY_MUTE)
                .radialCandidate(muteIcon));

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

            controller.dualSense().ifPresent(ds -> {
                ds.setMuteLight(ClientManager.getPlayerStateManager().isMuted());
            });
        });
    }

    public static boolean isPTTDown() {
        return pttDown;
    }

    public static boolean isWhisperDown() {
        return whisperDown;
    }

    private static void checkConnected() {
        if (Minecraft.getInstance().getOverlay() == null && Minecraft.getInstance().screen == null) {
            ((KeyEventsAccessor) ClientManager.instance().getKeyEvents()).invokeCheckConnected();
        }
    }

    private static ResourceLocation registerIcon16x(ResourceLocation location) {
        ControlifyBindingsApi.get().registerRadialIcon(location, ((graphics, x, y, tickDelta) ->
                graphics.blit(location, x, y, 0, 0f, 0f, 16, 16, 16, 16)));
        return location;
    }
}
/*?}*/
