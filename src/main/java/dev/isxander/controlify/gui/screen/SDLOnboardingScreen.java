package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.driver.SDL3NativesManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class SDLOnboardingScreen extends ConfirmScreen implements DontInteruptScreen {
    public SDLOnboardingScreen(Runnable onceDecided, BooleanConsumer onAnswered) {
        super(
                yes -> {
                    Controlify.instance().config().globalSettings().loadVibrationNatives = yes;
                    Controlify.instance().config().globalSettings().vibrationOnboarded = true;
                    Controlify.instance().config().save();
                    onceDecided.run();
                    onAnswered.accept(yes);
                },
                Component.translatable("controlify.sdl3_onboarding.title").withStyle(ChatFormatting.BOLD),
                Util.make(() -> {
                    var message = Component.translatable("controlify.sdl3_onboarding.message");

                    if (SDL3NativesManager.Target.CURRENT.isMacArm()) {
                        message.append("\n").append(Component.translatable("controlify.sdl3_onboarding.message_mac").withStyle(ChatFormatting.RED));
                    }

                    message.append("\n\n").append(Component.translatable("controlify.sdl3_onboarding.question"));

                    return message;
                })
        );
    }
}
