package dev.isxander.controlify.controller.id;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record ControllerType(@Nullable String friendlyName, String mappingId, ResourceLocation namespace, boolean forceJoystick, boolean dontLoad) {
    public static final ControllerType DEFAULT = new ControllerType(null, "default", new ResourceLocation("controlify", "default"), false, false);

    public static final MapCodec<ControllerType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", null).forGetter(ControllerType::friendlyName),
            Codec.STRING.optionalFieldOf("mapping", DEFAULT.mappingId()).forGetter(ControllerType::mappingId),
            ResourceLocation.CODEC.optionalFieldOf("namespace", DEFAULT.namespace()).forGetter(ControllerType::namespace),
            Codec.BOOL.optionalFieldOf("force_joystick", false).forGetter(ControllerType::forceJoystick),
            Codec.BOOL.optionalFieldOf("dont_load", false).forGetter(ControllerType::dontLoad)
    ).apply(instance, ControllerType::new));

    public ResourceLocation getIconSprite() {
        /*? if >=1.20.3 {*/
        return CUtil.addPrefix(namespace, "controllers/");
        /*?} else {*//*
        return Controlify.id("textures/gui/sprites/controllers/" + namespace + ".png");
        *//*?}*/

    }
}
