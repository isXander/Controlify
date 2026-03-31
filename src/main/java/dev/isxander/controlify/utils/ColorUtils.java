package dev.isxander.controlify.utils;

import net.minecraft.util.Mth;

public final class ColorUtils {

    public static int grey(float brightness, float alpha) {
        int component = Mth.floor(brightness * 0xff);

        int color = Mth.floor(alpha * 0xff); // A
        color = (color << 8) | component; // R
        color = (color << 8) | component; // G
        color = (color << 8) | component; // B

        return color;
    }
}
