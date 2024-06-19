/*? if simple-voice-chat {*/
package dev.isxander.controlify.compatibility.simplevoicechat;

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.compatibility.simplevoicechat.mixins.KeyEventsAccessor;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SimpleVoiceChatCompat {
    private static InputBindingSupplier pttHoldSupplier, pttToggleSupplier;
    private static InputBindingSupplier whisperHoldSupplier, whisperToggleSupplier;

    private static boolean pttDown, whisperDown;

    public static void init() {
        ResourceLocation muteIcon = registerIcon16x(CUtil.rl("voicechat", "textures/icons/microphone_off.png"));
        ResourceLocation pttIcon = registerIcon16x(CUtil.rl("voicechat", "textures/icons/microphone.png"));
        ResourceLocation whisperIcon = registerIcon16x(CUtil.rl("voicechat", "textures/icons/microphone_whisper.png"));

        Component category = Component.translatable("key.categories.voicechat");
        pttHoldSupplier = ControlifyBindApi.get().registerBinding(builder -> builder
                .id("voicechat", "ptt_hold")
                .category(category)
                .addKeyCorrelation(KeyEvents.KEY_PTT));
        pttToggleSupplier = ControlifyBindApi.get().registerBinding(builder -> builder
                .id("voicechat", "ptt_toggle")
                .category(category)
                .radialCandidate(pttIcon)
                .addKeyCorrelation(KeyEvents.KEY_PTT));
        whisperHoldSupplier = ControlifyBindApi.get().registerBinding(builder -> builder
                .name(Component.translatable("key.whisper").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.hold")))
                .id("voicechat", "whisper_hold")
                .category(category)
                .addKeyCorrelation(KeyEvents.KEY_WHISPER));
        whisperToggleSupplier = ControlifyBindApi.get().registerBinding(builder -> builder
                .name(Component.translatable("key.whisper").append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.toggle")))
                .id("voicechat", "whisper_toggle")
                .category(category)
                .radialCandidate(whisperIcon)
                .addKeyCorrelation(KeyEvents.KEY_WHISPER));
        ControlifyBindApi.get().registerBinding(builder -> builder
                .id("voicechat", "mute_microphone")
                .category(category)
                .radialCandidate(muteIcon)
                .keyEmulation(KeyEvents.KEY_MUTE));

        ControlifyEvents.ACTIVE_CONTROLLER_TICKED.register(event -> {
            ControllerEntity controller = event.controller();

            var pttHold = pttHoldSupplier.on(controller);
            var pttToggle = pttToggleSupplier.on(controller);
            var whisperHold = whisperHoldSupplier.on(controller);
            var whisperToggle = whisperToggleSupplier.on(controller);

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
            if (pttHold.digitalNow()) {
                pttDown = true;
            } else if (pttHold.justReleased()) {
                pttDown = false;
            }
            if (whisperHold.digitalNow()) {
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
        ControlifyBindApi.get().registerRadialIcon(location, ((graphics, x, y, tickDelta) ->
                graphics.blit(location, x, y, 0, 0f, 0f, 16, 16, 16, 16)));
        return location;
    }
}
/*?}*/
