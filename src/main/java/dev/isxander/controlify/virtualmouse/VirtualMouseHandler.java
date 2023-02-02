package dev.isxander.controlify.virtualmouse;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.compatibility.screen.ScreenProcessorProvider;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.event.ControlifyEvents;
import dev.isxander.controlify.mixins.feature.virtualmouse.KeyboardHandlerAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class VirtualMouseHandler {
    private static final ResourceLocation CURSOR_TEXTURE = new ResourceLocation("controlify", "textures/gui/virtual_mouse.png");

    private double targetX, targetY;
    private double currentX, currentY;
    private final Minecraft minecraft;
    private boolean virtualMouseEnabled;

    public VirtualMouseHandler() {
        this.minecraft = Minecraft.getInstance();

        ControlifyEvents.INPUT_MODE_CHANGED.register(this::onInputModeChanged);
    }

    public void handleControllerInput(Controller controller) {
        if (controller.bindings().VMOUSE_TOGGLE.justPressed()) {
            toggleVirtualMouse();
        }

        if (!virtualMouseEnabled) {
            return;
        }

        var leftStickX = controller.state().axes().leftStickX();
        var leftStickY = controller.state().axes().leftStickY();

        // quadratic function to make small movements smaller
        // abs to keep sign
        targetX += leftStickX * Mth.abs(leftStickX) * 20f * controller.config().virtualMouseSensitivity;
        targetY += leftStickY * Mth.abs(leftStickY) * 20f * controller.config().virtualMouseSensitivity;

        targetX = Mth.clamp(targetX, 0, minecraft.getWindow().getWidth());
        targetY = Mth.clamp(targetY, 0, minecraft.getWindow().getHeight());

        var mouseHandler = (MouseHandlerAccessor) minecraft.mouseHandler;
        var keyboardHandler = (KeyboardHandlerAccessor) minecraft.keyboardHandler;

        if (controller.bindings().VMOUSE_LCLICK.justPressed()) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_PRESS, 0);
        } else if (controller.bindings().VMOUSE_LCLICK.justReleased()) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_RELEASE, 0);
        }

        if (controller.bindings().VMOUSE_RCLICK.justPressed()) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT, GLFW.GLFW_PRESS, 0);
        } else if (controller.bindings().VMOUSE_RCLICK.justReleased()) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT, GLFW.GLFW_RELEASE, 0);
        }

        if (controller.bindings().VMOUSE_MCLICK.justPressed()) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE, GLFW.GLFW_PRESS, 0);
        } else if (controller.bindings().VMOUSE_MCLICK.justReleased()) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE, GLFW.GLFW_RELEASE, 0);
        }

        if (controller.bindings().VMOUSE_ESCAPE.justPressed()) {
            keyboardHandler.invokeKeyPress(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_ESCAPE, 0, GLFW.GLFW_PRESS, 0);
        } else if (controller.bindings().VMOUSE_ESCAPE.justReleased()) {
            keyboardHandler.invokeKeyPress(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_ESCAPE, 0, GLFW.GLFW_RELEASE, 0);
        }

        // TODO: scrolling with right stick
    }

    public void updateMouse() {
        if (!virtualMouseEnabled) return;
        if (targetX == currentX && targetY == currentY) return; // don't need to needlessly update mouse position

        currentX = Mth.lerp(minecraft.getDeltaFrameTime(), currentX, targetX);
        currentY = Mth.lerp(minecraft.getDeltaFrameTime(), currentY, targetY);

        ((MouseHandlerAccessor) minecraft.mouseHandler).invokeOnMove(minecraft.getWindow().getWindow(), currentX, currentY);
    }

    public void onScreenChanged() {
        if (minecraft.screen != null) {
            if (requiresVirtualMouse()) {
                enableVirtualMouse();
            } else {
                disableVirtualMouse();
            }
            if (Controlify.instance().currentInputMode() == InputMode.CONTROLLER)
                GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        } else if (virtualMouseEnabled) {
            disableVirtualMouse();
            minecraft.mouseHandler.grabMouse();
        }
    }

    public void onInputModeChanged(InputMode mode) {
        if (mode == InputMode.CONTROLLER) {
            if (requiresVirtualMouse()) {
                enableVirtualMouse();
            }
        } else if (virtualMouseEnabled) {
            disableVirtualMouse();
        }
    }

    public void renderVirtualMouse(PoseStack matrices) {
        if (!virtualMouseEnabled) return;

        RenderSystem.setShaderTexture(0, CURSOR_TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();

        var scaledX = currentX * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        var scaledY = currentY * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();

        matrices.pushPose();
        matrices.translate(scaledX, scaledY, 0);
        matrices.scale(0.5f, 0.5f, 0.5f);

        GuiComponent.blit(matrices, -16, -16, 0, 0, 32, 32, 32, 32);

        matrices.popPose();

        RenderSystem.disableBlend();
    }

    public void enableVirtualMouse() {
        if (virtualMouseEnabled) return;

        setMousePosition();
        GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        virtualMouseEnabled = true;

        if (minecraft.mouseHandler.xpos() == 0 && minecraft.mouseHandler.ypos() == 0) {
            targetX = currentX = minecraft.getWindow().getScreenWidth() / 2f;
            targetY = currentY = minecraft.getWindow().getScreenHeight() / 2f;
        } else {
            targetX = currentX = minecraft.mouseHandler.xpos();
            targetY = currentY = minecraft.mouseHandler.ypos();
        }

        ControlifyEvents.VIRTUAL_MOUSE_TOGGLED.invoker().onVirtualMouseToggled(true);
    }

    public void disableVirtualMouse() {
        if (!virtualMouseEnabled) return;

        GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        setMousePosition();
        virtualMouseEnabled = false;
        targetX = currentX = minecraft.mouseHandler.xpos();
        targetY = currentY = minecraft.mouseHandler.ypos();

        ControlifyEvents.VIRTUAL_MOUSE_TOGGLED.invoker().onVirtualMouseToggled(false);
    }

    private void setMousePosition() {
        GLFW.glfwSetCursorPos(
                minecraft.getWindow().getWindow(),
                targetX,
                targetY
        );
    }

    public boolean requiresVirtualMouse() {
        return Controlify.instance().currentInputMode() == InputMode.CONTROLLER
                && minecraft.screen != null
                && (ScreenProcessorProvider.provide(minecraft.screen).forceVirtualMouse()
                || Controlify.instance().config().globalSettings().virtualMouseScreens.contains(minecraft.screen.getClass().getName())
                    );
    }

    public void toggleVirtualMouse() {
        if (minecraft.screen == null) return;

        var screens = Controlify.instance().config().globalSettings().virtualMouseScreens;
        var screenName = minecraft.screen.getClass().getName();
        if (screens.contains(screenName)) {
            screens.remove(screenName);
            disableVirtualMouse();
            Controlify.instance().hideMouse(true);

            minecraft.getToasts().addToast(SystemToast.multiline(
                    minecraft,
                    SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                    Component.translatable("controlify.toast.vmouse_disabled.title"),
                    Component.translatable("controlify.toast.vmouse_disabled.description")
            ));
        } else {
            screens.add(screenName);
            enableVirtualMouse();

            minecraft.getToasts().addToast(SystemToast.multiline(
                    minecraft,
                    SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                    Component.translatable("controlify.toast.vmouse_enabled.title"),
                    Component.translatable("controlify.toast.vmouse_enabled.description")
            ));
        }

        Controlify.instance().config().save();
    }

    public boolean isVirtualMouseEnabled() {
        return virtualMouseEnabled;
    }
}
