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
        this.addTopLeftLink();
        this.addTopRightLink();


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
            int mainPaneX = 0;
            int mainPaneY = 11;
            int mainPaneHeight = this.mainPaneHeight - mainPaneY;

            int slotPadding = 10;

            this.arrangeGrid(mainPaneX, mainPaneY, this.width, mainPaneHeight, slotPadding);
        }

        ButtonGuideApi.addGuideToButton(globalSettingsButton, ControlifyBindings.GUI_ABSTRACT_ACTION_1, ButtonGuidePredicate.always());
        ButtonGuideApi.addGuideToButton(doneButton, ControlifyBindings.GUI_BACK, ButtonGuidePredicate.always());
    }

    protected void arrangeGrid(int paneX, int paneY, int paneWidth, int paneHeight, int padding) {
        int playerSlotHeight = paneHeight - (2 * padding);
        int playerSlotWidth = paneWidth - (2 * padding);
        if (Controlify.instance().config().getSettings().globalSettings().showSplitscreenAd) {
            ProfileSlotEntry profileSlotEntry = new ProfileSlotEntry(
                    this,
                    0,
                    paneX + padding, paneY + padding,
                    playerSlotWidth / 2 - (padding / 2), playerSlotHeight
            );
            this.addRenderableWidget(profileSlotEntry);
            SplitscreenAdvertisementSlotEntry splitscreenAdEntry = new SplitscreenAdvertisementSlotEntry(
                    this,
                    paneX + padding + playerSlotWidth / 2 + (padding / 2), paneY + padding,
                    playerSlotWidth / 2 - (padding / 2), playerSlotHeight
            );
            this.addRenderableWidget(splitscreenAdEntry);
        } else {
            ProfileSlotEntry profileSlotEntry = new ProfileSlotEntry(
                    this,
                    0,
                    paneX + padding, paneY + padding,
                    playerSlotWidth, playerSlotHeight
            );
            this.addRenderableWidget(profileSlotEntry);
        }
    }

    protected void addTopLeftLink() {
        Component donateText = Component.translatable("controlify.gui.carousel.donate")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        PlainTextButton donateBtn = this.addRenderableWidget(new PlainTextButton(3, 3, 100, 11, donateText, btn -> {
            Util.getPlatform().openUri("https://patreon.com/isxander");
        }, font));
        donateBtn.setTabOrderGroup(2);
    }

    protected void addTopRightLink() {
        Component artCreditText = Component.translatable("controlify.gui.carousel.art_credit", Component.literal("Andrew Grant"))
                .withStyle(ChatFormatting.DARK_GRAY);
        int artCreditTextWidth = font.width(artCreditText);
        PlainTextButton artCreditBtn = this.addRenderableWidget(new PlainTextButton(width - artCreditTextWidth - 3, 3, artCreditTextWidth, 11, artCreditText, btn -> {
            Util.getPlatform().openUri("https://github.com/Andrew6rant");
        }, font));
        artCreditBtn.setTabOrderGroup(2);
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

    public abstract static class SlotEntry extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        protected final ControlifySettingsScreen screen;
        protected final int x, y, width, height;

        public SlotEntry(
                ControlifySettingsScreen screen,
                int x, int y, int width, int height
        ) {
            this.screen = screen;
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

    public static class ProfileSlotEntry extends SlotEntry {
        protected static final Identifier KEYBOARD_MOUSE_SPRITE = CUtil.rl("keyboard_mouse");

        protected final int playerIndex;
        protected @Nullable ControllerEntity controller;

        protected final Button settingsButton;
        protected final PlainTextWidget controllerNameText;
        protected /*? if >=1.21.8>>*/final ImageWidget controllerIcon;
        protected final GridLayout gridLayout;

        protected final ImmutableList<? extends GuiEventListener> children;

        public ProfileSlotEntry(
                ControlifySettingsScreen screen,
                int playerIndex,
                int x, int y, int width, int height
        ) {
            super(screen, x, y, width, height);
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
            var controllerType = this.controller != null ? this.controller.info().type().namespace() : null;
            ProfileSettings profileSettings = Controlify.instance().config().getSettings()
                    .getOrCreateProfileSettings(playerIndex, controllerType);
            ProfileSettings defaultSettings = ProfileSettings.createDefault(controllerType);

            var screen = ControllerConfigScreenFactory.generateConfigScreen(
                    this.screen,
                    profileSettings,
                    defaultSettings,
                    this.controller
            );
            Minecraft.getInstance().setScreen(screen);
        }

        private void updateControllerInfo(boolean force) {
            var currentController = getController();
            if (this.controller == currentController && !force) {
                return;
            }

            this.controller = currentController;
            if (this.controller != null) {
                this.controllerNameText.setMessage(Component.literal(this.controller.name()));
                this.updateIcon(this.controller.info().type().getIconSprite());
            } else {
                this.controllerNameText.setMessage(Component.translatable("controlify.gui.carousel.entry.keyboard_mouse"));
                this.updateIcon(KEYBOARD_MOUSE_SPRITE);
            }

            this.gridLayout.arrangeElements();
            FrameLayout.centerInRectangle(this.gridLayout, x, y, width, height);
        }

        private void updateIcon(Identifier iconSprite) {
            //? if >=1.21.8 {
            this.controllerIcon.updateResource(iconSprite);
            //?} else {
            /*this.controllerIcon = ImageWidget.sprite(64, 64, iconSprite);
            *///?}
        }

        protected @Nullable ControllerEntity getController() {
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

    public static class SplitscreenAdvertisementSlotEntry extends SlotEntry {
        private final PlainTextWidget adText;
        private final Button adButton;
        private final PlainTextButton disableAdButton;

        private final ImmutableList<? extends GuiEventListener> children;

        public SplitscreenAdvertisementSlotEntry(
                ControlifySettingsScreen screen,
                int x, int y, int width, int height
        ) {
            super(screen, x, y, width, height);

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
                    0, 0, Minecraft.getInstance().font.width(disableAdText), 11,
                    disableAdText,
                    btn -> {
                        Controlify.instance().config().getSettings().globalSettings().showSplitscreenAd = false;
                        Controlify.instance().config().saveSafely();
                        this.screen.rebuildWidgets();
                    },
                    Minecraft.getInstance().font
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
