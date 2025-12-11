package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.driver.sdl.dualsense.DS5EffectsState;
import dev.isxander.controlify.driver.sdl.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class DualSenseComponent implements ECSComponent {
    public static final Identifier ID = CUtil.rl("dualsense");

    private boolean muteLight;

    private DualsenseTriggerEffect leftTriggerEffect;
    private DualsenseTriggerEffect rightTriggerEffect;

    private boolean dirty;

    public void setLeftTriggerEffect(DualsenseTriggerEffect effect) {
        this.leftTriggerEffect = effect;
        this.setDirty();
    }

    public DualsenseTriggerEffect getLeftTriggerEffect() {
        return this.leftTriggerEffect;
    }

    public void setRightTriggerEffect(DualsenseTriggerEffect effect) {
        this.rightTriggerEffect = effect;
        this.setDirty();
    }

    public DualsenseTriggerEffect getRightTriggerEffect() {
        return this.rightTriggerEffect;
    }

    public void setMuteLight(boolean on) {
        if (this.muteLight != on) {
            this.muteLight = on;
            this.setDirty();
        }
    }

    public boolean getMuteLight() {
        return this.muteLight;
    }

    private void setDirty() {
        this.dirty = true;
    }

    public boolean consumeDirty() {
        boolean old = this.dirty;
        this.dirty = false;
        return old;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
