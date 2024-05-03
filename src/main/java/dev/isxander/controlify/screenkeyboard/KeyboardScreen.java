package dev.isxander.controlify.screenkeyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * An overlay screen that renders the keyboard on top of an existing screen.
 * This is a trick to the user which looks like the
 */
public class KeyboardScreen extends Screen {
    private final Screen screen;

    public KeyboardScreen(Screen screen) {
        super(screen.getTitle());
        this.screen = screen;
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        this.screen.init(minecraft, width, height);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new ChatKeyboardWidget(0, this.height / 3 * 2, this.width, this.height / 3, KeyPressConsumer.of(this::keyPressed, this::charTyped)));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.screen.render(guiGraphics, -1, -1, partialTick);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 500);

        super.render(guiGraphics, mouseX, mouseY, partialTick); // render widgets on top of other screen

        guiGraphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics/*? if >1.20.1{*/, int mouseX, int mouseY, float partialTick/*?}*/) {
        // no background
    }

    @Override
    public void onClose() {
        minecraft.screen = screen; // don't re-init
    }
}
