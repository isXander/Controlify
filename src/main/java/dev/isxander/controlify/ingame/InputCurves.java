package dev.isxander.controlify.ingame;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum InputCurves implements InputCurve, NameableEnum {
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

    @Override
    public double apply(double input) {
        return this.curve.apply(input);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("controlify.input_curves." + name().toLowerCase());
    }
}
