package dev.isxander.controlify.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public class ToastUtils {
    public static void sendToast(Component title, Component message, boolean longer) {
        SystemToast toast = SystemToast.multiline(
                Minecraft.getInstance(),
                longer ? SystemToast.SystemToastId.UNSECURE_SERVER_WARNING : SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                title,
                message
        );
        Minecraft.getInstance().getToastManager().addToast(toast);
    }
}
