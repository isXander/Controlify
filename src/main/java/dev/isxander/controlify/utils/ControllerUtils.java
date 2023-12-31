package dev.isxander.controlify.utils;

import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Function;

public class ControllerUtils {
    public static String createControllerString(Controller<?, ?> controller) {
        Optional<HIDDevice> hid = controller.hidInfo().flatMap(ControllerHIDService.ControllerHIDInfo::hidDevice);
        HexFormat hexFormat = HexFormat.of().withPrefix("0x");

        return String.format("'%s'#%s-%s (%s, %s: %s)",
                controller.name(),
                controller.joystickId(),
                controller.kind(),
                hid.map(device -> hexFormat.toHexDigits(device.vendorID())).orElse("?"),
                hid.map(device -> hexFormat.toHexDigits(device.productID())).orElse("?"),
                controller.hidInfo().map(ControllerHIDService.ControllerHIDInfo::type)
                        .orElse(ControllerType.UNKNOWN)
                        .friendlyName()
        );
    }

    public static void wrapControllerError(Runnable runnable, String errorTitle, Controller<?, ?> controller) {
        try {
            runnable.run();
        } catch (Throwable e) {
            CrashReport crashReport = CrashReport.forThrowable(e, errorTitle);
            CrashReportCategory category = crashReport.addCategory("Affected controller");
            category.setDetail("Controller name", controller.name());
            category.setDetail("Controller identification", controller.type().toString());
            category.setDetail("Controller type", controller.getClass().getCanonicalName());
            throw new ReportedException(crashReport);
        }
    }

    public static float deadzone(float value, float deadzone) {
        return (value - Math.copySign(Math.min(deadzone, Math.abs(value)), value)) / (1 - deadzone);
    }

    public static float applyCircularityX(float x, float y) {
        return (float) (x * Math.sqrt(1 - (y * y) / 2));
    }
    public static float applyCircularityY(float x, float y) {
        return (float) (y * Math.sqrt(1 - (x * x) / 2));
    }

    public static Vector2f applyEasingToLength(float x, float y, Function<Float, Float> easing) {
        float length = Mth.sqrt(x * x + y * y);
        float easedLength = easing.apply(length);
        float angle = (float) Mth.atan2(y, x);
        return new Vector2f(
                Mth.cos(angle) * easedLength,
                Mth.sin(angle) * easedLength
        );
    }

    public static boolean shouldApplyAntiSnapBack(float x, float y, float px, float py, float threshold) {
        float dx = x - px;
        float dy = y - py;
        float distanceSquared = dx * dx + dy * dy;

        boolean isSnap = distanceSquared >= threshold * threshold;
        boolean hasCrossedOrigin = Math.signum(x) != Math.signum(px) && Math.signum(y) != Math.signum(py);

        if (isSnap && hasCrossedOrigin) {
            // t is the distance from the origin to the middle of the line
            float t = (-x * (px - x) + -y * (py - y)) / distanceSquared;
            t = Mth.clamp(t, 0, 1);

            // Calculate the distance from the middle of the line to the origin
            double distanceToMiddle = Math.sqrt(Math.pow(-t * x + t * px, 2)
                    + Math.pow(-t * y + t * py, 2));

            // If the distance is less than 0.05, then the stick is close enough to the middle to be snap-backed
            return distanceToMiddle <= 0.01;
        }

        return false;
    }
}
