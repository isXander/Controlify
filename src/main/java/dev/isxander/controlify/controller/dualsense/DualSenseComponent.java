package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ECSComponent;
import net.minecraft.resources.ResourceLocation;

public class DualSenseComponent implements ECSComponent {
    public static final ResourceLocation ID = Controlify.id("dualsense");

    private boolean muteLight;
    private boolean muteLightDirty;

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
        boolean dirty = this.muteLightDirty;
        this.muteLightDirty = false;
        return dirty;
    }
}
