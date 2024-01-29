package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.BindRenderer;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.RadialIcons;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.gui.guide.GuideAction;
import dev.isxander.controlify.gui.guide.GuideActionRenderer;
import dev.isxander.controlify.gui.layout.AnchorPoint;
import dev.isxander.controlify.gui.layout.PositionedComponent;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.sound.ControlifySounds;
import dev.isxander.controlify.utils.Animator;
import dev.isxander.controlify.utils.Easings;
import dev.isxander.controlify.utils.CUtil;
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
import java.util.Map;
import java.util.Optional;

public class RadialMenuScreen extends Screen implements ScreenControllerEventListener, ScreenProcessorProvider {
    public static final ResourceLocation EMPTY_ACTION = new ResourceLocation("controlify", "empty_action");

    private final Controller<?, ?> controller;
    private final boolean editMode;
    private final Screen parent;

    private final RadialButton[] buttons = new RadialButton[8];
    private int selectedButton = -1;
    private int idleTicks;
    private boolean isEditing;

    private ActionSelectList actionSelectList;

    private final Processor processor = new Processor(this);

    public RadialMenuScreen(Controller<?, ?> controller, boolean editMode, Screen parent) {
        super(Component.empty());
        this.controller = controller;
        this.editMode = editMode;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        RadialButton button;
        addRenderableWidget(buttons[0] = button = new RadialButton(0, centerX - 16, centerY - 64 - 8));
        addRenderableWidget(buttons[1] = button = new RadialButton(1, button.x + 32 + 8, button.y + 16));
        addRenderableWidget(buttons[2] = button = new RadialButton(2, button.x + 16, button.y + 32 + 8));
        addRenderableWidget(buttons[3] = button = new RadialButton(3, button.x - 16, button.y + 32 + 8));
        addRenderableWidget(buttons[4] = button = new RadialButton(4, button.x - 32 - 8, button.y + 16));
        addRenderableWidget(buttons[5] = button = new RadialButton(5, button.x - 32 - 8, button.y - 16));
        addRenderableWidget(buttons[6] = button = new RadialButton(6, button.x - 16, button.y - 32 - 8));
        addRenderableWidget(buttons[7]          = new RadialButton(7, button.x + 16, button.y - 32 - 8));

        Animator.AnimationInstance animation = new Animator.AnimationInstance(5, Easings::easeOutQuad);
        for (RadialButton radialButton : buttons) {
            animation.addConsumer(radialButton::setX, centerX - 16, (float) radialButton.getX());
            animation.addConsumer(radialButton::setY, centerY - 16, (float) radialButton.getY());
        }
        Animator.INSTANCE.play(animation);

        if (editMode) {
            var exitGuide = addRenderableWidget(new PositionedComponent<>(
                    new GuideActionRenderer<>(
                            new GuideAction<>(
                                    controller.bindings().GUI_BACK,
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
    public void onControllerInput(Controller<?, ?> controller) {
        if (this.controller != controller) return;

        if (!editMode && !controller.bindings().RADIAL_MENU.held()) {
            if (selectedButton != -1 && buttons[selectedButton].invoke()) {
                playClickSound();
            }

            onClose();
        }

        if (editMode && controller.bindings().GUI_BACK.justPressed()) {
            playClickSound();
            onClose();
        }

        if (!isEditing) {
            float x = controller.bindings().RADIAL_AXIS_RIGHT.state() - controller.bindings().RADIAL_AXIS_LEFT.state();
            float y = controller.bindings().RADIAL_AXIS_DOWN.state() - controller.bindings().RADIAL_AXIS_UP.state();
            float threshold = controller.config().buttonActivationThreshold;

            if (Math.abs(x) >= threshold || Math.abs(y) >= threshold) {
                float angle = Mth.wrapDegrees(Mth.RAD_TO_DEG * (float) Mth.atan2(y, x) - 90f) + 180f;
                float each = 360f / buttons.length;

                int newSelected = Mth.floor((angle + each / 2f) / each) % buttons.length;
                if (newSelected != selectedButton) {
                    selectedButton = newSelected;
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ControlifySounds.SCREEN_FOCUS_CHANGE, 1f));
                }

                for (int i = 0; i < buttons.length; i++) {
                    boolean selected = i == selectedButton;
                    buttons[i].setFocused(selected);
                    if (selected) {
                        this.setFocused(buttons[i]);
                    }
                }

                idleTicks = 0;
            } else if (!editMode) {
                idleTicks++;
                if (idleTicks >= 20) {
                    selectedButton = -1;
                    for (RadialButton button : buttons) {
                        button.setFocused(false);
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (editMode)
            renderDirtBackground(graphics);

        super.render(graphics, mouseX, mouseY, delta);

        if (!editMode) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("controlify.radial_menu.configure_hint"),
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
        return editMode;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return this.processor;
    }

    public class RadialButton implements Renderable, GuiEventListener, NarratableEntry, ComponentProcessor {
        public static final ResourceLocation TEXTURE = Controlify.id("textures/gui/radial-buttons.png");

        private int x, y;
        private float translateX, translateY;
        private boolean focused;
        private ControllerBinding binding;
        private MultiLineLabel name;
        private RadialIcon icon;

        private RadialButton(int index, float x, float y) {
            this.setX(x);
            this.setY(y);

            ResourceLocation binding = controller.config().radialActions[index];
            if (controller.bindings().get(binding) == null) {
                CUtil.LOGGER.warn("Binding {} does not exist!", binding);
                controller.config().radialActions[index] = EMPTY_ACTION;
                Controlify.instance().config().setDirty();
            }
            this.setAction(binding);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            graphics.pose().pushPose();
            graphics.pose().translate(x + translateX, y + translateY, 0);

            graphics.pose().pushPose();
            graphics.pose().scale(2, 2, 1);
            graphics.blit(TEXTURE, 0, 0, focused ? 16 : 0, 0, 16, 16, 32, 16);
            graphics.pose().popPose();

            if (!editMode || !focused) {
                graphics.pose().pushPose();
                graphics.pose().translate(4, 4, 0);
                graphics.pose().scale(1.5f, 1.5f, 1);
                this.icon.draw(graphics, 0, 0, delta);
                graphics.pose().popPose();
            } else {
                BindRenderer renderer = controller.bindings().GUI_PRESS.renderer();
                renderer.render(graphics, 16 - renderer.size().width() / 2, 16);
            }

            graphics.pose().popPose();

            if (focused)
                name.renderCentered(graphics, width / 2, height / 2 - font.lineHeight / 2 - ((name.getLineCount() - 1) * font.lineHeight / 2));
        }

        public boolean invoke() {
            if (binding != null) {
                binding.fakePress();
                return true;
            }
            return false;
        }

        public void setAction(ResourceLocation binding) {
            if (!EMPTY_ACTION.equals(binding) && controller.bindings().get(binding) != null) {
                this.binding = controller.bindings().get(binding);
                this.icon = RadialIcons.getIcons().get(this.binding.radialIcon().orElseThrow());
                this.name = MultiLineLabel.create(font, this.binding.name(), 76);
            } else {
                this.binding = null;
                this.name = MultiLineLabel.EMPTY;
                this.icon = RadialIcons.getIcons().get(RadialIcons.EMPTY);
            }
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
        public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?, ?> controller) {
            if (editMode && controller == RadialMenuScreen.this.controller && controller.bindings().GUI_PRESS.justPressed()) {
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
            if (binding != null)
                builder.add(NarratedElementType.TITLE, binding.name());
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

            controller.bindings().registry().entrySet().stream()
                    .filter(entry -> entry.getValue().radialIcon().isPresent())
                    .map(Map.Entry::getKey)
                    .forEach(id -> children.add(new ActionEntry(id)));

            var selectedBind = controller.config().radialActions[radialIndex];
            children.stream()
                    .filter(action -> action.binding.equals(selectedBind))
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
        public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?, ?> controller) {
            if (controller == RadialMenuScreen.this.controller) {
                if (controller.bindings().GUI_BACK.justPressed()) {
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
                builder.add(NarratedElementType.TITLE, getFocused().name);
            }
        }

        public class ActionEntry implements GuiEventListener, ComponentProcessor {
            private int x, y;
            private boolean focused;
            private final ResourceLocation binding;
            private final Component name;

            public ActionEntry(ResourceLocation binding) {
                this.binding = binding;
                this.name = controller.bindings().get(binding).name();
            }

            public void render(GuiGraphics graphics, int x, int y, int width, int itemHeight, int mouseX, int mouseY, float delta) {
                this.x = x;
                this.y = y;

                if (focused)
                    graphics.fill(x, y, x + width, y + itemHeight, 0xff000000);
                graphics.drawString(RadialMenuScreen.this.font, name, x + 2, y + 1, focused ? -1 : 0xffa6a6a6);
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
            public boolean overrideControllerButtons(ScreenProcessor<?> screen, Controller<?, ?> controller) {
                if (controller == RadialMenuScreen.this.controller) {
                    if (controller.bindings().GUI_PRESS.justPressed()) {
                        controller.config().radialActions[radialIndex] = binding;
                        Controlify.instance().config().setDirty();

                        buttons[radialIndex].setAction(binding);

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
