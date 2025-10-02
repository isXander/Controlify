package dev.isxander.controlify.mixins.feature.screenop.impl.chat;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.ChatScreenProcessor;
import dev.isxander.controlify.screenop.keyboard.*;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen implements ScreenProcessorProvider, MixinInputTarget, ChatKeyboardDucky {

    @Shadow protected EditBox input;
    @Shadow CommandSuggestions commandSuggestions;

    @Unique private KeyboardWidget keyboard;
    @Unique private float shiftChatAmt = 0f;
    @Unique private final ChatScreenProcessor screenProcessor = new ChatScreenProcessor(
            (ChatScreen) (Object) this,
            () -> this.input,
            () -> this.keyboard,
            () -> (ChatScreenProcessor.CmdSuggestionsController) this.commandSuggestions
    );

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void addKeyboard(CallbackInfo ci) {
        this.shiftChatAmt = 0f;

        ControlifyApi.get().getCurrentController().ifPresent(c -> {
            // if the keyboard is already present, re-add it even if we're in kb/m mode since
            // setting fullscreen will turn it to that mode
            if (!ControlifyApi.get().currentInputMode().isController() && this.keyboard == null) return;
            if (!c.genericConfig().config().showOnScreenKeyboard) return;

            this.shiftChatAmt = 0.5f;
            int keyboardHeight = (int) (this.height * this.shiftChatAmt);
            this.addRenderableWidget(this.keyboard = new KeyboardWidget(0, this.height - keyboardHeight, this.width, keyboardHeight, KeyboardLayouts.full(), this));
        });
    }

    @ModifyArg(method = "setInitialFocus", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;setInitialFocus(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V"))
    private GuiEventListener modifyInitialFocus(GuiEventListener editBox) {
        return this.keyboard != null ? this.keyboard : editBox;
    }

    @Definition(id = "height", field = "Lnet/minecraft/client/gui/screens/ChatScreen;height:I")
    @Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/ChatScreen;width:I")
    // EditBox can't be referenced here because it's an anonymous subclass that doesn't have a concrete Class<?> type
    @Expression("new ?(?, ?, 4, @(this.height - 12), this.width - 4, 12, ?)")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyInputBoxY(int y) {
        return this.height - (int) (this.height * this.shiftChatAmt) - 12;
    }

    @Definition(id = "fill", method = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V")
    @Definition(id = "height", field = "Lnet/minecraft/client/gui/screens/ChatScreen;height:I")
    @Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/ChatScreen;width:I")
    @Expression("?.fill(2, @(this.height - 14), this.width - 2, this.height - 2, ?)")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyInputBoxBackgroundTop(int y) {
        return this.input.getY() - 2;
    }

    @Definition(id = "fill", method = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V")
    @Definition(id = "height", field = "Lnet/minecraft/client/gui/screens/ChatScreen;height:I")
    @Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/ChatScreen;width:I")
    @Expression("?.fill(2, this.height - 14, this.width - 2, @(this.height - 2), ?)")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyInputBoxBackgroundBottom(int y) {
        return this.input.getBottom() - 2;
    }

    @Definition(id = "CommandSuggestions", type = CommandSuggestions.class)
    @Expression("new CommandSuggestions(?, ?, ?, ?, ?, ?, ?, @(10), ?, ?)")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyMaxSuggestionCount(int count) {
        return shiftChatAmt > 0 ? 8 : count;
    }

    @Override
    public float controlify$keyboardShiftAmount() {
        return this.shiftChatAmt;
    }

    @Override
    public boolean controlify$supportsCharInput() {
        return true;
    }

    @Override
    public boolean controlify$acceptChar(char ch, int modifiers) {
        //? if >=1.21.9 {
        this.input.charTyped(new net.minecraft.client.input.CharacterEvent(ch, modifiers));
        //?} else {
        /*this.input.charTyped(ch, modifiers);
        *///?}
        return true;
    }

    @Override
    public boolean controlify$supportsKeyCodeInput() {
        return true;
    }

    @Override
    public boolean controlify$acceptKeyCode(int keycode, int scancode, int modifiers) {
        boolean bypassInput = List.of(
                InputConstants.KEY_RETURN,
                InputConstants.KEY_ESCAPE
        ).contains(keycode);

        //? if >=1.21.9 {
        Predicate<GuiEventListener> keyPress = listener -> listener.keyPressed(new net.minecraft.client.input.KeyEvent(keycode, scancode, modifiers));
        //?} else {
        /*Predicate<GuiEventListener> keyPress = listener -> listener.keyPressed(keycode, scancode, modifiers);
        *///?}

        if (bypassInput) {
            return keyPress.test((ChatScreen) (Object) this);
        }
        return keyPress.test(this.input);
    }

    @Override
    public boolean controlify$supportsCopying() {
        return true;
    }

    @Override
    public boolean controlify$copy() {
        minecraft.keyboardHandler.setClipboard(this.input.getValue());
        return true;
    }

    @Override
    public boolean controlify$supportsCursorMovement() {
        return true;
    }

    @Override
    public boolean controlify$moveCursor(int amount) {
        this.input.moveCursor(amount, false);
        return true;
    }

    @Override
    public ChatScreenProcessor screenProcessor() {
        return this.screenProcessor;
    }

}
