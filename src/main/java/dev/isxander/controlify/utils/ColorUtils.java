package dev.isxander.controlify.utils;

//? if >=1.21.2 {
import net.minecraft.util.ARGB;
//?} else {
/*import net.minecraft.util.FastColor;
*///?}



public final class ColorUtils {

    public static int lerpARGB(float delta, int start, int end) {
        //? if >=1.21.2 {
        return ARGB.lerp(delta, start, end);
        //?} else {
        /*return FastColor.ARGB32.lerp(delta, start, end);
        *///?}
    }

    public static int argbRed(int argb) {
        //? if >=1.21.2 {
        return ARGB.red(argb);
        //?} else {
        /*return FastColor.ARGB32.red(argb);
        *///?}
    }

    public static int argbGreen(int argb) {
        //? if >=1.21.2 {
        return ARGB.green(argb);
        //?} else {
        /*return FastColor.ARGB32.green(argb);
        *///?}
    }

    public static int argbBlue(int argb) {
        //? if >=1.21.2 {
        return ARGB.blue(argb);
        //?} else {
        /*return FastColor.ARGB32.blue(argb);
        *///?}
    }

    public static int argbAlpha(int argb) {
        //? if >=1.21.2 {
        return ARGB.alpha(argb);
        //?} else {
        /*return FastColor.ARGB32.alpha(argb);
        *///?}
    }

    public static float[] decomposeARGBFloat(int argb) {
        //? if >=1.21.2 {
        return new float[]{
                ARGB.alpha(argb) / 255f,
                ARGB.red(argb) / 255f,
                ARGB.green(argb) / 255f,
                ARGB.blue(argb) / 255f
        };
        //?} else {
        /*return new float[]{
                FastColor.ARGB32.alpha(argb) / 255f,
                FastColor.ARGB32.red(argb) / 255f,
                FastColor.ARGB32.green(argb) / 255f,
                FastColor.ARGB32.blue(argb) / 255f
        };
        *///?}
    }
}
