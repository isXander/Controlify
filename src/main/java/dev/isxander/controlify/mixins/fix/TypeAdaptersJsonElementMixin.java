package dev.isxander.controlify.mixins.fix;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

/**
 * If this breaks everything I am so so so so sooooooo
 * sorry...
 */
@Mixin(targets = "com.google.gson.internal.bind.TypeAdapters$28")
public class TypeAdaptersJsonElementMixin {
    @Inject(method = "readTerminal", at = @At("HEAD"), cancellable = true)
    private void dontLazilyParseStrings(JsonReader in, JsonToken peeked, CallbackInfoReturnable<JsonElement> cir) throws IOException {
        if (peeked == JsonToken.NUMBER) {
            try {
                cir.setReturnValue(new JsonPrimitive(in.nextInt()));
            } catch (NumberFormatException nfe) {
                // let the regular lazy number parsing work
            }
        }
    }
}
