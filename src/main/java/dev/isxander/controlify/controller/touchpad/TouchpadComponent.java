package dev.isxander.controlify.controller.touchpad;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TouchpadComponent implements ECSComponent {
    public static final ResourceLocation ID = CUtil.rl("touchpad");

    private List<TouchpadState.Finger> fingers;
    private List<TouchpadState.Finger> prevFingers;
    private final int maxFingers;

    public TouchpadComponent(int maxFingers) {
        this.fingers = new ArrayList<>();
        this.prevFingers = new ArrayList<>();
        this.maxFingers = maxFingers;
    }

    public List<TouchpadState.Finger> fingersNow() {
        return this.fingers;
    }

    public List<TouchpadState.Finger> fingersThen() {
        return this.prevFingers;
    }

    public void pushFingers(Collection<TouchpadState.Finger> fingers) {
        this.prevFingers = this.fingers;
        this.fingers = fingers.stream().toList();
    }

    public int getMaxFingers() {
        return this.maxFingers;
    }
}
