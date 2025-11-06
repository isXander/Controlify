package dev.isxander.controlify.gui.screen;

import com.google.common.collect.ImmutableList;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.GenericControllerConfig;
import dev.isxander.controlify.controller.steamdeck.SteamDeckComponent;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.gui.components.FakePositionPlainTextButton;
import dev.isxander.controlify.mixins.feature.ui.AbstractSelectionListAccessor;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.render.Blit;
import dev.isxander.controlify.utils.ClientUtils;
import dev.isxander.controlify.utils.ColorUtils;
import dev.isxander.controlify.utils.animation.api.Animatable;
import dev.isxander.controlify.utils.animation.api.Animation;
import dev.isxander.controlify.utils.animation.api.EasingFunction;
import dev.isxander.controlify.utils.render.CGuiPose;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ControllerCarouselScreen extends Screen implements ScreenControllerEventListener {
    public static final ResourceLocation CHECKMARK = ResourceLocation.withDefaultNamespace("icon/checkmark");
    public static final ResourceLocation DANGER = ResourceLocation.withDefaultNamespace("icon/unseen_notification");

    private final Screen parent;
    private int footerY;

    private List<CarouselEntry> carouselEntries = null;
    private int carouselIndex;
    private Animatable carouselAnimation = null;

    private final Controlify controlify;
    private final ControllerManager controllerManager;

    private Button globalSettingsButton, unbindControllerButton, doneButton;
    private Button controllerNotDetectedButton;

    private ControllerCarouselScreen(Screen parent) {
        super(Component.translatable("controlify.gui.carousel.title"));
        this.parent = parent;

        this.controlify = Controlify.instance();
        this.controllerManager = controlify.getControllerManager().orElseThrow();

        this.carouselIndex = controlify.getCurrentController().map(c -> controllerManager.getConnectedControllers().indexOf(c)).orElse(0);
    }

    public static void openConfigScreen(Screen parent) {
        Minecraft.getInstance().setScreen(new ControllerCarouselScreen(parent));
    }

    @Override
    protected void init() {
        refreshControllers();

        Component donateText = Component.translatable("controlify.gui.carousel.donate")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        FakePositionPlainTextButton donateBtn = this.addRenderableWidget(new FakePositionPlainTextButton(donateText, font, 3, 3, btn -> {
            Util.getPlatform().openUri("https://patreon.com/isxander");
        }));
        donateBtn.setFakePosition(new ScreenRectangle(0, height, width, 1));

        Component artCreditText = Component.translatable("controlify.gui.carousel.art_credit", Component.literal("Andrew Grant"))
                .withStyle(ChatFormatting.DARK_GRAY);
        FakePositionPlainTextButton artCreditBtn = this.addRenderableWidget(new FakePositionPlainTextButton(artCreditText, font, width - font.width(artCreditText) - 3, 3, btn -> {
            Util.getPlatform().openUri("https://github.com/Andrew6rant");
        }));
        artCreditBtn.setFakePosition(new ScreenRectangle(0, height + 1, width, 1));

        GridLayout grid = new GridLayout().columnSpacing(10);
        GridLayout.RowHelper rowHelper = grid.createRowHelper(2);
        globalSettingsButton = rowHelper.addChild(Button.builder(Component.translatable("controlify.gui.global_settings.title"), btn -> minecraft.setScreen(GlobalSettingsScreenFactory.createGlobalSettingsScreen(this))).build());
        doneButton = rowHelper.addChild(Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose()).build());
        grid.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });
        grid.arrangeElements();
        FrameLayout.centerInRectangle(grid, 0, this.height - 36, this.width, 36);

        controllerNotDetectedButton = this.addRenderableWidget(
                Button.builder(
                        Component.translatable("controlify.gui.carousel.controller_not_detected_btn"),
                        btn -> Util.getPlatform().openUri("https://docs.isxander.dev/controlify/users/controller-issues#my-controller-is-not-detected")
                )
                        .pos(width / 2 - 75, (this.height - 36) / 2 + 10)
                        .tooltip(Tooltip.create(Component.translatable("controlify.gui.carousel.controller_not_detected_btn.tooltip")))
                        .build()
        );
        controllerNotDetectedButton.visible = carouselEntries.isEmpty();

        ButtonGuideApi.addGuideToButton(globalSettingsButton, ControlifyBindings.GUI_ABSTRACT_ACTION_1, ButtonGuidePredicate.always());
        ButtonGuideApi.addGuideToButton(doneButton, ControlifyBindings.GUI_BACK, ButtonGuidePredicate.always());

        this.footerY = Mth.roundToward(this.height - 36 - 2, 2);
    }

    public void refreshControllers() {
        ControllerEntity prevSelectedController;
        if (carouselEntries != null && !carouselEntries.isEmpty()) {
            carouselEntries.forEach(this::removeWidget);
            CarouselEntry prevEntry = carouselEntries.get(carouselIndex);
            if (prevEntry instanceof ControllerCarouselEntry cce) {
                prevSelectedController = cce.controller;
            } else {
                prevSelectedController = null;
            }
        } else {
            prevSelectedController = null;
        }

        carouselEntries = new ArrayList<>();

        controllerManager.getConnectedControllers().stream()
                .map(c -> new ControllerCarouselEntry(c, this.width / 3, this.height - 66))
                .forEach(entry -> {
                    carouselEntries.add(entry);
                });
        carouselIndex = carouselEntries.stream()
                .filter(e -> (e instanceof ControllerCarouselEntry ce && ce.controller == prevSelectedController) || (prevSelectedController == null && e instanceof KBMControllerCarouselEntry))
                .findFirst()
                .map(carouselEntries::indexOf)
                .orElse(Controlify.instance().getCurrentController()
                        .map(c -> controllerManager.getConnectedControllers().indexOf(c))
                        .orElse(0)
                );
        if (!carouselEntries.isEmpty()) {
            // insert the keyboard & mouse entry at the start
            // but only do it if there are other options available
            // so we can show the controllerNotDetected UI
            carouselEntries.addFirst(new KBMControllerCarouselEntry(this.width / 3, this.height - 66));

            carouselEntries.forEach(this::addRenderableWidget);
            carouselEntries.get(carouselIndex).overlayColor = 0;
        }

        float offsetX = (this.width / 2f) * -(carouselIndex - 1) - this.width / 6f;
        for (int i = 0; i < carouselEntries.size(); i++) {
            CarouselEntry entry = carouselEntries.get(i);
            entry.setX(offsetX + (this.width / 2f) * i);
            entry.setY(i == carouselIndex ? 20 : 10);
        }

        if (controllerNotDetectedButton != null)
            controllerNotDetectedButton.visible = carouselEntries.isEmpty();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        Blit.tex(
                graphics,
                minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR,
                0, footerY,
                0.0F, 0.0F,
                this.width, 2,
                32, 2
        );

        if (carouselEntries.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("controlify.gui.carousel.no_controllers"), this.width / 2, (this.height - 36) / 2 - 10, 0xFFAAAAAA);
        }
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

    public void focusOnEntry(int index) {
        if (carouselAnimation != null && !carouselAnimation.isDone())
            return;

        int diff = index - carouselIndex;
        if (diff == 0) return;

        carouselIndex = index;

        Animation animation = Animation.of(10)
                .easing(EasingFunction.EASE_IN_OUT_CUBIC);
        for (CarouselEntry entry : carouselEntries) {
            boolean selected = carouselEntries.indexOf(entry) == index;
            animation.consumerF(entry::setX, entry.getX(), entry.getX() + -diff * (this.width / 2f));
            animation.consumerF(entry::setY, entry.getY(), selected ? 20f : 10f);
            animation.consumerF(t -> entry.overlayColor = ColorUtils.lerpARGB(t, entry.overlayColor, selected ? 0 : 0x90000000), 0f, 1f);
        }
        carouselAnimation = animation.play();
    }

    @Override
    public void onControllerInput(ControllerEntity controller) {
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            globalSettingsButton.onPress(/*? if >=1.21.9 >>*/ null );
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private abstract class CarouselEntry extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        private int x, y;
        private final int width, height;
        private float translationX, translationY;

        private final Button useButton;
        private final @Nullable Button settingsButton;
        private final ImmutableList<? extends GuiEventListener> children;

        private boolean prevUse;
        private float currentlyUsedPos;
        private Animation currentlyUsedAnimation;

        private int overlayColor = 0x90000000;

        private boolean hovered = false;

        private CarouselEntry(int width, int height) {
            this.width = width;
            this.height = height;

            int buttonWidth = this.hasSettingsButton() ? (getWidth() - 2) / 2 - 2 : getWidth() - 4;
            this.settingsButton = !this.hasSettingsButton() ? null : Button.builder(Component.translatable("controlify.gui.carousel.entry.settings"), this::onSettingsButtonPressed).width(buttonWidth).build();
            this.useButton = Button.builder(Component.translatable("controlify.gui.carousel.entry.use"), this::onUseButtonPressed).width(buttonWidth).build();

            this.children = this.hasSettingsButton() ? ImmutableList.of(settingsButton, useButton) : ImmutableList.of(useButton);

            this.prevUse = isCurrentlyUsed();
            this.currentlyUsedPos = prevUse ? 1 : 0;
        }

        protected abstract Component getName();
        protected abstract Optional<Component> getNickname();

        protected abstract ResourceLocation getIconSprite();

        protected abstract boolean hasSettingsButton();
        protected abstract void onSettingsButtonPressed(Button button);

        protected abstract boolean isCurrentlyUsed();
        protected abstract void onUseButtonPressed();

        protected void doExtraRendering(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            hovered = isMouseOver(mouseX, mouseY);

            boolean hasNickname = getNickname().isPresent();

            graphics.enableScissor(x, y, x + width + (translationX > 0 ? 1 : 0), y + height + (translationY > 0 ? 1 : 0));

            var pose = CGuiPose.ofPush(graphics);
            pose.translate(translationX, translationY);

            //graphics.blit(CreateWorldScreen.LIGHT_DIRT_BACKGROUND, x, y, 0, 0f, 0f, width, height, 32, 32);

            graphics./*? if >=1.21.9 {*/submitOutline/*?} else {*//*renderOutline*//*?}*/(x, y, width, height, 0x5AFFFFFF);
            useButton.render(graphics, mouseX, mouseY, delta);
            if (this.hasSettingsButton()) {
                settingsButton.render(graphics, mouseX, mouseY, delta);
            }

            graphics.drawCenteredString(font, this.getName(), x + width / 2, y + height - 26 - font.lineHeight - (hasNickname ? font.lineHeight + 1 : 0), 0xFFFFFFFF);
            this.getNickname().ifPresent(nickname -> {
                graphics.drawCenteredString(font, nickname, x + width / 2, y + height - 26 - font.lineHeight, 0xFFAAAAAA);
            });

            Component currentlyInUseText = Component.translatable("controlify.gui.carousel.entry.in_use").withStyle(ChatFormatting.GREEN);
            pose.push();
            pose.translate((4 + 9 + 4 + font.width(currentlyInUseText)) * (currentlyUsedPos - 1), 0);

            if (currentlyUsedPos > 0) {
                ClientUtils.drawSprite(graphics, CHECKMARK, x + 4, y + 4, 9, 8);
                graphics.drawString(font, currentlyInUseText, x + 17, y + 4, -1);
            }
            this.doExtraRendering(graphics, mouseX, mouseY, delta);

            pose.pop();

            int iconWidth = width - 6;
            //                      buttons   4px padding top  currently in use       controller name                                   image padding
            int iconHeight = height - 22       - 4             - font.lineHeight - 8  - (font.lineHeight * (hasNickname ? 2 : 1) + 1) - 6;
            int iconSize = Mth.roundToward(Math.min(iconHeight, iconWidth), 2);

            pose.push();
            pose.translate(x + width / 2f - iconSize / 2f, y + font.lineHeight + 12 + iconHeight / 2f - iconSize / 2f);
            pose.scale(iconSize / 64f, iconSize / 64f);
            ClientUtils.drawSprite(graphics, this.getIconSprite(), 0, 0, 64, 64);
            pose.pop();

            pose.translate(0, 0);
            graphics.fill(x, y, x + width, y + height, overlayColor);

            pose.pop();

            graphics.disableScissor();

            if (prevUse != isCurrentlyUsed()) {
                if (currentlyUsedAnimation != null)
                    currentlyUsedAnimation.skipToEnd();
                currentlyUsedAnimation = Animation.of(20)
                        .easing(EasingFunction.EASE_OUT_QUINT)
                        .consumerF(t -> currentlyUsedPos = t, currentlyUsedPos, isCurrentlyUsed() ? 1 : 0)
                        .play();
            }
            prevUse = isCurrentlyUsed();
        }

        @Override
        //? if >=1.21.9 {
        public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
            double mouseX = mouseButtonEvent.x();
            double mouseY = mouseButtonEvent.y();
        //?} else {
        /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        *///?}
            if (isMouseOver(mouseX, mouseY)) {
                int index = carouselEntries.indexOf(this);
                if (index != carouselIndex) {
                    if (carouselAnimation == null || carouselAnimation.isDone())
                        focusOnEntry(index);

                    return true;
                }
            }

            //? if >=1.21.9 {
            return super.mouseClicked(mouseButtonEvent, doubleClick);
            //?} else {
            /*return super.mouseClicked(mouseX, mouseY, button);
            *///?}
        }

        private void onUseButtonPressed(Button button) {
            this.onUseButtonPressed();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (focused) {
                focusOnEntry(carouselEntries.indexOf(this));
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

        public void setX(float x) {
            this.x = (int) x;

            if (this.hasSettingsButton()) {
                this.settingsButton.setX((int) x + 2);
                this.useButton.setX(this.settingsButton.getX() + this.settingsButton.getWidth() + 2);
            } else {
                this.useButton.setX((int) x + 2);
            }
            this.translationX = x - (int)x;
        }

        public void setY(float y) {
            this.y = (int)y;

            int buttonRowY = (int)y + getHeight() - 20 - 2;
            this.useButton.setY(buttonRowY);
            if (this.hasSettingsButton()) {
                this.settingsButton.setY(buttonRowY);
            }

            this.translationY = y - (int)y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        @Nullable
        @Override
        public ComponentPath nextFocusPath(FocusNavigationEvent event) {
            if (carouselAnimation != null && !carouselAnimation.isDone())
                return null;
            return super.nextFocusPath(event);
        }

        @Override
        public ScreenRectangle getRectangle() {
            return new ScreenRectangle(x, y, width, height);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return isFocused() ? NarrationPriority.FOCUSED : hovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput builder) {
            builder.add(NarratedElementType.TITLE, getName());
            builder.add(NarratedElementType.USAGE, Component.literal("Left arrow to go to previous controller, right arrow to go to next controller."));
        }
    }

    private class ControllerCarouselEntry extends CarouselEntry {

        private final ControllerEntity controller;
        private final boolean badSteamDeck;

        private ControllerCarouselEntry(ControllerEntity controller, int width, int height) {
            super(width, height);
            this.controller = controller;

            // Check if the Steam Deck is loaded but not with dedicated driver (the component is only provided by the dedicated driver)
            this.badSteamDeck = controller.info().type().isSteamDeck()
                                && controller.getComponent(SteamDeckComponent.ID).isEmpty();
        }

        @Override
        protected Component getName() {
            return Component.literal(controller.name());
        }

        @Override
        protected Optional<Component> getNickname() {
            GenericControllerConfig config = this.controller.genericConfig().config();
            @Nullable String nickname = config.nickname;

            Component ogName = null;

            if (nickname != null) {
                config.nickname = null;
                ogName = Component.literal(controller.name());
                config.nickname = nickname;
            }

            return Optional.ofNullable(ogName);
        }

        @Override
        protected ResourceLocation getIconSprite() {
            return controller.info().type().getIconSprite();
        }

        @Override
        protected boolean hasSettingsButton() {
            return true;
        }

        @Override
        protected void onSettingsButtonPressed(Button button) {
            minecraft.setScreen(ControllerConfigScreenFactory.generateConfigScreen(ControllerCarouselScreen.this, controller));
        }

        @Override
        protected boolean isCurrentlyUsed() {
            return Controlify.instance().getCurrentController().orElse(null) == controller;
        }

        @Override
        protected void onUseButtonPressed() {
            Controlify.instance().setCurrentController(controller, true);
        }

        @Override
        protected void doExtraRendering(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            if (badSteamDeck) {
                ClientUtils.drawSprite(graphics, DANGER, this.getX() + 4, this.getY() + 4 + 10, 9, 8);
                graphics.drawString(font, Component.translatable("controlify.steam_deck.no_driver"), this.getX() + 17, this.getY() + 4 + 10, -1);
            }
        }
    }

    private class KBMControllerCarouselEntry extends CarouselEntry {

        private KBMControllerCarouselEntry(int width, int height) {
            super(width, height);
        }

        @Override
        protected Component getName() {
            return Component.translatable("controlify.gui.carousel.entry.keyboard_mouse");
        }

        @Override
        protected Optional<Component> getNickname() {
            return Optional.empty();
        }

        @Override
        protected ResourceLocation getIconSprite() {
            return CUtil.rl("keyboard_mouse");
        }

        @Override
        protected boolean hasSettingsButton() {
            return false;
        }

        @Override
        protected void onSettingsButtonPressed(Button button) {

        }

        @Override
        protected boolean isCurrentlyUsed() {
            return Controlify.instance().getCurrentController().orElse(null) == null;
        }

        @Override
        protected void onUseButtonPressed() {
            Controlify.instance().setCurrentController(null, true);
            Controlify.instance().config().setCurrentControllerUid(null);
        }
    }
}
