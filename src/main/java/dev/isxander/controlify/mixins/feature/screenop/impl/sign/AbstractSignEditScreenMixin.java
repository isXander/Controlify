package dev.isxander.controlify.mixins.feature.screenop.impl.sign;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.AbstractSignEditScreenProcessor;
import dev.isxander.controlify.screenop.keyboard.KeyboardLayouts;
import dev.isxander.controlify.screenop.keyboard.KeyboardWidget;
import dev.isxander.controlify.screenop.keyboard.MixinInputTarget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen implements ScreenProcessorProvider, MixinInputTarget {

    @Shadow private @Nullable TextFieldHelper signField;
    @Shadow @Final private String[] messages;
    @Shadow private int line;
    @Shadow protected abstract void onDone();
    @Shadow @Final protected SignBlockEntity sign;

    @Unique
    protected KeyboardWidget keyboard;
    @Unique
    private final AbstractSignEditScreenProcessor screenProcessor = new AbstractSignEditScreenProcessor(
            (AbstractSignEditScreen) (Object) this,
            direction -> {
                this.line = this.line + direction & 3;
                this.signField.setCursorToEnd();
            },
            () -> this.sign,
            () -> keyboard
    );


    protected AbstractSignEditScreenMixin(Component title) {
        super(title);
    }

    /**
     * Add a screen keyboard if necessary.
     */
    @Inject(method = "init", at = @At("HEAD"))
    private void addKeyboard(CallbackInfo ci) {
        ControlifyApi.get().getCurrentController().ifPresent(c -> {
            // if the keyboard is already present, re-add it even if we're in kb/m mode since
            // setting fullscreen will turn it to that mode
            if (!ControlifyApi.get().currentInputMode().isController() && this.keyboard == null) return;
            if (!c.genericConfig().config().showOnScreenKeyboard) return;

            int keyboardHeight = (int) (this.height * 0.5f);
            this.addRenderableWidget(this.keyboard = new KeyboardWidget(0, this.height - keyboardHeight, this.width, keyboardHeight, KeyboardLayouts.simple(), this));
        });
    }

    /**
     * Only add a "Done" button if we aren't doing an on-screen keyboard.
     */
    @WrapWithCondition(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractSignEditScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"
            )
    )
    private boolean shouldAddDoneButton(AbstractSignEditScreen instance, GuiEventListener guiEventListener) {
        return this.keyboard == null;
    }

    //? if <1.21.6 {
    /*@WrapOperation(method = "offsetSign", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void modifySignOffset(PoseStack instance, float x, float y, float z, Operation<Void> original, @Local(argsOnly = true) BlockState state) {
        if (this.keyboard != null) {
            boolean isStanding = state.getBlock() instanceof StandingSignBlock;
            y = isStanding ? 30f : 20f;
        }
        original.call(instance, x, y, z);
    }
    *///?}

    @Definition(id = "drawCenteredString", method = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")
    @Definition(id = "title", field = "Lnet/minecraft/client/gui/screens/inventory/AbstractSignEditScreen;title:Lnet/minecraft/network/chat/Component;")
    @Expression("?.drawCenteredString(?, this.title, ?, ?, ?)")
    @WrapWithCondition(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean preventDrawingTitle(GuiGraphics instance, Font font, Component text, int x, int y, int color) {
        return this.keyboard == null;
    }

    @Override
    public boolean controlify$supportsCharInput() {
        return true;
    }

    @Override
    public boolean controlify$acceptChar(char ch, int modifiers) {
        if (this.signField == null) return false;
        //? if >=1.21.9 {
        return this.signField.charTyped(new net.minecraft.client.input.CharacterEvent(ch, modifiers));
        //?} else {
        /*return this.signField.charTyped(ch);
        *///?}
    }

    @Override
    public boolean controlify$supportsKeyCodeInput() {
        return true;
    }

    @Override
    public boolean controlify$acceptKeyCode(int keycode, int scancode, int modifiers) {
        if (keycode == InputConstants.KEY_RETURN) {
            this.onDone();
            return true;
        }
        if (this.signField == null) return false;

        //? if >=1.21.9 {
        return this.signField.keyPressed(new net.minecraft.client.input.KeyEvent(keycode, scancode, modifiers));
        //?} else {
        /*return this.signField.keyPressed(keycode);
        *///?}
    }

    @Override
    public boolean controlify$supportsCopying() {
        return true;
    }

    @Override
    public boolean controlify$copy() {
        if (this.signField == null) return false;

        minecraft.keyboardHandler.setClipboard(this.messages[this.line]);
        return true;
    }

    @Override
    public boolean controlify$supportsCursorMovement() {
        return true;
    }

    @Override
    public boolean controlify$moveCursor(int amount) {
        if (this.signField == null) return false;

        this.signField.moveByChars(amount);
        return true;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return screenProcessor;
    }
}
