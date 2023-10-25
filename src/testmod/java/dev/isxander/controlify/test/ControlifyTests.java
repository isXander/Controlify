package dev.isxander.controlify.test;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.BindingSupplier;
import dev.isxander.controlify.api.bind.ControlifyBindingsApi;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.EmptyBind;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.isxander.controlify.test.Assertions.*;
import static dev.isxander.controlify.test.ClientTestHelper.*;

public class ControlifyTests {
    BindingSupplier binding = null;
    boolean titleScreenProcessorPresent = false;

    @Test.Entrypoint("Binding registry test")
    void bindingRegistryTest() {
        var registry = ControlifyBindingsApi.get();
        assertNotNull(registry, "Binding registry is null");
        binding = registry.registerBind(
                new ResourceLocation("controlify", "test_bind"),
                builder -> builder
                        .defaultBind(new EmptyBind<>())
                        .category(Component.literal("Test"))
        );
        assertNotNull(binding, "Bind registry failed - BindingSupplier is null");
    }

    @Test.TitleScreen("BindingSupplier getter test")
    void bindingSupplierGetterTest() {
        var controller = createAndUseDummyController();
        assertNotNull(binding.onController(controller), "Bind registry failed - Bind for fake controller is null");
        controller.finish();
    }

    @Test.TitleScreen("Input mode changed event test")
    void checkInputModeChangedEvent() {
        var api = ControlifyApi.get();

        AtomicBoolean called = new AtomicBoolean(false);

        ControlifyEvents.INPUT_MODE_CHANGED.register(mode -> called.set(true));
        api.setInputMode(InputMode.CONTROLLER);
        api.setInputMode(InputMode.KEYBOARD_MOUSE);

        assertTrue(called.get(), "Input mode changed event was not called");
    }

    @Test.Entrypoint("Screen component registry setup test")
    void setupScreenComponentRegistry() {
        ScreenProcessorProvider.registerProvider(TitleScreen.class, ts -> new ScreenProcessor<>(ts){
            @Override
            public void onWidgetRebuild() {
                super.onWidgetRebuild();
                titleScreenProcessorPresent = true;
            }
        });
    }

    @Test.TitleScreen("Screen component registry test")
    void checkScreenComponentRegistry() {
        assertTrue(titleScreenProcessorPresent, "Screen processor was not called");
    }
}
