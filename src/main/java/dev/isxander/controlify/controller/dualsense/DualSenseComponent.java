package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;
import java.util.Queue;

public class DualSenseComponent implements ECSComponent {
    public static final Identifier ID = CUtil.rl("dualsense");

    private final Queue<DS5Effect> effectQueue = new ArrayDeque<>();

    public void submitEffect(DS5Effect effect) {
        this.effectQueue.add(effect);
    }

    public Queue<DS5Effect> getEffectQueue() {
        return this.effectQueue;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
