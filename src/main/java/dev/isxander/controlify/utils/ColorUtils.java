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
}
