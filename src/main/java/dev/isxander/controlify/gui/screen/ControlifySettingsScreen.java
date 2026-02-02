package dev.isxander.controlify.gui.screen;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.gui.components.PlainTextWidget;
import dev.isxander.controlify.mixins.feature.ui.AbstractSelectionListAccessor;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.render.Blit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ControlifySettingsScreen extends Screen implements ScreenControllerEventListener {
    private final @Nullable Screen parent;

    private Button globalSettingsButton, doneButton, controllerNotDetectedButton;

    private int mainPaneHeight, footerY, footerHeight;

    public ControlifySettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("controlify.gui.carousel.title"));
        this.parent = parent;
    }

    public static ControlifySettingsScreen openScreen(@Nullable Screen parent) {
        var screen = new ControlifySettingsScreen(parent);
        Minecraft.getInstance().setScreen(screen);
        return screen;
    }

    @Override
    protected void init() {
        this.footerY = Mth.roundToward(this.height - 36 - 2, 2);
        this.footerHeight = this.height - this.footerY;
        this.mainPaneHeight = this.footerY;


        // Top Links
        Component donateText = Component.translatable("controlify.gui.carousel.donate")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        PlainTextButton donateBtn = this.addRenderableWidget(new PlainTextButton(3, 3, 100, 11, donateText, btn -> {
            Util.getPlatform().openUri("https://patreon.com/isxander");
        }, font));
        donateBtn.setTabOrderGroup(2);

        Component artCreditText = Component.translatable("controlify.gui.carousel.art_credit", Component.literal("Andrew Grant"))
                .withStyle(ChatFormatting.DARK_GRAY);
        int artCreditTextWidth = font.width(artCreditText);
        PlainTextButton artCreditBtn = this.addRenderableWidget(new PlainTextButton(width - artCreditTextWidth - 3, 3, artCreditTextWidth, 11, artCreditText, btn -> {
            Util.getPlatform().openUri("https://github.com/Andrew6rant");
        }, font));
        artCreditBtn.setTabOrderGroup(2);


        // Controller Not Detected Button
        controllerNotDetectedButton = this.addRenderableWidget(
                Button.builder(
                                Component.translatable("controlify.gui.carousel.controller_not_detected_btn"),
                                btn -> Util.getPlatform().openUri("https://docs.isxander.dev/controlify/users/controller-issues#my-controller-is-not-detected")
                )
                        .pos(width / 2 - 75, (this.height - 36) / 2 + 10)
                        .tooltip(Tooltip.create(Component.translatable("controlify.gui.carousel.controller_not_detected_btn.tooltip")))
                        .build()
        );
        controllerNotDetectedButton.visible = !hasController();
        FrameLayout.centerInRectangle(controllerNotDetectedButton, 0, 0, this.width, this.mainPaneHeight);


        // Footer
        GridLayout grid = new GridLayout().columnSpacing(10);
        GridLayout.RowHelper rowHelper = grid.createRowHelper(2);
        globalSettingsButton = rowHelper.addChild(
                Button.builder(
                        Component.translatable("controlify.gui.global_settings.title"),
                        btn -> minecraft.setScreen(GlobalSettingsScreenFactory.createGlobalSettingsScreen(this))
                ).build()
        );
        doneButton = rowHelper.addChild(
                Button.builder(
                        CommonComponents.GUI_DONE,
                        btn -> this.onClose()
                ).build()
        );
        grid.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });
        grid.arrangeElements();
        FrameLayout.centerInRectangle(grid, 0, this.footerY, this.width, this.footerHeight);


        // Main Pane
        if (!this.controllerNotDetectedButton.visible) {
            int playerSlotHeight = mainPaneHeight - 30;
            int playerSlotWidth = this.width - 20;
            if (Controlify.instance().config().getSettings().globalSettings().showSplitscreenAd && false) {
                ProfileSlotEntry profileSlotEntry = new ProfileSlotEntry(
                        0,
                        10, 15,
                        playerSlotWidth / 2 - 5, playerSlotHeight
                );
                this.addRenderableWidget(profileSlotEntry);
                SplitscreenAdvertisementSlotEntry splitscreenAdEntry = new SplitscreenAdvertisementSlotEntry(
                        10 + playerSlotWidth / 2 + 5, 15,
                        playerSlotWidth / 2 - 5, playerSlotHeight
                );
                this.addRenderableWidget(splitscreenAdEntry);
            } else {
                ProfileSlotEntry profileSlotEntry = new ProfileSlotEntry(
                        0,
                        10, 15,
                        playerSlotWidth, playerSlotHeight
                );
                this.addRenderableWidget(profileSlotEntry);
            }
        }

        ButtonGuideApi.addGuideToButton(globalSettingsButton, ControlifyBindings.GUI_ABSTRACT_ACTION_1, ButtonGuidePredicate.always());
        ButtonGuideApi.addGuideToButton(doneButton, ControlifyBindings.GUI_BACK, ButtonGuidePredicate.always());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.controllerNotDetectedButton.visible == hasController()) {
            this.repositionElements();
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Blit.tex(
                guiGraphics,
                minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR,
                0, footerY,
                0.0F, 0.0F,
                this.width, 2,
                32, 2
        );
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int i, int j, float f) {
        super.renderBackground(graphics, i, j, f);

        Blit.tex(
                graphics,
                minecraft.level == null ? AbstractSelectionListAccessor.getMenuListBackground() : AbstractSelectionListAccessor.getInWorldMenuListBackground(),
                0, 0,
                0f, 0f,
                width, footerY,
                32, 32
        );
    }

    @Override
    public void onControllerInput(ControllerEntity controller) {
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            globalSettingsButton.onPress(/*? if >=1.21.9 >>*/null );
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private boolean hasController() {
        return Controlify.instance().getControllerManager()
                .map(c -> !c.getConnectedControllers().isEmpty())
                .orElse(false);
    }

    private abstract static class SlotEntry extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        protected final int x, y, width, height;

        public SlotEntry(
                int x, int y, int width, int height
        ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(
                    this.x, this.y,
                    this.x + this.width, this.y + this.height,
                    0x55000000
            );
            guiGraphics./*? if >=1.21.9 && <1.21.11 {*//*submitOutline*//*?} else {*/renderOutline/*?}*/(x, y, width, height, 0x5AFFFFFF);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        @Override
        public @NonNull ScreenRectangle getRectangle() {
            return new ScreenRectangle(x, y, width, height);
        }
    }

    private class ProfileSlotEntry extends SlotEntry {
        private static final Identifier KEYBOARD_MOUSE_SPRITE = CUtil.rl("keyboard_mouse");

        private final int playerIndex;
        private @Nullable ControllerEntity controller;

        private final Button settingsButton;
        private final PlainTextWidget controllerNameText;
        private final ImageWidget controllerIcon;
        private final GridLayout gridLayout;

        private final ImmutableList<? extends GuiEventListener> children;

        public ProfileSlotEntry(
                int playerIndex,
                int x, int y, int width, int height
        ) {
            super(x, y, width, height);
            this.playerIndex = playerIndex;

            this.settingsButton = Button.builder(Component.translatable("controlify.gui.carousel.entry.settings"), btn -> onSettingsButtonPressed()).build();
            this.controllerNameText = new PlainTextWidget(Component.empty());
            this.controllerIcon = ImageWidget.sprite(64, 64, KEYBOARD_MOUSE_SPRITE);
            this.children = ImmutableList.of(this.settingsButton, this.controllerNameText, this.controllerIcon);

            this.gridLayout = new GridLayout().spacing(10);
            this.gridLayout.defaultCellSetting().alignHorizontallyCenter();
            var rowHelper = this.gridLayout.createRowHelper(1);
            rowHelper.addChild(this.controllerIcon);
            rowHelper.addChild(this.controllerNameText);
            rowHelper.addChild(this.settingsButton);

            updateControllerInfo(true);

            this.gridLayout.arrangeElements();
            FrameLayout.centerInRectangle(this.gridLayout, x, y, width, height);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);

            updateControllerInfo(false);

            this.settingsButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.controllerNameText.render(guiGraphics, mouseX, mouseY, partialTick);
            this.controllerIcon.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        private void onSettingsButtonPressed() {
            ProfileSettings profileSettings = Controlify.instance().config().getSettings().getProfileSettings(playerIndex);
            ProfileSettings defaultSettings = ProfileSettings.createDefault(
                    this.controller != null ? this.controller.info().type().namespace() : null
            );

            var screen = ControllerConfigScreenFactory.generateConfigScreen(
                    ControlifySettingsScreen.this,
                    profileSettings,
                    defaultSettings,
                    this.controller
            );
            minecraft.setScreen(screen);
        }

        private void updateControllerInfo(boolean force) {
            var currentController = getController();
            if (this.controller == currentController && !force) {
                return;
            }

            this.controller = currentController;
            if (this.controller != null) {
                this.controllerNameText.setMessage(Component.literal(this.controller.name()));
                this.controllerIcon.updateResource(this.controller.info().type().getIconSprite());
            } else {
                this.controllerNameText.setMessage(Component.translatable("controlify.gui.carousel.entry.keyboard_mouse"));
                this.controllerIcon.updateResource(KEYBOARD_MOUSE_SPRITE);
            }

            this.gridLayout.arrangeElements();
            FrameLayout.centerInRectangle(this.gridLayout, x, y, width, height);
        }

        private @Nullable ControllerEntity getController() {
            return Controlify.instance().getCurrentController().orElse(null);
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public @NonNull NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }


        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(NarratedElementType.TITLE, this.controllerNameText.getMessage());
        }
    }

    private class SplitscreenAdvertisementSlotEntry extends SlotEntry {
        private final PlainTextWidget adText;
        private final Button adButton;
        private final PlainTextButton disableAdButton;

        private final ImmutableList<? extends GuiEventListener> children;

        public SplitscreenAdvertisementSlotEntry(
                int x, int y, int width, int height
        ) {
            super(x, y, width, height);

            this.adText = new PlainTextWidget(
                    Component.literal("Try Splitscreen preview!")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
            );
            this.adButton = Button.builder(
                    Component.literal("Donate on Patreon"),
                    btn -> Util.getPlatform().openUri("https://patreon.com/isxander")
            ).build();
            Component disableAdText = Component.literal("Don't show this again")
                    .withStyle(ChatFormatting.DARK_GRAY);
            this.disableAdButton = new PlainTextButton(
                    0, 0, font.width(disableAdText), 11,
                    disableAdText,
                    btn -> {
                        Controlify.instance().config().getSettings().globalSettings().showSplitscreenAd = false;
                        Controlify.instance().config().saveSafely();
                        ControlifySettingsScreen.this.rebuildWidgets();
                    },
                    font
            );
            this.children = ImmutableList.of(adText, adButton, disableAdButton);

            var grid = new GridLayout().spacing(10);
            grid.defaultCellSetting().alignHorizontallyCenter();
            var rowHelper = grid.createRowHelper(1);
            rowHelper.addChild(adText);
            rowHelper.addChild(adButton);
            rowHelper.addChild(disableAdButton);
            grid.arrangeElements();
            FrameLayout.centerInRectangle(grid, x, y, width, height);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);

            this.adText.render(guiGraphics, mouseX, mouseY, partialTick);
            this.adButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.disableAdButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return this.children;
        }


        @Override
        public @NonNull NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@NonNull NarrationElementOutput narrationElementOutput) {

        }
    }
}
