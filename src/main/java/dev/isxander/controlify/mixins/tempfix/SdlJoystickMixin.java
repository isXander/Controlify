package dev.isxander.controlify.mixins.tempfix;

import io.github.libsdl4j.api.joystick.SdlJoystick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SdlJoystick.class)
public class SdlJoystickMixin {
    @ModifyConstant(method = "SDL_GetJoystickGUIDString", constant = @Constant(intValue = 33))
    private static int modifyGuidStringLength1(int original) {
        return 256;
    }

    @ModifyConstant(method = "SDL_GetJoystickGUIDString", constant = @Constant(longValue = 33))
    private static long modifyGuidStringLength2(long original) {
        return 256;
    }
}
