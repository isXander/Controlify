package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.dualsense.HapticEffects;
import dev.isxander.controlify.gui.guide.GuideAction;
import dev.isxander.controlify.gui.guide.GuideActionRenderer;
import dev.isxander.controlify.gui.layout.AnchorPoint;
import dev.isxander.controlify.gui.layout.PositionedComponent;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.sound.ControlifyClientSounds;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.animation.api.Animation;
import dev.isxander.controlify.utils.animation.api.EasingFunction;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RadialMenuScreen extends Screen implements ScreenControllerEventListener, ScreenProcessorProvider {
    public static final ResourceLocation EMPTY_ACTION = CUtil.rl("empty_action");

    private final ControllerEntity controller;
    private final @Nullable EditMode editMode;
    private final Screen parent;
    private final Component text;

    private final RadialItem[] items;
    private final RadialButton[] buttons;
    private float radialRadius;

    private final InputBinding openBind;

    private int selectedButton = -1;
    private int idleTicks;
    private final int idleTicksTimeout;
    private boolean isEditing;

    private ActionSelectList actionSelectList;

    private final Processor processor = new Processor(this);

    public RadialMenuScreen(ControllerEntity controller, InputBinding openBind, RadialItem[] items, Component text, @Nullable EditMode editMode, Screen parent) {
        super(text);
        this.text = text;
        this.controller = controller;
        this.items = items;
        this.buttons = new RadialButton[items.length];
        this.editMode = editMode;
        this.parent = parent;
        this.idleTicksTimeout = controller.input().orElseThrow().confObj().radialButtonFocusTimeoutTicks;
        this.openBind = openBind;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // get diameter of enclosing circle of a radial button
        // w = 16, h = 16
        // r = sqrt(w^2 + h^2) / 2
        float buttonRadius = (float) Math.sqrt(32 * 32 + 32 * 32) + 8;
        // add the amount of radii together to create the circumference of the circle they all fit around
        float circumference = buttonRadius * items.length;
        // c = 2 * pi * r
        radialRadius = Math.max(circumference / Mth.TWO_PI, 43);

        Animation animation = Animation.of(5)
                .easing(EasingFunction.EASE_OUT_QUAD);
        for (int i = 0; i < items.length; i++) {
            float angle = Mth.TWO_PI * i / items.length - (90 * Mth.DEG_TO_RAD);
            float x = centerX + Mth.cos(angle) * radialRadius;
            float y = centerY + Mth.sin(angle) * radialRadius;

            RadialButton button = buttons[i] = new RadialButton(items[i], centerX - 16, centerY - 16);

            animation
                    .consumerF(button::setX, centerX - 16, x - 16)
                    .consumerF(button::setY, centerY - 16, y - 16);

            addRenderableWidget(button);
        }
        animation.play();

        if (editMode != null) {
            var exitGuide = addRenderableWidget(new PositionedComponent<>(
                    new GuideActionRenderer<>(
                            new GuideAction<>(
                                    ControlifyBindings.GUI_BACK.on(controller),
                                    obj -> Optional.of(CommonComponents.GUI_DONE)
                            ),
                            false,
                            true
                    ),
                    AnchorPoint.BOTTOM_CENTER,
                    0, -10,
                    AnchorPoint.BOTTOM_CENTER
            ));

            exitGuide.getComponent().updateName(null);
            exitGuide.updatePosition(width, height);
        }
    }

    @Override
    public void onControllerInput(ControllerEntity controller) {
        if (this.controller != controller) return;

        if (editMode == null && !openBind.digitalNow()) {
            if (selectedButton != -1 && buttons[selectedButton].invoke()) {
                playClickSound();
            }

            onClose();
        }

        if (editMode != null && ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
            playClickSound();
            onClose();
        }

        if (!isEditing) {
            float x = ControlifyBindings.RADIAL_AXIS_RIGHT.on(controller).analogueNow()
                    - ControlifyBindings.RADIAL_AXIS_LEFT.on(controller).analogueNow();
            float y = ControlifyBindings.RADIAL_AXIS_DOWN.on(controller).analogueNow()
                    - ControlifyBindings.RADIAL_AXIS_UP.on(controller).analogueNow();
            float threshold = controller.input().orElseThrow().config().config().buttonActivationThreshold;

            if (Math.abs(x) >= threshold || Math.abs(y) >= threshold) {
                float angle = Mth.wrapDegrees(Mth.RAD_TO_DEG * (float) Mth.atan2(y, x) - 90f) + 180f;
                float each = 360f / buttons.length;

                int newSelected = Mth.floor((angle + each / 2f) / each) % buttons.length;
                if (newSelected != selectedButton) {
                    selectedButton = newSelected;
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ControlifyClientSounds.SCREEN_FOCUS_CHANGE.get(), 1f));
                    controller.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));
                }

                for (int i = 0; i < buttons.length; i++) {
                    boolean selected = i == selectedButton;
                    buttons[i].setFocused(selected);
                    if (selected) {
                        this.setFocused(buttons[i]);
                    }
                }

                idleTicks = 0;
            } else if (editMode == null) {
                idleTicks++;
                if (idleTicks >= idleTicksTimeout && selectedButton != -1) {
                    selectedButton = -1;
                    for (RadialButton button : buttons) {
                        button.setFocused(false);
                    }
                    controller.hdHaptics().ifPresent(haptics -> haptics.playHaptic(HapticEffects.NAVIGATE));
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (editMode != null) {
            /*? if >1.20.4 {*/
            renderBackground(graphics, mouseX, mouseY, delta);
            /*?} else {*/
            /*renderDirtBackground(graphics);
            *//*?}*/
        }

        super.render(graphics, mouseX, mouseY, delta);

        if (editMode == null) {
            graphics.drawCenteredString(
                    font,
                    text,
                    width / 2,
                    height - 39,
                    -1
            );
        }
    }

    private void playClickSound() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
    }

    private void finishEditing() {
        isEditing = false;
        removeWidget(actionSelectList);
        this.setFocused(null);
        actionSelectList = null;
    }

    @Override
    public void onClose() {
        Controlify.instance().config().saveIfDirty();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return editMode != null;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return this.processor;
    }

    public interface RadialItem {
        Component name();

        RadialIcon icon();

        boolean playAction();
    }

    public interface EditMode {
        void setRadialItem(int index, RadialItem item);

        List<RadialItem> getEditCandidates();
    }

    public class RadialButton implements Renderable, GuiEventListener, NarratableEntry, ComponentProcessor {
        public static final ResourceLocation TEXTURE = CUtil.rl("textures/gui/radial-buttons.png");

        private int x, y;
        private float translateX, translateY;
        private boolean focused;
        private RadialItem item;
        private MultiLineLabel name;

        private RadialButton(RadialItem item, float x, float y) {
            this.setX(x);
            this.setY(y);

            this.setAction(item);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            graphics.pose().pushPose();
            graphics.pose().translate(x + translateX, y + translateY, 0);

            graphics.pose().pushPose();
            graphics.pose().scale(2, 2, 1);
            graphics.blit(TEXTURE, 0, 0, focused ? 16 : 0, 0, 16, 16, 32, 16);
            graphics.pose().popPose();

            if (editMode == null || !focused) {
                graphics.pose().pushPose();
                graphics.pose().translate(4, 4, 0);
                graphics.pose().scale(1.5f, 1.5f, 1);
                this.item.icon().draw(graphics, 0, 0, delta);
                graphics.pose().popPose();
            } else {
                Component bind = ControlifyBindings.GUI_PRESS.on(controller).inputIcon();
                graphics.drawString(font, bind, 16 - font.width(bind) / 2, 16 - font.lineHeight / 2, -1);
            }

            graphics.pose().popPose();

            if (focused)
                name.renderCentered(graphics, width / 2, height / 2 - font.lineHeight / 2 - ((name.getLineCount() - 1) * font.lineHeight / 2));
        }

        public boolean invoke() {
            return this.item.playAction();
        }

        public void setAction(RadialItem item) {
            this.item = item;
            this.name = MultiLineLabel.create(font, item.name(), (int)(radialRadius * 2 - 32));
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(float x) {
            this.x = (int) x;
            this.translateX = x - this.x;
        }

        public void setY(float y) {
            this.y = (int) y;
            this.translateY = y - this.y;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
            if (editMode != null && controller == RadialMenuScreen.this.controller && ControlifyBindings.GUI_PRESS.on(controller).justPressed()) {
                RadialButton button = buttons[selectedButton];
                int x = button.x < width / 2 ? button.x - 110 : button.x + 42;
                actionSelectList = new ActionSelectList(selectedButton, x, button.y, 100, 80);
                addRenderableWidget(actionSelectList);
                RadialMenuScreen.this.setFocused(actionSelectList);
                isEditing = true;
                return true;
            }
            return false;
        }

        @Override
        public NarrationPriority narrationPriority() {
            return isFocused() ? NarrationPriority.FOCUSED : NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput builder) {
            builder.add(NarratedElementType.TITLE, item.name());
        }

        @Override
        public ScreenRectangle getRectangle() {
            return new ScreenRectangle(x, y, 32, 32);
        }
    }

    public class ActionSelectList implements Renderable, ContainerEventHandler, NarratableEntry, ComponentProcessor {
        private final int radialIndex;

        private int x, y;
        private int width, height;
        private final int itemHeight = 10;
        private int scrollOffset;

        private boolean focused;
        private ActionEntry focusedEntry;

        private final List<ActionEntry> children = new ArrayList<>();

        public ActionSelectList(int index, int x, int y, int width, int height) {
            this.radialIndex = index;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            for (RadialItem item : editMode.getEditCandidates()) {
                children.add(new ActionEntry(item));
            }

            RadialItem item = items[radialIndex];
            children.stream()
                    .filter(action -> action.item.equals(item))
                    .findAny()
                    .ifPresent(this::setFocused);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(x, y, x + width, y + height, 0x80000000);

            graphics.enableScissor(x, y, x + width, y + height);
            int y = this.y - scrollOffset;
            for (ActionEntry child : children) {
                child.render(graphics, x, y, width, itemHeight, mouseX, mouseY, delta);
                y += itemHeight;
            }
            graphics.disableScissor();

            graphics.renderOutline(x - 1, this.y - 1, width + 2, height + 2, 0x80ffffff);
        }

        @Override
        public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
            if (controller == RadialMenuScreen.this.controller) {
                if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
                    finishEditing();
                    return true;
                }
            }

            return false;
        }

        @Override
        public List<ActionEntry> children() {
            return children;
        }

        @Override
        public boolean isDragging() {
            return false;
        }

        @Override
        public void setDragging(boolean dragging) {

        }

        @Nullable
        @Override
        public ActionEntry getFocused() {
            return focusedEntry;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener child) {
            ActionEntry focus = (ActionEntry) child;
            this.focusedEntry = focus;

            if (focus != null) {
                int index = children().indexOf(child);
                if (index != -1) {
                    int focusY = index * itemHeight - scrollOffset;
                    if (focusY < 0)
                        scrollOffset = Mth.clamp(index * itemHeight, 0, children().size() * itemHeight - height);
                    else if (focusY + itemHeight > height)
                        scrollOffset = Mth.clamp(index * itemHeight + itemHeight - height, 0, children().size() * itemHeight - height);
                }
            }
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }

        @Override
        public NarrationPriority narrationPriority() {
            return focused ? NarrationPriority.FOCUSED : NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput builder) {
            if (getFocused() != null) {
                builder.add(NarratedElementType.TITLE, getFocused().item.name());
            }
        }

        public class ActionEntry implements GuiEventListener, ComponentProcessor {
            private int x, y;
            private boolean focused;
            private final RadialItem item;

            public ActionEntry(RadialItem item) {
                this.item = item;
            }

            public void render(GuiGraphics graphics, int x, int y, int width, int itemHeight, int mouseX, int mouseY, float delta) {
                this.x = x;
                this.y = y;

                if (focused)
                    graphics.fill(x, y, x + width, y + itemHeight, 0xff000000);
                graphics.drawString(RadialMenuScreen.this.font, item.name(), x + 2, y + 1, focused ? -1 : 0xffa6a6a6);
            }

            @Override
            public void setFocused(boolean focused) {
                this.focused = focused;
            }

            @Override
            public boolean isFocused() {
                return focused;
            }

            @Nullable
            @Override
            public ComponentPath nextFocusPath(FocusNavigationEvent event) {
                return !focused ? ComponentPath.leaf(this) : null;
            }

            @Override
            public ScreenRectangle getRectangle() {
                return new ScreenRectangle(x, y, width, itemHeight);
            }

            @Override
            public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
                if (controller == RadialMenuScreen.this.controller) {
                    if (ControlifyBindings.GUI_PRESS.on(controller).justPressed()) {
                        editMode.setRadialItem(radialIndex, item);
                        Controlify.instance().config().setDirty();

                        buttons[radialIndex].setAction(item);

                        playClickSound();
                        finishEditing();
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public static class Processor extends ScreenProcessor<RadialMenuScreen> {
        public Processor(RadialMenuScreen screen) {
            super(screen);
        }

        @Override
        public VirtualMouseBehaviour virtualMouseBehaviour() {
            return VirtualMouseBehaviour.DISABLED;
        }
    }
}
