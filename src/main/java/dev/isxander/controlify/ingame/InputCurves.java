package dev.isxander.controlify.ingame;

import com.mojang.serialization.Codec;
import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum InputCurves implements InputCurve, NameableEnum, StringRepresentable {
    LINEAR(d -> d),
    GENTLE(InputCurve.power(1.5)),
    STANDARD(InputCurve.power(2.0)),
    STRONG(InputCurve.power(2.5)),
    PRECISION_CUBIC(InputCurve.cubicBlend(0.5)),
    S_CURVE(InputCurve.sCurve());

    private final InputCurve curve;

    InputCurves(InputCurve curve) {
        this.curve = curve;
    }

    public static final Codec<InputCurves> CODEC = StringRepresentable.fromEnum(InputCurves::values);

    @Override
    public double apply(double input) {
        return this.curve.apply(input);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("controlify.input_curves." + name().toLowerCase());
    }

    @Override
    public @NonNull String getSerializedName() {
        return name().toLowerCase();
    }
}
