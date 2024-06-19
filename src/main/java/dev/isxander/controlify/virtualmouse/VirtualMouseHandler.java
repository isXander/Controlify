package dev.isxander.controlify.virtualmouse;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.vmousesnapping.ISnapBehaviour;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadState;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.mixins.feature.virtualmouse.KeyboardHandlerAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.HoldRepeatHelper;
import dev.isxander.controlify.utils.ToastUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class VirtualMouseHandler {
    private static final ResourceLocation CURSOR_TEXTURE = CUtil.rl("textures/gui/virtual_mouse.png");

    private double targetX, targetY;
    private double currentX, currentY;

    private double scrollX, scrollY;

    private float prevXFinger, prevYFinger;

    private final Minecraft minecraft;
    private boolean virtualMouseEnabled;

    private Set<SnapPoint> snapPoints;
    private SnapPoint lastSnappedPoint;

    private final HoldRepeatHelper holdRepeatHelper = new HoldRepeatHelper(10, 6);

    public VirtualMouseHandler() {
        this.minecraft = Minecraft.getInstance();

        if (minecraft.screen != null && minecraft.screen instanceof ISnapBehaviour snapBehaviour)
            snapPoints = snapBehaviour.getSnapPoints();
        else
            snapPoints = Set.of();

        ControlifyEvents.INPUT_MODE_CHANGED.register(event -> this.onInputModeChanged(event.mode()));
    }

    public void handleControllerInput(ControllerEntity controller) {
        if (ControlifyBindings.VMOUSE_TOGGLE.on(controller).justPressed()) {
            toggleVirtualMouse();
        }

        if (!virtualMouseEnabled) {
            return;
        }

        InputComponent input = controller.input().orElseThrow();
        Optional<TouchpadComponent> touchpad = controller.touchpad();

        List<TouchpadState.Finger> fingerDeltas = touchpad.map(state -> ControllerUtils.deltaFingers(
                state.fingersNow(),
                state.fingersThen()
        )).orElse(List.of());

        float xImpulseFinger = 0;
        float yImpulseFinger = 0;
        if (!fingerDeltas.isEmpty()) {
            TouchpadState.Finger finger = fingerDeltas.get(0);
            xImpulseFinger = finger.position().x();
            yImpulseFinger = finger.position().y();

            // finger pos is in range 0-1, so we need to scale it up loads
            xImpulseFinger *= 20;
            yImpulseFinger *= 20;
        }

        InputBinding moveRight = ControlifyBindings.VMOUSE_MOVE_RIGHT.on(controller);
        InputBinding moveLeft = ControlifyBindings.VMOUSE_MOVE_LEFT.on(controller);
        InputBinding moveDown = ControlifyBindings.VMOUSE_MOVE_DOWN.on(controller);
        InputBinding moveUp = ControlifyBindings.VMOUSE_MOVE_UP.on(controller);

        // apply an easing function directly to the vector's length
        // if you do easing(x), easing(y), then the diagonals where it's something like (~0.8, ~0.8) will incorrectly ease
        Vector2f impulse = ControllerUtils.applyEasingToLength(
                moveRight.analogueNow() - moveLeft.analogueNow(),
                moveDown.analogueNow() - moveUp.analogueNow(),
                x -> (float) Math.pow(x, 3)
        );
        Vector2f prevImpulse = ControllerUtils.applyEasingToLength(
                moveRight.analoguePrev() - moveLeft.analoguePrev(),
                moveDown.analoguePrev() - moveUp.analoguePrev(),
                x -> (float) Math.pow(x, 3)
        );

        Vector2f fingerImpulse = ControllerUtils.applyEasingToLength(xImpulseFinger, yImpulseFinger, x -> (float) Math.pow(x, 1.5));
        Vector2f prevFingerImpulse = ControllerUtils.applyEasingToLength(prevXFinger, prevYFinger, x -> (float) Math.pow(x, 1.5));

        impulse.add(fingerImpulse);
        prevImpulse.add(prevFingerImpulse);

        prevXFinger = xImpulseFinger;
        prevYFinger = yImpulseFinger;

        if (minecraft.screen != null && minecraft.screen instanceof ISnapBehaviour snapBehaviour) {
            snapPoints = snapBehaviour.getSnapPoints();
        } else {
            snapPoints = Set.of();
        }

        // if just released stick, snap to nearest snap point
        if (impulse.x == 0 && impulse.y == 0) {
            if ((prevImpulse.x != 0 || prevImpulse.y != 0))
                snapToClosestPoint();
        }

        var sensitivity = input.config().config().virtualMouseSensitivity;
        var windowSizeModifier = Math.max(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight()) / 800f;

        // cubic function to make small movements smaller
        // abs to keep sign
        targetX += impulse.x * 20f * sensitivity * windowSizeModifier;
        targetY += impulse.y * 20f * sensitivity * windowSizeModifier;

        targetX = Mth.clamp(targetX, 0, minecraft.getWindow().getWidth());
        targetY = Mth.clamp(targetY, 0, minecraft.getWindow().getHeight());

        scrollY += ControlifyBindings.VMOUSE_SCROLL_UP.on(controller).analogueNow()
                - ControlifyBindings.VMOUSE_SCROLL_DOWN.on(controller).analogueNow();

        if (holdRepeatHelper.shouldAction(ControlifyBindings.VMOUSE_SNAP_UP.on(controller))) {
            snapInDirection(ScreenDirection.UP);
            holdRepeatHelper.onNavigate();
        } else if (holdRepeatHelper.shouldAction(ControlifyBindings.VMOUSE_SNAP_DOWN.on(controller))) {
            snapInDirection(ScreenDirection.DOWN);
            holdRepeatHelper.onNavigate();
        } else if (holdRepeatHelper.shouldAction(ControlifyBindings.VMOUSE_SNAP_LEFT.on(controller))) {
            snapInDirection(ScreenDirection.LEFT);
            holdRepeatHelper.onNavigate();
        } else if (holdRepeatHelper.shouldAction(ControlifyBindings.VMOUSE_SNAP_RIGHT.on(controller))) {
            snapInDirection(ScreenDirection.RIGHT);
            holdRepeatHelper.onNavigate();
        }

        if (ScreenProcessorProvider.provide(minecraft.screen).virtualMouseBehaviour().isDefaultOr(VirtualMouseBehaviour.ENABLED)) {
            handleCompatibilityBinds(controller);
        }

        if (ControlifyBindings.GUI_BACK.on(controller).justPressed() && minecraft.screen != null) {
            ScreenProcessor.playClackSound();
            minecraft.screen.onClose();
        }
    }

    public void handleCompatibilityBinds(ControllerEntity controller) {
        var mouseHandler = (MouseHandlerAccessor) minecraft.mouseHandler;
        var keyboardHandler = (KeyboardHandlerAccessor) minecraft.keyboardHandler;

        Optional<TouchpadComponent> touchpad = controller.touchpad();
        List<TouchpadState.Finger> touchpadState = touchpad.map(TouchpadComponent::fingersNow).orElse(List.of());
        List<TouchpadState.Finger> prevTouchpadState = touchpad.map(TouchpadComponent::fingersThen).orElse(List.of());

        InputComponent input = controller.input().orElseThrow();
        boolean touchpadPressed = input.stateNow().isButtonDown(GamepadInputs.TOUCHPAD_BUTTON);
        boolean prevTouchpadPressed = input.stateThen().isButtonDown(GamepadInputs.TOUCHPAD_BUTTON);

        if (ControlifyBindings.VMOUSE_LCLICK.on(controller).justPressed() || (touchpadPressed && !prevTouchpadPressed && touchpadState.size() == 1)) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_PRESS, 0);
        } else if (ControlifyBindings.VMOUSE_LCLICK.on(controller).justReleased() || (!touchpadPressed && prevTouchpadPressed)) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_RELEASE, 0);
        }

        if (ControlifyBindings.VMOUSE_RCLICK.on(controller).justPressed() || (touchpadPressed && !prevTouchpadPressed && touchpadState.size() == 2)) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT, GLFW.GLFW_PRESS, 0);
        } else if (ControlifyBindings.VMOUSE_RCLICK.on(controller).justReleased() || (!touchpadPressed && prevTouchpadPressed)) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT, GLFW.GLFW_RELEASE, 0);
        }

        if (ControlifyBindings.VMOUSE_SHIFT_CLICK.on(controller).justPressed()) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_PRESS, 0);
        } else if (ControlifyBindings.VMOUSE_SHIFT_CLICK.on(controller).justReleased()) {
            mouseHandler.invokeOnPress(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_RELEASE, 0);
        }
    }

    public void updateMouse() {
        if (!virtualMouseEnabled) return;
        /*? if >1.20.6 {*/
        float delta = minecraft.getTimer().getRealtimeDeltaTicks();
        /*?} else {*/
        /*float delta = minecraft.getDeltaFrameTime();
        *//*?}*/

        if (Math.round(targetX * 100) / 100.0 != Math.round(currentX * 100) / 100.0 || Math.round(targetY * 100) / 100.0 != Math.round(currentY * 100) / 100.0) {
            currentX = Mth.lerp(delta, currentX, targetX);
            currentY = Mth.lerp(delta, currentY, targetY);

            ((MouseHandlerAccessor) minecraft.mouseHandler).invokeOnMove(minecraft.getWindow().getWindow(), currentX, currentY);
        } else {
            currentX = targetX;
            currentY = targetY;
        }

        if (Math.abs(scrollX) >= 0.01 || Math.abs(scrollY) >= 0.01) {
            var currentScrollY = scrollY * delta;
            scrollY -= currentScrollY;
            var currentScrollX = scrollX * delta;
            scrollX -= currentScrollX;

            ((MouseHandlerAccessor) minecraft.mouseHandler).invokeOnScroll(minecraft.getWindow().getWindow(), currentScrollX, currentScrollY);
        } else {
            scrollX = scrollY = 0;
        }
    }

    public void snapToClosestPoint() {
        var window = minecraft.getWindow();
        var scaleFactor = new Vector2d((double)window.getGuiScaledWidth() / (double)window.getScreenWidth(), (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
        var target = new Vector2d(targetX, targetY).mul(scaleFactor);

        if (lastSnappedPoint != null) {
            if (lastSnappedPoint.position().distanceSquared(new Vector2i(target, RoundingMode.FLOOR)) > (long) lastSnappedPoint.range() * lastSnappedPoint.range()) {
                lastSnappedPoint = null;
            }
        }

        var closestSnapPoint = snapPoints.stream()
                .filter(snapPoint -> !snapPoint.equals(lastSnappedPoint)) // don't snap to the point currently over snapped point
                .map(snapPoint -> new Pair<>(snapPoint, snapPoint.position().distanceSquared(new Vector2i(target, RoundingMode.FLOOR)))) // map with distance to current pos
                .filter(point -> point.getSecond() <= (long) point.getFirst().range() * point.getFirst().range()) // filter out of range options
                .min(Comparator.comparingLong(Pair::getSecond)) // find the closest point
                .orElse(new Pair<>(null, Long.MAX_VALUE)).getFirst(); // retrieve point

        if (closestSnapPoint != null) {
            snapToPoint(closestSnapPoint, scaleFactor);
        }
    }

    public void snapInDirection(ScreenDirection direction) {
        var window = minecraft.getWindow();
        var scaleFactor = new Vector2d((double)window.getGuiScaledWidth() / (double)window.getScreenWidth(), (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
        var target = new Vector2d(targetX, targetY).mul(scaleFactor);

        var closestSnapPoint = snapPoints.stream()
                .filter(snapPoint -> !snapPoint.equals(lastSnappedPoint)) // don't snap to the point currently over snapped point
                .map(snapPoint -> new Pair<>(snapPoint, new Vector2d(snapPoint.position().x() - target.x(), snapPoint.position().y() - target.y()))) // map with distance to current pos
                // filter points that are not in the correct direction
                .filter(pair -> {
                    Vector2d dist = pair.getSecond();

                    double axis = direction.getAxis() == ScreenAxis.HORIZONTAL ? dist.x : dist.y;
                    double positive = direction.isPositive() ? 1 : -1;

                    return axis * positive > 0;
                })
                .filter(pair -> {
                    SnapPoint snapPoint = pair.getFirst();
                    Vector2d dist = pair.getSecond();

                    // distance in the correct orthogonal direction
                    double distance = Math.abs(direction.getAxis() == ScreenAxis.HORIZONTAL ? dist.x : dist.y);
                    // distance in the incorrect orthogonal direction
                    double deviation = Math.abs(direction.getAxis() == ScreenAxis.HORIZONTAL ? dist.y : dist.x);

                    // punish deviation significantly
                    pair.getSecond().set(distance, deviation * 4);

                    // reject if the deviation is double the correct direction away
                    return distance >= snapPoint.range() && (deviation < distance * 2);
                })
                // pick the closest point
                .min(Comparator.comparingDouble(pair -> {
                    Vector2d distDev = pair.getSecond();
                    // x = dist, y = deviation
                    return distDev.x + distDev.y;
                }))
                .map(Pair::getFirst);

        closestSnapPoint.ifPresent(snapPoint -> {
            snapToPoint(snapPoint, scaleFactor);
        });
    }

    public void snapToPoint(SnapPoint snapPoint, Vector2dc scaleFactor) {
        lastSnappedPoint = snapPoint;

        targetX = currentX = snapPoint.position().x() / scaleFactor.x();
        targetY = currentY = snapPoint.position().y() / scaleFactor.y();
        ((MouseHandlerAccessor) minecraft.mouseHandler).invokeOnMove(minecraft.getWindow().getWindow(), currentX, currentY);
    }

    public void onScreenChanged() {
        if (minecraft.screen != null) {
            if (requiresVirtualMouse()) {
                enableVirtualMouse();
            } else {
                disableVirtualMouse();
            }
            if (Controlify.instance().currentInputMode().isController())
                GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        } else if (virtualMouseEnabled) {
            disableVirtualMouse();

            minecraft.mouseHandler.grabMouse(); // re-grab mouse after vmouse disable
        }
    }

    public void onInputModeChanged(InputMode mode) {
        if (mode.isController()) {
            if (requiresVirtualMouse()) {
                enableVirtualMouse();
            }
        } else if (virtualMouseEnabled) {
            disableVirtualMouse();
        }
    }

    public void renderVirtualMouse(GuiGraphics graphics) {
        if (!virtualMouseEnabled) return;

        if (DebugProperties.DEBUG_SNAPPING) {
            for (var snapPoint : snapPoints) {
                graphics.fill(snapPoint.position().x() - snapPoint.range(), snapPoint.position().y() - snapPoint.range(), snapPoint.position().x() + snapPoint.range(), snapPoint.position().y() + snapPoint.range(), 0x33FFFFFF);
                graphics.fill( snapPoint.position().x() - 1, snapPoint.position().y() - 1, snapPoint.position().x() + 1, snapPoint.position().y() + 1, snapPoint.equals(lastSnappedPoint) ? 0xFFFFFF00 : 0xFFFF0000);
            }
        }

        var scaledX = currentX * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        var scaledY = currentY * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();

        graphics.pose().pushPose();
        graphics.pose().translate(scaledX, scaledY, 1000f);
        graphics.pose().scale(0.5f, 0.5f, 0.5f);

        RenderSystem.enableBlend();
        graphics.blit(CURSOR_TEXTURE, -16, -16, 0, 0, 32, 32, 32, 32);
        RenderSystem.disableBlend();

        graphics.pose().popPose();
    }

    public void enableVirtualMouse() {
        if (virtualMouseEnabled) return;

        GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        virtualMouseEnabled = true;

        if (minecraft.mouseHandler.xpos() == -50 && minecraft.mouseHandler.ypos() == -50) {
            targetX = currentX = minecraft.getWindow().getScreenWidth() / 2f;
            targetY = currentY = minecraft.getWindow().getScreenHeight() / 2f;
        } else {
            targetX = currentX = minecraft.mouseHandler.xpos();
            targetY = currentY = minecraft.mouseHandler.ypos();
        }
        setMousePosition();

        ControlifyEvents.VIRTUAL_MOUSE_TOGGLED.invoke(new ControlifyEvents.VirtualMouseToggled(true));
        if (minecraft.screen != null) {
            ScreenProcessorProvider.provide(minecraft.screen).onVirtualMouseToggled(true);
        }
    }

    public void disableVirtualMouse() {
        if (!virtualMouseEnabled) return;

        // make sure minecraft doesn't think the mouse is grabbed when it isn't
        ((MouseHandlerAccessor) minecraft.mouseHandler).setMouseGrabbed(false);

        Controlify.instance().hideMouse(true, true);
        GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        setMousePosition();
        virtualMouseEnabled = false;
        targetX = currentX = minecraft.mouseHandler.xpos();
        targetY = currentY = minecraft.mouseHandler.ypos();

        ControlifyEvents.VIRTUAL_MOUSE_TOGGLED.invoke(new ControlifyEvents.VirtualMouseToggled(false));
        if (minecraft.screen != null) {
            ScreenProcessorProvider.provide(minecraft.screen).onVirtualMouseToggled(false);
        }
    }

    private void setMousePosition() {
        GLFW.glfwSetCursorPos(
                minecraft.getWindow().getWindow(),
                targetX,
                targetY
        );
    }

    public boolean requiresVirtualMouse() {
        var isController = Controlify.instance().currentInputMode().isController();
        var hasScreen = minecraft.screen != null;

        if (isController && hasScreen) {
            return switch (ScreenProcessorProvider.provide(minecraft.screen).virtualMouseBehaviour()) {
                case DEFAULT -> Controlify.instance().config().globalSettings().virtualMouseScreens.stream().anyMatch(s -> s.isAssignableFrom(minecraft.screen.getClass()));
                case ENABLED, CURSOR_ONLY -> true;
                case DISABLED -> false;
            };
        }

        return false;
    }

    public void toggleVirtualMouse() {
        if (minecraft.screen == null) return;

        if (ScreenProcessorProvider.provide(minecraft.screen).virtualMouseBehaviour() != VirtualMouseBehaviour.DEFAULT) {
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.vmouse_unavailable.title"),
                    Component.translatable("controlify.toast.vmouse_unavailable.description"),
                    false
            );
            return;
        }

        var screens = Controlify.instance().config().globalSettings().virtualMouseScreens;
        var screenClass = minecraft.screen.getClass();
        if (screens.contains(screenClass)) {
            screens.remove(screenClass);
            disableVirtualMouse();
            Controlify.instance().hideMouse(true, false);

            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.vmouse_disabled.title"),
                    Component.translatable("controlify.toast.vmouse_disabled.description"),
                    false
            );
        } else {
            screens.add(screenClass);
            enableVirtualMouse();

            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.vmouse_enabled.title"),
                    Component.translatable("controlify.toast.vmouse_enabled.description"),
                    false
            );
        }

        Controlify.instance().config().save();
    }

    public boolean isVirtualMouseEnabled() {
        return virtualMouseEnabled;
    }

    public int getCurrentX(float deltaTime) {
        return (int) Mth.lerp(deltaTime, currentX, targetX);
    }

    public int getCurrentY(float deltaTime) {
        return (int) Mth.lerp(deltaTime, currentY, targetY);
    }
}
