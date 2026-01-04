package dev.isxander.controlify.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated on {@link Object} to indicate that the object is a
 * <code>Component</code>/<code>Text</code> and can be safely cast.
 * If annotated on a parameter, it indicates that the method expects the parameter
 * to be a Minecraft Component/Text, and will result in a ClassCastException if it is not.
 * <p>
 * This is required because this API is designed without a direct dependency to Minecraft.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE_USE,
        ElementType.PARAMETER
})
public @interface MinecraftComponent {
}
