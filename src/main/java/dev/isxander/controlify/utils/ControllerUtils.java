package dev.isxander.controlify.utils;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.touchpad.Touchpads;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ControllerUtils {
    public static String createControllerString(ControllerEntity controller) {
        return String.format("'%s'#%s-%s (%s)",
                controller.name(),
                controller.info().ucid().toString(),
                controller.info().hid().map(hid -> hid.asIdentifier().toString()).orElse("hid"),
                controller.info().type().friendlyName()
        );
    }

    public static void wrapControllerError(Runnable runnable, String errorTitle, ControllerEntity controller) {
        try {
            runnable.run();
        } catch (Throwable e) {
            CrashReport crashReport = CrashReport.forThrowable(e, errorTitle);
            CrashReportCategory category = crashReport.addCategory("Affected controller");
            category.setDetail("Controller name", controller.name());
            category.setDetail("Controller identification", controller.info().type().friendlyName());
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

    public static Vector2d applyEasingToLength(double x, double y, Function<Double, Double> easing) {
        double length = Math.sqrt(x * x + y * y);
        if(length == 0.0)
            return new Vector2d(0, 0);
        double easedLength = easing.apply(length);
        double angle = Mth.atan2(y, x);
        return new Vector2d(
                Math.cos(angle) * easedLength,
                Math.sin(angle) * easedLength
        );
    }

    public static Vector2d applyEasingToLength(Vector2d vec, Function<Double, Double> easing) {
        double length = vec.length();
        if(length == 0.0)
            return new Vector2d(0, 0);
        double easedLength = easing.apply(length);
        return vec.normalize(easedLength);
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

    public static List<Touchpads.Finger> deltaFingers(List<Touchpads.Finger> now, List<Touchpads.Finger> then) {
        return now.stream()
                .flatMap(finger -> then.stream().anyMatch(f -> f.id() == finger.id()) ? Stream.of(finger) : Stream.empty())
                .map(nowFinger -> {
                    Touchpads.Finger thenFinger = then.stream()
                            .filter(f -> f.id() == nowFinger.id())
                            .findFirst()
                            .orElseThrow();

                    return new Touchpads.Finger(
                            nowFinger.id(),
                            new Vector2f(
                                    nowFinger.position().x() - thenFinger.position().x(),
                                    nowFinger.position().y() - thenFinger.position().y()
                            ),
                            nowFinger.pressure() - thenFinger.pressure()
                    );
                })
                .toList();
    }
}
