package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.misc.BluetoothDeviceComponent;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BluetoothWarningScreen extends ConfirmScreen {
    public BluetoothWarningScreen(BluetoothDeviceComponent bt, Screen nextScreen) {
        super(showAgain -> {
            if (!showAgain) {
                bt.settings().dontShowWarning = true;
                Controlify.instance().config().saveSafely();
            }

            MinecraftUtil.setScreen(nextScreen);
        },
                Component.translatable("controlify.bluetooth_warning.title"),
                Component.translatable("controlify.bluetooth_warning.desc"),
                CommonComponents.GUI_OK,
                Component.translatable("controlify.bluetooth_warning.dont_show")
        );
    }
}
