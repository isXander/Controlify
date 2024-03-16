package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.misc.BluetoothDeviceComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BluetoothWarningScreen extends ConfirmScreen {
    public BluetoothWarningScreen(BluetoothDeviceComponent bt, Screen nextScreen) {
        super(showAgain -> {
            if (!showAgain) {
                bt.confObj().dontShowWarningAgain = true;
                Controlify.instance().config().save();
            }

            Minecraft.getInstance().setScreen(nextScreen);
        },
                Component.translatable("controlify.bluetooth_warning.title"),
                Component.translatable("controlify.bluetooth_warning.desc"),
                CommonComponents.GUI_OK,
                Component.translatable("controlify.bluetooth_warning.dont_show")
        );
    }
}
