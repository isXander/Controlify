package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.SelectWorldScreenAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.CommonComponents;

import java.util.function.Supplier;

public class JoinMultiplayerScreenProcessor extends ScreenProcessor<JoinMultiplayerScreen> {
    private final Supplier<ServerSelectionList> listSupplier;
    private final Supplier<Button> backButtonSupplier;
    private final Supplier<Button> directConnectButtonSupplier;
    private final Supplier<Button> addServerButtonSupplier;

    public JoinMultiplayerScreenProcessor(
            JoinMultiplayerScreen screen,
            Supplier<ServerSelectionList> listSupplier,
            Supplier<Button> backButtonSupplier,
            Supplier<Button> directConnectButtonSupplier,
            Supplier<Button> addServerButtonSupplier
    ) {
        super(screen);
        this.listSupplier = listSupplier;
        this.backButtonSupplier = backButtonSupplier;
        this.directConnectButtonSupplier = directConnectButtonSupplier;
        this.addServerButtonSupplier = addServerButtonSupplier;
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
            if (ControlifyBindings.GUI_BACK.on(controller).guiPressed().get()) {
                screen.setFocused(listSupplier.get());
                return;
            }
        }

        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).guiPressed().get()) {
            Button directConnectButton = this.directConnectButtonSupplier.get();
            if (directConnectButton != null) {
                playClackSound();
                directConnectButton.onPress(/*? if >=1.21.9 >>*/null );
            }
        }

        if (ControlifyBindings.GUI_ABSTRACT_ACTION_2.on(controller).guiPressed().get()) {
            Button addServerButton = this.addServerButtonSupplier.get();
            if (addServerButton != null) {
                playClackSound();
                addServerButton.onPress(/*? if >=1.21.9 >>*/null );
            }
        }

        super.handleButtons(controller);
    }

    @Override
    public void onWidgetRebuild() {
        Button backButton = backButtonSupplier.get();
        Button directConnectButton = directConnectButtonSupplier.get();
        Button addServerButton = addServerButtonSupplier.get();

        if (backButton != null) {
            ButtonGuideApi.addGuideToButton(
                    backButton,
                    ControlifyBindings.GUI_BACK,
                    ButtonGuidePredicate.always()
            );
        }

        if (directConnectButton != null) {
            ButtonGuideApi.addGuideToButton(
                    directConnectButton,
                    ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                    ButtonGuidePredicate.always()
            );
        }

        if (addServerButton != null) {
            ButtonGuideApi.addGuideToButton(
                    addServerButton,
                    ControlifyBindings.GUI_ABSTRACT_ACTION_2,
                    ButtonGuidePredicate.always()
            );
        }

        super.onWidgetRebuild();
    }
}
