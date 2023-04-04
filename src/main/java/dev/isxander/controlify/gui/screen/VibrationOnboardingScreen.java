package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VibrationOnboardingScreen extends ConfirmScreen {
    public VibrationOnboardingScreen(Screen lastScreen, BooleanConsumer onAnswered) {
        super(
                yes -> {
                    Controlify.instance().config().globalSettings().loadVibrationNatives = yes;
                    Controlify.instance().config().globalSettings().vibrationOnboarded = true;
                    Controlify.instance().config().save();
                    Minecraft.getInstance().setScreen(lastScreen);
                    onAnswered.accept(yes);
                },
                Component.translatable("controlify.vibration_onboarding.title").withStyle(ChatFormatting.BOLD),
                Component.translatable("controlify.vibration_onboarding.message")
        );
    }
}
