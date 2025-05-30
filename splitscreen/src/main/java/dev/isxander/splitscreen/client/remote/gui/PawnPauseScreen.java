package dev.isxander.splitscreen.client.remote.gui;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.splitscreen.client.LocalSplitscreenPawn;
import dev.isxander.splitscreen.client.features.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.splitscreen.client.features.screenop.ScreenSplitscreenMode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class PawnPauseScreen extends Screen implements ScreenSplitscreenBehaviour, ScreenControllerEventListener {
    private static final int BTN_WIDTH = 204;
    private static final int BTN_PADDING = 4;
    private static final int BTN_HALF_WIDTH = BTN_WIDTH / 2 - BTN_PADDING;

    private final LocalSplitscreenPawn pawn;

    private Button returnToGame, advancements, stats, disconnectController;

    public PawnPauseScreen(LocalSplitscreenPawn pawn) {
        super(Component.translatable("menu.game"));
        this.pawn = pawn;
    }

    @Override
    protected void init() {
        GridLayout grid = new GridLayout();
        grid.defaultCellSetting().padding(BTN_PADDING, BTN_PADDING, BTN_PADDING, 0);
        GridLayout.RowHelper rows = grid.createRowHelper(2);

        // Return to Game
        rows.addChild(
                returnToGame = Button.builder(
                        Component.translatable("menu.returnToGame"),
                        button -> {
                            this.minecraft.setScreen(null);
                            this.minecraft.mouseHandler.grabMouse();
                        }
                ).width(BTN_WIDTH).build(),
                2, grid.newCellSettings().paddingTop(50)
        );
        ButtonGuideApi.addGuideToButton(returnToGame, ControlifyBindings.GUI_BACK, ButtonGuidePredicate.always());

        // Advancements
        rows.addChild(
                advancements = this.openScreenButton(
                        Component.translatable("gui.advancements"),
                        () -> new AdvancementsScreen(this.minecraft.player.connection.getAdvancements(), this)
                )
        );

        // Statistics
        rows.addChild(
                stats = this.openScreenButton(
                        Component.translatable("gui.stats"),
                        () -> new StatsScreen(this, this.minecraft.player.getStats())
                )
        );

        // Disconnect Controller
        rows.addChild(
                disconnectController = Button.builder(
                        Component.literal("Disconnect Controller"),
                        button -> {
                            button.active = false;
                            this.disconnectController();
                        }
                ).width(BTN_WIDTH).build(),
                2
        );
        ButtonGuideApi.addGuideToButton(disconnectController, () -> {
            if (disconnectController.isFocused()) {
                return ControlifyBindings.GUI_PRESS;
            } else {
                return ControlifyBindings.GUI_ABSTRACT_ACTION_2;
            }
        }, ButtonGuidePredicate.always());


        grid.arrangeElements();
        FrameLayout.alignInRectangle(grid, 0, 0, this.width, this.height, 0.5f, 0.25f);
        grid.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void onControllerInput(ControllerEntity controller) {
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_2.on(controller).guiPressed().get()) {
            this.setFocused(disconnectController);
        }
    }

    private void disconnectController() {
        this.pawn.closeGame();
    }

    private Button openScreenButton(Component message, Supplier<Screen> screenSupplier) {
        return Button.builder(message, button -> this.minecraft.setScreen(screenSupplier.get())).width(BTN_HALF_WIDTH).build();
    }

    @Override
    public ScreenSplitscreenMode getSplitscreenMode() {
        return ScreenSplitscreenMode.SPLITSCREEN;
    }
}
