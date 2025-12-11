//? if sodium {
/*package dev.isxander.controlify.compatibility.sodium.screenop;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.compatibility.sodium.mixins.FlatButtonWidgetAccessor;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.screens.Screen;

public class SodiumGuiScreenProcessor extends ScreenProcessor<Screen> {
    private final SodiumScreenOperations operations;

    public SodiumGuiScreenProcessor(Screen screen, SodiumScreenOperations operations) {
        super(screen);
        this.operations = operations;
    }

    @Override
    protected void handleComponentNavigation(ControllerEntity controller) {
        super.handleComponentNavigation(controller);
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            ((FlatButtonWidgetAccessor) operations.controlify$getApplyButton()).invokeDoAction();
        }
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_2.on(controller).justPressed()) {
            ((FlatButtonWidgetAccessor) operations.controlify$getUndoButton()).invokeDoAction();
        }

        if (ControlifyBindings.GUI_NEXT_TAB.on(controller).justPressed()) {
            operations.controlify$nextPage();
        }

        if (ControlifyBindings.GUI_PREV_TAB.on(controller).justPressed()) {
            operations.controlify$prevPage();
        }

        super.handleButtons(controller);
    }

    @Override
    protected void setInitialFocus() {

    }

    public void onRebuildGUI() {
        ButtonGuideApi.addGuideToButton(
                operations.controlify$getApplyButton(),
                ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.always()
        );
        ButtonGuideApi.addGuideToButton(
                operations.controlify$getUndoButton(),
                ControlifyBindings.GUI_ABSTRACT_ACTION_2,
                ButtonGuidePredicate.always()
        );

        ButtonGuideApi.addGuideToButton(
                operations.controlify$getCloseButton(),
                ControlifyBindings.GUI_BACK,
                ButtonGuidePredicate.always()
        );
        
        super.onWidgetRebuild();
    }
}
*///?}
