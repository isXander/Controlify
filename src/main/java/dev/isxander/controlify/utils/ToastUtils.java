package dev.isxander.controlify.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToastUtils {
    public static ControlifyToast sendToast(Component title, Component message, boolean longer) {
        ControlifyToast toast = ControlifyToast.create(title, message, longer);
        Minecraft.getInstance().getToasts().addToast(toast);
        return toast;
    }

    public static class ControlifyToast extends SystemToast {
        private boolean removed;

        private ControlifyToast(Component title, List<FormattedCharSequence> description, int maxWidth, boolean longer) {
            super(
                    /*? if >=1.20.4 {*/
                    longer ? SystemToastId.UNSECURE_SERVER_WARNING : SystemToastId.PERIODIC_NOTIFICATION,
                    /*?} else {*/
                    /*longer ? SystemToastIds.UNSECURE_SERVER_WARNING : SystemToastIds.PERIODIC_NOTIFICATION,
                    *//*?}*/
                    title,
                    description,
                    maxWidth
            );
        }

        @Override
        public @NotNull Visibility render(@NotNull GuiGraphics graphics, @NotNull ToastComponent manager, long startTime) {
            if (removed)
                return Visibility.HIDE;

            return super.render(graphics, manager, startTime);
        }

        public void remove() {
            this.removed = true;
        }

        public static ControlifyToast create(Component title, Component message, boolean longer) {
            Font font = Minecraft.getInstance().font;
            List<FormattedCharSequence> list = font.split(message, 200);
            int i = Math.max(200, list.stream().mapToInt(font::width).max().orElse(200));
            return new ControlifyToast(title, list, i + 30, longer);
        }
    }
}
