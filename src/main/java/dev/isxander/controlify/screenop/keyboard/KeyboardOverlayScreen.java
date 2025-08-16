package dev.isxander.controlify.screenop.keyboard;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.Controlify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;

/**
 * A screen with a single widget: the keyboard widget.
 * Renders this screen on top of the previous screen, and
 * quietly closes and returns to the previous screen without
 * reinitialisation when the keyboard is closed.
 * <p>
 * This is done to be minimally invasive to the underlying screen,
 * instead of adding and removing widgets, where focus to the keyboard could
 * be lost without the keyboard closing.
 *
 * @see dev.isxander.controlify.mixins.feature.screenop.MinecraftMixin#preventRemovingOldScreen(Screen, Screen)
 * this mixin prevents calling removed() on the underlying screen when this overlay is presented, since it will be restored
 */
public class KeyboardOverlayScreen extends Screen {
    private final Screen backgroundScreen;
    private final KeyboardLayoutWithId initialKeyboardLayout;
    private final InputTarget inputTarget;
    private final KeyboardPositioner keyboardPositioner;

    private KeyboardWidget keyboardWidget;

    public KeyboardOverlayScreen(
            Screen backgroundScreen,
            KeyboardLayoutWithId initialKeyboardLayout,
            InputTarget inputTarget,
            KeyboardPositioner keyboardPositioner
    ) {
        super(backgroundScreen.getTitle());
        this.backgroundScreen = backgroundScreen;
        this.initialKeyboardLayout = initialKeyboardLayout;
        this.inputTarget = new WrappedInputTarget(inputTarget);
        this.keyboardPositioner = keyboardPositioner;
    }

    @Override
    protected void init() {
        KeyboardLayoutWithId layout = this.keyboardWidget != null
                ? Controlify.instance().keyboardLayoutManager().getLayout(this.keyboardWidget.getCurrentLayoutId())
                : this.initialKeyboardLayout;

        ScreenRectangle keyboardRect = this.keyboardPositioner.positionKeyboard(this.width, this.height);

        this.keyboardWidget = this.addRenderableWidget(
                new KeyboardWidget(
                        keyboardRect.left(),
                        keyboardRect.top(),
                        keyboardRect.width(),
                        keyboardRect.height(),
                        layout,
                        this.inputTarget
                )
        );
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        this.backgroundScreen.resize(minecraft, width, height);
        super.resize(minecraft, width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.backgroundScreen.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.backgroundScreen.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        } else {
            return this.backgroundScreen.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button /*? if >=1.21.9 {*/ ,boolean doubleClick /*?}*/) {
        if (super.mouseClicked(x, y, button /*? if >=1.21.9 {*/,doubleClick/*?}*/)) {
            return true;
        } else {
            if (this.backgroundScreen.mouseClicked(x, y, button /*? if >=1.21.9 {*/,doubleClick/*?}*/)) {
                onClose();
                return true;
            }
        }

        return false;
    }

    @Override
    public void tick() {
        this.backgroundScreen.tick();
        super.tick();
    }

    @Override
    public void onClose() {
        // restore the previous screen without calling minecraft.setScreen
        // so the screen is not reinitialised
        this.minecraft.screen = this.backgroundScreen;

        Controlify.instance().virtualMouseHandler().onScreenChanged();
    }

    /**
     * An input target that captures {@link InputConstants#KEY_RETURN} and {@link InputConstants#KEY_ESCAPE}
     * key presses to close the keyboard overlay screen.
     */
    private class WrappedInputTarget extends InputTarget.Delegated {
        public WrappedInputTarget(InputTarget target) {
            super(target);
        }

        @Override
        public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
            if (keycode == InputConstants.KEY_RETURN || keycode == InputConstants.KEY_ESCAPE) {
                // Close the keyboard overlay when pressing Enter or Escape
                KeyboardOverlayScreen.this.onClose();
                return true;
            }

            return super.acceptKeyCode(keycode, scancode, modifiers);
        }
    }

    @FunctionalInterface
    public interface KeyboardPositioner {
        ScreenRectangle positionKeyboard(int screenWidth, int screenHeight);
    }

    public static KeyboardPositioner aboveOrBelowWidgetPositioner(int desiredKeyboardWidth, int desiredKeyboardHeight, int padding, Supplier<ScreenRectangle> widgetRectSupplier) {
        return (screenWidth, screenHeight) -> {
            ScreenRectangle widgetRect = widgetRectSupplier.get();

            int keyboardWidth = Math.min(desiredKeyboardWidth, screenWidth);
            int keyboardLeft = Math.clamp(
                    widgetRect.getCenterInAxis(ScreenAxis.HORIZONTAL) - keyboardWidth / 2,
                    0, screenWidth - keyboardWidth
            );

            int spaceBelow = screenHeight - widgetRect.bottom() - padding;
            int spaceAbove = widgetRect.top() - padding;

            // Determine the best vertical position for the keyboard.
            // 1. Prefer placing the keyboard below the widget, if it fits.
            // 2. If there's not enough space below, check space above.
            // 3. If neither fits fully, pick the side with more space and shrink the height.
            int keyboardHeight;
            boolean above;
            if (spaceBelow >= desiredKeyboardHeight) {
                keyboardHeight = desiredKeyboardHeight;
                above = false;
            } else if (spaceAbove >= desiredKeyboardHeight) {
                keyboardHeight = desiredKeyboardHeight;
                above = true;
            } else if (spaceBelow >= spaceAbove) {
                keyboardHeight = spaceBelow;
                above = false;
            } else {
                keyboardHeight = spaceAbove;
                above = true;
            }
            int keyboardTop = above
                    ? widgetRect.top() - keyboardHeight - padding
                    : widgetRect.bottom() + padding;

            return new ScreenRectangle(
                    keyboardLeft,
                    keyboardTop,
                    keyboardWidth,
                    keyboardHeight
            );
        };
    }
}
