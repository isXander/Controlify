package dev.isxander.controlify.api.bind;

import com.google.gson.JsonObject;
import dev.isxander.controlify.bindings.bind.BindType;
import dev.isxander.controlify.bindings.bind.BindValue;
import dev.isxander.yacl.api.Option;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public interface ControllerBinding {
    BindValue value();
    BindValue prevValue();

    boolean justPressed();
    boolean justReleased();

    BindType preferredType();

    @Deprecated float state();
    @Deprecated float prevState();

    @Deprecated boolean held();
    @Deprecated boolean prevHeld();

    Component name();
    Component description();
    Component category();

    ResourceLocation id();

    /**
     * The vanilla override of the binding. Null if there is no override.
     */
    @Nullable KeyMappingOverride override();

    void resetBind();
    boolean isUnbound();

    BindRenderer renderer();

    Option<?> generateYACLOption();

    JsonObject toJson();

    record KeyMappingOverride(KeyMapping keyMapping, BooleanSupplier toggleable) {
    }
}
