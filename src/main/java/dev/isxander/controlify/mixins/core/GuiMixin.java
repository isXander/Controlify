package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.utils.InitialScreenRegistryDuck;
import dev.isxander.controlify.utils.MinecraftUtil;
import dev.isxander.controlify.utils.MouseMinecraftCallNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(
        //? if >=26.2 {
        net.minecraft.client.gui.Gui.class
        //?} else {
        /*Minecraft.class
        *///?}
)
public class GuiMixin implements InitialScreenRegistryDuck {
    @Unique
    private final List<Function<Runnable, Screen>> initialScreenCallbacks = new ArrayList<>();
    @Unique private boolean initialScreensHappened = false;

    // Ideally, this would be done in MouseHandler#releaseMouse, but moving
    // the mouse before the screen init is bad, because some mods (e.g. PuzzleLib)
    // have custom mouse events that call into screens, events that have not been
    // initialised yet in Screen#init. Causing NPEs and many strange issues.
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;releaseMouse()V"))
    private void notifyInjectionToNotRun(Screen screen, CallbackInfo ci) {
        ((MouseMinecraftCallNotifier) Minecraft.getInstance().mouseHandler).controlify$imFromMinecraftSetScreen();
    }

    /**
     * Without this, the mouse would be left in the middle of the
     * screen, hovering over whatever is there which would look wrong
     * as there is a focus as well.
     */
    @Inject(
            method = "setScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;init(II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void hideMouseAfterRelease(Screen screen, CallbackInfo ci) {
        if (ControlifyApi.get().currentInputMode().isController()) {
            Controlify.instance().hideMouse(true, true);
        }
    }

    @WrapMethod(method = "addInitialScreens")
    private boolean injectCustomInitialScreens(List<Function<Runnable, Screen>> screens, Operation<Boolean> original) {
        boolean result = original.call(screens);
        screens.addAll(initialScreenCallbacks);
        initialScreensHappened = true;
        return result;
    }

    @Override
    public void controlify$registerInitialScreen(Function<Runnable, Screen> screenFactory) {
        if (initialScreensHappened) {
            Screen lastScreen = MinecraftUtil.getScreen();
            MinecraftUtil.setScreen(screenFactory.apply(() -> MinecraftUtil.setScreen(lastScreen)));
        } else {
            initialScreenCallbacks.add(screenFactory);
        }
    }
}
