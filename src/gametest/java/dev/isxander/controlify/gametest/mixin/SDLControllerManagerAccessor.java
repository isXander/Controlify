package dev.isxander.controlify.gametest.mixin;

import dev.isxander.controlify.controllermanager.SDLControllerManager;
import dev.isxander.sdl.Sdl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SDLControllerManager.class)
public interface SDLControllerManagerAccessor {
	@Accessor("sdl")
	Sdl controlify_test$getSdl();
}
