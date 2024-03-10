package dev.isxander.controlify.controller;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.driver.global.GlobalDriver;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GenericControllerConfig implements ConfigClass {
    public static final ResourceLocation ID = Controlify.id("config/generic");

    @Nullable
    public String nickname = null;

    public boolean autoJump = false;
    public boolean toggleSprint = true;
    public boolean toggleSneak = true;
    public boolean disableFlyDrifting = false;

    public boolean showIngameGuide = true;
    public boolean ingameGuideBottom = false;
    public boolean showScreenGuides = true;

    public boolean showOnScreenKeyboard = GlobalDriver.get().onScreenKeyboard().isOnScreenKeyboardSupported();
    public float chatKeyboardHeight = showOnScreenKeyboard ? 0.5f : 0f;

    public ResourceLocation[] radialActions = new ResourceLocation[8];
    public int radialButtonFocusTimeoutMs = 1000;

    public boolean dontShowControllerSubmission = false;

    @Override
    public void onConfigSaveLoad(ControllerEntity controller) {
        this.validateRadialActions(controller.bindings());
    }

    public boolean validateRadialActions(ControllerBindings bindings) {
        boolean changed = false;
        for (int i = 0; i < radialActions.length; i++) {
            ResourceLocation action = radialActions[i];
            if (!RadialMenuScreen.EMPTY_ACTION.equals(action) && (action == null || !bindings.registry().containsKey(action) || bindings.registry().get(action).radialIcon().isEmpty())) {
                setDefaultRadialAction(bindings, i);
                changed = true;
            }
        }
        if (changed)
            Controlify.instance().config().setDirty();

        return !changed;
    }

    private void setDefaultRadialAction(ControllerBindings bindings, int index) {
        radialActions[index] = switch (index) {
            case 0 -> bindings.TOGGLE_HUD_VISIBILITY.id();
            case 1 -> bindings.CHANGE_PERSPECTIVE.id();
            case 2 -> bindings.DROP_STACK.id();
            case 3 -> bindings.OPEN_CHAT.id();
            case 4 -> bindings.SWAP_HANDS.id();
            case 5 -> bindings.PICK_BLOCK.id();
            case 6 -> bindings.TAKE_SCREENSHOT.id();
            case 7 -> bindings.SHOW_PLAYER_LIST.id();
            default -> RadialMenuScreen.EMPTY_ACTION;
        };
    }
}
