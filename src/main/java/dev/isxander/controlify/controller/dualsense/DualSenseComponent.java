package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.driver.sdl.dualsense.DS5EffectsState;
import dev.isxander.controlify.driver.sdl.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class DualSenseComponent implements ECSComponent {
    public static final ResourceLocation ID = CUtil.rl("dualsense");

    private boolean muteLight = false;
    private boolean muteLightDirty = true;

    private DualsenseTriggerEffect leftTriggerEffect = new DualsenseTriggerEffect.Off();
    private boolean leftDirty = true;
    private DualsenseTriggerEffect rightTriggerEffect = new DualsenseTriggerEffect.Off();
    private boolean rightDirty = true;

    public void setLeftTriggerEffect(DualsenseTriggerEffect effect) {
        this.leftTriggerEffect = effect;
        this.leftDirty = true;
    }

    public DualsenseTriggerEffect getLeftTriggerEffect() {
        return this.leftTriggerEffect;
    }

    public boolean consumeLeftTriggerDirty() {
        boolean wasDirty = this.leftDirty;
        this.leftDirty = false;
        return wasDirty;
    }

    public void setRightTriggerEffect(DualsenseTriggerEffect effect) {
        this.rightTriggerEffect = effect;
        this.rightDirty = true;
    }

    public DualsenseTriggerEffect getRightTriggerEffect() {
        return this.rightTriggerEffect;
    }

    public boolean consumeRightTriggerDirty() {
        boolean wasDirty = this.rightDirty;
        this.rightDirty = false;
        return wasDirty;
    }

    public void setMuteLight(boolean on) {
        if (this.muteLight != on) {
            this.muteLight = on;
            this.muteLightDirty = true;
        }
    }

    public boolean getMuteLight() {
        return this.muteLight;
    }

    public boolean consumeMuteLightDirty() {
        boolean wasDirty = this.muteLightDirty;
        this.muteLightDirty = false;
        return wasDirty;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
