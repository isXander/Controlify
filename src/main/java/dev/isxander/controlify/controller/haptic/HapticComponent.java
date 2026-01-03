package dev.isxander.controlify.controller.haptic;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.haptics.hd.HDHapticsMixer;
import dev.isxander.controlify.haptics.hd.source.HDHapticsSource;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class HapticComponent implements ECSComponent {
    public static final Identifier ID = CUtil.rl("hd_haptics");

    private final HDHapticsMixer mixer;
    private final Runnable pumpFunction;

    public HapticComponent(HDHapticsMixer mixer, Runnable pumpFunction) {
        this.mixer = mixer;
        this.pumpFunction = pumpFunction;
    }

    public void addHaptic(HDHapticsSource source) {
        this.mixer.add(source);
        this.pumpFunction.run();
    }

    public void clearHaptics() {
        this.mixer.clear();
        this.pumpFunction.run();
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
