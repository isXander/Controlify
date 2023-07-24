package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.guide.GuideActionNameSupplier;
import dev.isxander.controlify.bindings.GamepadBinds;
import dev.isxander.controlify.bindings.RadialAction;
import dev.isxander.controlify.bindings.RadialIcons;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.gui.guide.GuideAction;
import dev.isxander.controlify.gui.guide.GuideActionRenderer;
import dev.isxander.controlify.gui.layout.AnchorPoint;
import dev.isxander.controlify.gui.layout.PositionedComponent;
import dev.isxander.controlify.screenop.ScreenControllerEventListener;
import dev.isxander.controlify.sound.ControlifySounds;
import dev.isxander.controlify.utils.Animator;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RadialMenuScreen extends Screen implements ScreenControllerEventListener {
    public static final ResourceLocation EMPTY = new ResourceLocation("controlify", "empty_action");

    private final Controller<?, ?> controller;

    public RadialMenuScreen(Controller<?, ?> controller) {
        super(Component.empty());
        this.controller = controller;
    }

    private final RadialButton[] buttons = new RadialButton[8];
    private int selectedButton = -1;
    private int idleTicks;
    private boolean editMode;
    private boolean isEditing;

    private PositionedComponent<GuideActionRenderer<Object>> editModeGuide;
    private ActionSelectList actionSelectList;

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
        addRenderableWidget(buttons[7] = new RadialButton(7, button.x + 16, button.y - 32 - 8));

        Animator.AnimationInstance animation = new Animator.AnimationInstance(5, Easings::easeOutQuad);
        for (RadialButton radialButton : buttons) {
            animation.addConsumer(radialButton::setX, centerX - 16, radialButton.getX());
            animation.addConsumer(radialButton::setY, centerY - 16, radialButton.getY());
        }
        Animator.INSTANCE.play(animation);

        editModeGuide = addRenderableWidget(new PositionedComponent<>(
                new GuideActionRenderer<>(
                        new GuideAction<>(
                                controller.bindings().GUI_ABSTRACT_ACTION_2,
                                obj -> Optional.of(Component.literal(!editMode ? "Edit Mode" : "Done Editing"))
                        ),
                        false,
                        true
                ),
                AnchorPoint.BOTTOM_CENTER,
                0, -10,
                AnchorPoint.BOTTOM_CENTER
        ));

        editModeGuide.getComponent().updateName(null);
        editModeGuide.updatePosition(width, height);
    }

    @Override
    public void onControllerInput(Controller<?, ?> controller) {
        if (this.controller != controller) return;

        if (!controller.bindings().RADIAL_MENU.held()) {
            if (!isEditing) {
                if (!editMode) {
                    if (selectedButton != -1 && buttons[selectedButton].invoke()) {
                        playClickSound();
                    }

                    onClose();
                } else {
                    RadialButton button = buttons[selectedButton];
                    int x = button.x < width / 2 ? button.x - 110 : button.x + 42;
                    actionSelectList = new ActionSelectList(selectedButton, x, button.y, 100, 80);
                    addRenderableWidget(actionSelectList);
                    setFocused(actionSelectList);
                    isEditing = true;
                }
            }
        }

        if (controller.bindings().GUI_ABSTRACT_ACTION_2.justPressed()) {
            editMode = !editMode;
            editModeGuide.getComponent().updateName(null);
            editModeGuide.updatePosition(width, height);
            playClickSound();

            if (!editMode) {
                finishEditing();
            }
        }

        if (isEditing) {
            if (controller.bindings().GUI_PRESS.justPressed() || controller.bindings().GUI_BACK.justPressed()) {
                finishEditing();
            }
        }

        if (!isEditing && controller.state() instanceof GamepadState state) {
            float x = state.gamepadAxes().leftStickX();
            float y = state.gamepadAxes().leftStickY();
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
                    buttons[i].setFocused(i == selectedButton);
                }

                idleTicks = 0;
            } else {
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

    private void playClickSound() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
    }

    private void finishEditing() {
        isEditing = false;
        onClose();
    }

    @Override
    public void onClose() {
        Controlify.instance().config().saveIfDirty();
        super.onClose();
    }

    public class RadialButton implements Renderable, GuiEventListener, NarratableEntry {
        public static final ResourceLocation TEXTURE = Controlify.id("textures/gui/radial-buttons.png");

        private int x, y;
        private boolean focused;
        private final ControllerBinding binding;
        private final MultiLineLabel name;
        private final RadialIcons.Icon icon;

        private RadialButton(int index, int x, int y) {
            this.x = x;
            this.y = y;

            RadialAction action = controller.config().radialActions[index];
            if (!EMPTY.equals(action.binding())) {
                this.binding = controller.bindings().get(action.binding());
                this.name = MultiLineLabel.create(font, this.binding.name(), 76);
            } else {
                this.binding = null;
                this.name = MultiLineLabel.EMPTY;
            }
            this.icon = RadialIcons.getIcons().get(action.icon());
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(2, 2, 1);
            graphics.blit(TEXTURE, 0, 0, focused ? 16 : 0, 0, 16, 16, 32, 16);
            graphics.pose().popPose();

            graphics.pose().pushPose();
            graphics.pose().translate(x + 4, y + 4, 0);
            graphics.pose().scale(1.5f, 1.5f, 1);
            this.icon.draw(graphics, 0, 0);
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

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
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
        public NarrationPriority narrationPriority() {
            return isFocused() ? NarrationPriority.FOCUSED : NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput builder) {

        }

        @Override
        public ScreenRectangle getRectangle() {
            return new ScreenRectangle(x, y, 32, 32);
        }
    }

    public class ActionSelectList implements Renderable, ContainerEventHandler, NarratableEntry {
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

            controller.bindings().registry().forEach((id, binding) -> {
                children.add(new ActionEntry(id));
            });
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
        public GuiEventListener getFocused() {
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

                controller.config().radialActions[radialIndex] = new RadialAction(focus.binding, controller.config().radialActions[radialIndex].icon());
                Controlify.instance().config().setDirty();
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

        }

        public class ActionEntry implements GuiEventListener {
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
                graphics.drawString(RadialMenuScreen.this.font, name, x + 2, y + 1, -1);
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
        }
    }
}
