package dev.isxander.controlify.gui.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.GenericControllerConfig;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.gui.components.FakePositionPlainTextButton;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ClientUtils;
import dev.isxander.controlify.utils.animation.api.Animatable;
import dev.isxander.controlify.utils.animation.api.Animation;
import dev.isxander.controlify.utils.animation.api.EasingFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
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
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ControllerCarouselScreen extends Screen implements ScreenControllerEventListener {
    public static final ResourceLocation CHECKMARK =
            /*? if >=1.20.3 {*/
            CUtil.mcRl("icon/checkmark");
            /*?} else {*/
            /*CUtil.mcRl("textures/gui/checkmark.png");
            *//*?}*/

    private final Screen parent;
    private int footerY;

    private List<CarouselEntry> carouselEntries = null;
    private int carouselIndex;
    private Animatable carouselAnimation = null;

    private final Controlify controlify;
    private final ControllerManager controllerManager;

    private Button globalSettingsButton, doneButton;
    private Button controllerNotDetectedButton;

    private ControllerCarouselScreen(Screen parent) {
        super(Component.translatable("controlify.gui.carousel.title"));
        this.parent = parent;

        this.controlify = Controlify.instance();
        this.controllerManager = controlify.getControllerManager().orElseThrow();

        this.carouselIndex = controlify.getCurrentController().map(c -> controllerManager.getConnectedControllers().indexOf(c)).orElse(0);
    }

    public static void openConfigScreen(Screen parent) {
        var controlify = Controlify.instance();

        controlify.finishControlifyInit().whenComplete((v, th) -> {
            Minecraft.getInstance().setScreen(new ControllerCarouselScreen(parent));
        });
    }

    @Override
    protected void init() {
        refreshControllers();

        Component donateText = Component.translatable("controlify.gui.carousel.donate")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        FakePositionPlainTextButton donateBtn = this.addRenderableWidget(new FakePositionPlainTextButton(donateText, font, 3, 3, btn -> {
            Util.getPlatform().openUri("https://ko-fi.com/isxander");
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
            prevSelectedController = carouselEntries.get(carouselIndex).controller;
        } else {
            prevSelectedController = null;
        }

        carouselEntries = controllerManager.getConnectedControllers().stream()
                .map(c -> new CarouselEntry(c, this.width / 3, this.height - 66))
                .peek(this::addRenderableWidget)
                .toList();
        carouselIndex = carouselEntries.stream()
                .filter(e -> e.controller == prevSelectedController)
                .findFirst()
                .map(carouselEntries::indexOf)
                .orElse(Controlify.instance().getCurrentController()
                        .map(c -> controllerManager.getConnectedControllers().indexOf(c))
                        .orElse(0)
                );
        if (!carouselEntries.isEmpty())
            carouselEntries.get(carouselIndex).overlayColor = 0;

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
        /*? if =1.20.4 {*/
        /*renderBackground(graphics, mouseX, mouseY, delta);
        *//*?} else <1.20.4 {*/
        /*renderBackground(graphics);
        *//*?}*/
        super.render(graphics, mouseX, mouseY, delta);

        RenderSystem.enableBlend();
        graphics.blit(
                /*? if >1.20.4 {*/
                minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR,
                /*?} else {*/
                /*CreateWorldScreen.FOOTER_SEPERATOR,
                *//*?}*/
                0, footerY,
                0.0F, 0.0F,
                this.width, 2,
                32, 2
        );
        RenderSystem.disableBlend();

        if (carouselEntries.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("controlify.gui.carousel.no_controllers"), this.width / 2, (this.height - 36) / 2 - 10, 0xFFAAAAAA);
        }
    }

    @Override
    public void renderBackground(
            GuiGraphics graphics
            /*? if >=1.20.4 {*/
            , int i, int j, float f
            /*?}*/
    ) {
        /*? if >1.20.4 {*/
        super.renderBackground(graphics, i, j, f);

        RenderSystem.enableBlend();
        graphics.blit(
                minecraft.level == null ? AbstractSelectionList.MENU_LIST_BACKGROUND : AbstractSelectionList.INWORLD_MENU_LIST_BACKGROUND,
                0, 0,
                0f, 0f,
                width, footerY,
                32, 32
        );
        RenderSystem.disableBlend();
        /*?} else {*/
        /*graphics.setColor(0.5f, 0.5f, 0.5f, 1f);
        graphics.blit(CreateWorldScreen.LIGHT_DIRT_BACKGROUND, 0, 0, 0, 0f, 0f, this.width, footerY, 32, 32);
        graphics.setColor(1f, 1f, 1f, 1f);

        graphics.blit(CreateWorldScreen.LIGHT_DIRT_BACKGROUND, 0, footerY, 0, 0f, 0f, this.width, this.height - footerY, 32, 32);
        *//*?}*/

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
            animation.consumerF(t -> entry.overlayColor = FastColor.ARGB32.lerp(t, entry.overlayColor, selected ? 0 : 0x90000000), 0f, 1f);
        }
        carouselAnimation = animation.play();
    }

    @Override
    public void onControllerInput(ControllerEntity controller) {
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
            globalSettingsButton.onPress();
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private class CarouselEntry extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
        private int x, y;
        private final int width, height;
        private float translationX, translationY;

        private final ControllerEntity controller;
        private final boolean hasNickname;

        private final Button useControllerButton;
        private final Button settingsButton;
        private final ImmutableList<? extends GuiEventListener> children;

        private boolean prevUse;
        private float currentlyUsedPos;
        private Animation currentlyUsedAnimation;

        private int overlayColor = 0x90000000;

        private boolean hovered = false;

        private CarouselEntry(ControllerEntity controller, int width, int height) {
            this.width = width;
            this.height = height;

            this.controller = controller;
            this.hasNickname = this.controller.genericConfig().config().nickname != null;

            this.settingsButton = Button.builder(Component.translatable("controlify.gui.carousel.entry.settings"), btn -> minecraft.setScreen(ControllerConfigScreenFactory.generateConfigScreen(ControllerCarouselScreen.this, controller))).width((getWidth() - 2) / 2 - 2).build();
            this.useControllerButton = Button.builder(Component.translatable("controlify.gui.carousel.entry.use"), btn -> Controlify.instance().setCurrentController(controller, true)).width(settingsButton.getWidth()).build();
            this.children = ImmutableList.of(settingsButton, useControllerButton);

            this.prevUse = isCurrentlyUsed();
            this.currentlyUsedPos = prevUse ? 0 : -1;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            hovered = isMouseOver(mouseX, mouseY);

            graphics.enableScissor(x, y, x + width + (translationX > 0 ? 1 : 0), y + height + (translationY > 0 ? 1 : 0));

            graphics.pose().pushPose();
            graphics.pose().translate(translationX, translationY, 0);

            //graphics.blit(CreateWorldScreen.LIGHT_DIRT_BACKGROUND, x, y, 0, 0f, 0f, width, height, 32, 32);

            graphics.renderOutline(x, y, width, height, 0x5AFFFFFF);
            useControllerButton.render(graphics, mouseX, mouseY, delta);
            settingsButton.render(graphics, mouseX, mouseY, delta);

            graphics.drawCenteredString(font, controller.name(), x + width / 2, y + height - 26 - font.lineHeight - (hasNickname ? font.lineHeight + 1 : 0), 0xFFFFFF);
            if (hasNickname) {
                GenericControllerConfig config = this.controller.genericConfig().config();
                String nickname = config.nickname;
                config.nickname = null;
                graphics.drawCenteredString(font, controller.name(), x + width / 2, y + height - 26 - font.lineHeight, 0xAAAAAA);
                config.nickname = nickname;
            }

            Component currentlyInUseText = Component.translatable("controlify.gui.carousel.entry.in_use").withStyle(ChatFormatting.GREEN);
            graphics.pose().pushPose();
            graphics.pose().translate((4 + 9 + 4 + font.width(currentlyInUseText)) * currentlyUsedPos, 0, 0);

            if (currentlyUsedPos > -1) {
                ClientUtils.drawSprite(graphics, CHECKMARK, x + 4, y + 4, 9, 8);
                graphics.drawString(font, currentlyInUseText, x + 17, y + 4, -1);
            }
            graphics.pose().popPose();

            int iconWidth = width - 6;
            //                      buttons   4px padding top  currently in use       controller name                                   image padding
            int iconHeight = height - 22       - 4             - font.lineHeight - 8  - (font.lineHeight * (hasNickname ? 2 : 1) + 1) - 6;
            int iconSize = Mth.roundToward(Math.min(iconHeight, iconWidth), 2);

            graphics.pose().pushPose();
            graphics.pose().translate(x + width / 2f - iconSize / 2f, y + font.lineHeight + 12 + iconHeight / 2f - iconSize / 2f, 0);
            graphics.pose().scale(iconSize / 64f, iconSize / 64f, 1);
            ClientUtils.drawSprite(graphics, controller.info().type().getIconSprite(), 0, 0, 64, 64);
            graphics.pose().popPose();

            graphics.pose().translate(0, 0, 1);
            graphics.fill(x, y, x + width, y + height, overlayColor);

            graphics.pose().popPose();

            graphics.disableScissor();

            if (prevUse != isCurrentlyUsed()) {
                if (currentlyUsedAnimation != null)
                    currentlyUsedAnimation.skipToEnd();
                currentlyUsedAnimation = Animation.of(20)
                        .easing(EasingFunction.EASE_OUT_QUINT)
                        .consumerF(t -> currentlyUsedPos = t, currentlyUsedPos, isCurrentlyUsed() ? 0 : -1)
                        .play();
            }
            prevUse = isCurrentlyUsed();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                int index = carouselEntries.indexOf(this);
                if (index != carouselIndex) {
                    if (carouselAnimation == null || carouselAnimation.isDone())
                        focusOnEntry(index);

                    return true;
                }
            }

            return super.mouseClicked(mouseX, mouseY, button);
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
            this.x = (int)x;
            this.settingsButton.setX((int)x + 2);
            this.useControllerButton.setX(this.settingsButton.getX() + this.settingsButton.getWidth() + 2);
            this.translationX = x - (int)x;
        }

        public void setY(float y) {
            this.y = (int)y;
            this.useControllerButton.setY((int)y + getHeight() - 20 - 2);
            this.settingsButton.setY(this.useControllerButton.getY());
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

        public boolean isCurrentlyUsed() {
            return Controlify.instance().getCurrentController().orElse(null) == controller;
        }

        @Override
        public NarrationPriority narrationPriority() {
            return isFocused() ? NarrationPriority.FOCUSED : hovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput builder) {
            builder.add(NarratedElementType.TITLE, controller.name());
            builder.add(NarratedElementType.USAGE, Component.literal("Left arrow to go to previous controller, right arrow to go to next controller."));
        }
    }
}
