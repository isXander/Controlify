package dev.isxander.controlify.test;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindingsApi;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.BindingSupplier;
import dev.isxander.controlify.bindings.GamepadBinds;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.isxander.controlify.test.Assertions.*;
import static dev.isxander.controlify.test.ClientTestHelper.*;

public class ControlifyTests {
    BindingSupplier binding = null;

    @Test.PreLoad("Binding registry test")
    void bindingRegistryTest() {
        var registry = ControlifyBindingsApi.get();
        assertNotNull(registry, "Binding registry is null");
        binding = registry.registerBind(GamepadBinds.A_BUTTON, new ResourceLocation("controlify", "test_bind"));
        assertNotNull(binding, "Bind registry failed - BindingSupplier is null");
    }

    @Test.PostLoad("BindingSupplier getter test")
    void bindingSupplierGetterTest() {
        var controller = createFakeController();
        assertNotNull(binding.get(controller), "Bind registry failed - Bind for fake controller is null");
    }

    @Test.PostLoad("Input mode changed event test")
    void checkInputModeChangedEvent() {
        var api = ControlifyApi.get();

        AtomicBoolean called = new AtomicBoolean(false);

        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> called.set(true));
        api.setInputMode(InputMode.CONTROLLER);
        api.setInputMode(InputMode.KEYBOARD_MOUSE);

        assertTrue(called.get(), "Input mode changed event was not called");
    }
}
