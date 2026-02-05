package dev.isxander.splitscreen.client.host.gui;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.gui.components.PlainTextWidget;
import dev.isxander.controlify.gui.screen.ControlifySettingsScreen;
import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.SplitscreenPosition;
import dev.isxander.splitscreen.client.config.SplitscreenConfig;
import dev.isxander.splitscreen.client.integrations.ControlifyExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ControlifySplitscreenSettingsScreen extends ControlifySettingsScreen {

    private PlainTextWidget addPlayerHintWidget;
    private Component[] addPlayerHintLabels;
    private int addPlayerHintCurrentLabelIndex;
    private float addPlayerHintLabelTimer;

    public ControlifySplitscreenSettingsScreen(@Nullable Screen parent) {
        super(parent);
    }

    @Override
    protected void arrangeGrid(int paneX, int paneY, int paneWidth, int paneHeight, int padding) {
        int pawnCount = SplitscreenBootstrapper.getController()
                .map(c -> c.getPawnCount(true))
                .orElse(1);

        boolean preferHorizontal = !SplitscreenConfig.INSTANCE.preferVerticalSplitscreen.get();
        SplitscreenPosition.Visible[] positions = SplitscreenPosition.Visible.arrangeForN(pawnCount, preferHorizontal);

        for (int i = 0; i < positions.length; i++) {
            SplitscreenPosition.Visible pos = positions[i];

            ScreenRectangle slotRect = pos.applyToRealDims(paneX, paneY, paneWidth, paneHeight, padding, padding);

            this.addRenderableWidget(
                    new SplitscreenProfileSlotEntry(
                            this,
                            i,
                            slotRect.left(), slotRect.top(), slotRect.width(), slotRect.height()
                    )
            );
        }
    }

    @Override
    protected void addTopRightLink() {
        if (ControlifyExtension.getAvailableControllers().findAny().isEmpty()) {
            super.addTopRightLink();
            return;
        }

        this.addPlayerHintWidget = this.addRenderableWidget(
                new PlainTextWidget(Component.empty())
        );

        this.addPlayerHintWidget.setY(3);

        this.addPlayerHintLabels = ControlifyExtension.getAvailableControllers()
                .map(controller -> Controlify.instance().inputFontMapper()
                        .getComponentFromBinding(controller.info().type().namespace(), ControlifyExtension.ADD_PLAYER_BIND.onOrNull(controller)))
                .map(glyph -> Component.translatable(
                        "controlify.splitscreen.join_prompt",
                        glyph
                ))
                .toArray(Component[]::new);
        this.addPlayerHintCurrentLabelIndex = 0;
        this.addPlayerHintLabelTimer = 0f;

        this.updateJoinHintText();
    }

    public void onAvailableControllerPressJoin(ControllerEntity controller, boolean onlyController) {
        // bootstrap if we haven't already bootstrapped
        if (!SplitscreenBootstrapper.isSplitscreen()) {
            SplitscreenBootstrapper.boostrapAsController(
                    this.minecraft,
                    this.choosePrimaryInputMethod(controller, onlyController)
            );
        }

        SplitscreenBootstrapper.getController().orElseThrow()
                .summonNewPawnClient(InputMethod.controller(controller.uid()));
    }

    private InputMethod choosePrimaryInputMethod(ControllerEntity joiningController, boolean onlyController) {
        // if the joining controller is the only controller connected,
        // use KB&M for player 1
        if (onlyController) {
            return InputMethod.keyboardAndMouse();
        }

        // if the current input mode is KB&M,
        // use KB&M for player 1
        if (Controlify.instance().currentInputMode() == InputMode.KEYBOARD_MOUSE) {
            return InputMethod.keyboardAndMouse();
        }

        // if the current controller is not the joining controller, use the current controller for player 1
        ControllerEntity currentController = Controlify.instance().getCurrentController().orElseThrow();
        if (currentController != joiningController) {
            return InputMethod.controller(currentController.uid());
        }

        // if the current controller is the joining controller,
        // use KB&M for player 1
        return InputMethod.keyboardAndMouse();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.addPlayerHintWidget != null) {
            this.addPlayerHintLabelTimer += partialTick;
            if (this.addPlayerHintLabelTimer >= 20f * 2f) { // change label every 2 seconds
                this.addPlayerHintLabelTimer = 0f;
                this.addPlayerHintCurrentLabelIndex = (this.addPlayerHintCurrentLabelIndex + 1) % this.addPlayerHintLabels.length;
                this.updateJoinHintText();
            }
        }
    }

    private void updateJoinHintText() {
        Component hint = this.addPlayerHintLabels[this.addPlayerHintCurrentLabelIndex];

        this.addPlayerHintWidget.setMessage(hint);

        this.addPlayerHintWidget.setX(this.width - this.addPlayerHintWidget.getWidth() - 3);
    }



    public static class SplitscreenProfileSlotEntry extends ProfileSlotEntry {

        public SplitscreenProfileSlotEntry(ControlifySettingsScreen screen, int playerIndex, int x, int y, int width, int height) {
            super(screen, playerIndex, x, y, width, height);
        }

        @Override
        protected @Nullable ControllerEntity getController() {
            return SplitscreenBootstrapper.getController()
                    .flatMap(c -> {
                        InputMethod inputMethod = c.getPawns().get(this.playerIndex).getAssociatedInputMethod();
                        return switch (inputMethod) {
                            case InputMethod.Controller controller -> controller.findControllerEntity();
                            case InputMethod.KeyboardAndMouse ignored -> Optional.empty();
                        };
                    })
                    .orElseGet(super::getController);
        }
    }
}
