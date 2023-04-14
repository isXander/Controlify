package dev.isxander.controlify.api.bind;

import com.google.gson.JsonObject;
import dev.isxander.yacl.api.Option;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

public interface ControllerBinding {
    float state();
    float prevState();

    boolean held();
    boolean prevHeld();

    boolean justPressed();
    boolean justReleased();

    Component name();
    Component description();
    Component category();

    ResourceLocation id();

    KeyMappingOverride override();

    void resetBind();
    boolean isUnbound();

    BindRenderer renderer();

    Option<?> generateYACLOption();

    JsonObject toJson();

    record KeyMappingOverride(KeyMapping keyMapping, BooleanSupplier toggleable) {
    }
}
