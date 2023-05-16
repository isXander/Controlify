package dev.isxander.controlify.bindings;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.BindRenderer;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.bind.ControllerBindingBuilder;
import dev.isxander.controlify.config.gui.GamepadBindController;
import dev.isxander.controlify.config.gui.JoystickBindController;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.gui.DrawSize;
import dev.isxander.yacl.api.Option;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class ControllerBindingImpl<T extends ControllerState> implements ControllerBinding {
    private final Controller<T, ?> controller;
    private IBind<T> bind;
    private final IBind<T> defaultBind;
    private BindRenderer renderer;
    private final ResourceLocation id;
    private final Component name, description, category;
    private final Set<BindContext> contexts;
    private final KeyMappingOverride override;

    private static final Map<Controller<?, ?>, Set<IBind<?>>> pressedBinds = new HashMap<>();

    private ControllerBindingImpl(Controller<T, ?> controller, IBind<T> defaultBind, ResourceLocation id, KeyMappingOverride vanillaOverride, Component name, Component description, Component category, Set<BindContext> contexts) {
        this.controller = controller;
        this.bind = this.defaultBind = defaultBind;
        this.renderer = new BindRendererImpl(bind);
        this.id = id;
        this.override = vanillaOverride;
        this.name = name;
        this.description = description;
        this.category = category;
        this.contexts = ImmutableSet.copyOf(contexts);
    }

    @Override
    public float state() {
        return bind.state(controller.state());
    }

    @Override
    public float prevState() {
        return bind.state(controller.prevState());
    }

    @Override
    public boolean held() {
        return bind.held(controller.state());
    }

    @Override
    public boolean prevHeld() {
        return bind.held(controller.prevState());
    }

    @Override
    public boolean justPressed() {
        if (hasBindPressed(this)) return false;

        if (held() && !prevHeld()) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean justReleased() {
        if (hasBindPressed(this)) return false;

        if (!held() && prevHeld()) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    public IBind<T> currentBind() {
        return bind;
    }

    public void setCurrentBind(IBind<T> bind) {
        this.bind = bind;
        this.renderer = new BindRendererImpl(bind);
        Controlify.instance().config().setDirty();
    }

    public IBind<T> defaultBind() {
        return defaultBind;
    }

    @Override
    public void resetBind() {
        setCurrentBind(defaultBind());
    }

    public ResourceLocation id() {
        return id;
    }

    @Override
    public Component name() {
        return name;
    }

    @Override
    public Component description() {
        return description;
    }

    @Override
    public Component category() {
        return category;
    }

    @Override
    public Set<BindContext> contexts() {
        return contexts;
    }

    @Override
    public IBind<T> getBind() {
        return bind;
    }

    @Override
    public boolean isUnbound() {
        return bind instanceof EmptyBind;
    }

    @Override
    public KeyMappingOverride override() {
        return override;
    }

    @Override
    public JsonObject toJson() {
        return currentBind().toJson();
    }

    @Override
    public BindRenderer renderer() {
        return renderer;
    }

    @Override
    public Option.Builder<?> startYACLOption() {
        Option.Builder<IBind<T>> option = Option.createBuilder((Class<IBind<T>>) (Class<?>) IBind.class)
                .name(name())
                .binding(defaultBind(), this::currentBind, this::setCurrentBind)
                .tooltip(this.description());

        if (controller instanceof GamepadController gamepad) {
            ((Option.Builder<IBind<GamepadState>>) (Object) option).controller(opt -> new GamepadBindController(opt, gamepad));
        } else if (controller instanceof JoystickController<?> joystick) {
            ((Option.Builder<IBind<JoystickState>>) (Object) option).controller(opt -> new JoystickBindController(opt, joystick));
        }

        return option;
    }

    // FIXME: very hack solution please remove me

    public static void clearPressedBinds(Controller<?, ?> controller) {
        if (pressedBinds.containsKey(controller)) {
            pressedBinds.get(controller).clear();
        }
    }

    private static boolean hasBindPressed(ControllerBindingImpl<?> binding) {
        var pressed = pressedBinds.getOrDefault(binding.controller, Set.of());
        return pressed.containsAll(getBinds(binding.bind));
    }

    private static void addPressedBind(ControllerBindingImpl<?> binding) {
        pressedBinds.computeIfAbsent(binding.controller, c -> new HashSet<>()).addAll(getBinds(binding.bind));
    }

    private static Set<IBind<?>> getBinds(IBind<?> bind) {
        return Set.of(bind);
    }

    @ApiStatus.Internal
    public static final class ControllerBindingBuilderImpl<T extends ControllerState> implements ControllerBindingBuilder<T> {
        private final Controller<T, ?> controller;
        private IBind<T> bind;
        private ResourceLocation id;
        private Component name = null, description = null, category = null;
        private KeyMappingOverride override = null;
        private final Set<BindContext> contexts = new HashSet<>();

        public ControllerBindingBuilderImpl(Controller<T, ?> controller) {
            this.controller = controller;
        }

        @Override
        public ControllerBindingBuilder<T> identifier(ResourceLocation id) {
            this.id = id;
            return this;
        }

        @Override
        public ControllerBindingBuilder<T> identifier(String namespace, String path) {
            return identifier(new ResourceLocation(namespace, path));
        }

        @Override
        public ControllerBindingBuilder<T> defaultBind(IBind<T> bind) {
            this.bind = bind;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ControllerBindingBuilder<T> defaultBind(GamepadBinds gamepadBind) {
            if (controller instanceof GamepadController gamepad) {
                this.bind = (IBind<T>) gamepadBind.forGamepad(gamepad);
            } else {
                this.bind = new EmptyBind<>();
            }
            return this;
        }

        @Override
        public ControllerBindingBuilder<T> name(Component name) {
            this.name = name;
            return this;
        }

        @Override
        public ControllerBindingBuilder<T> description(Component description) {
            this.description = description;
            return this;
        }

        @Override
        public ControllerBindingBuilder<T> category(Component category) {
            this.category = category;
            return this;
        }

        @Override
        public ControllerBindingBuilder<T> context(BindContext... contexts) {
            this.contexts.addAll(Set.of(contexts));
            return this;
        }

        @Override
        public ControllerBindingBuilder<T> vanillaOverride(KeyMapping keyMapping, BooleanSupplier toggleable) {
            this.override = new KeyMappingOverride(keyMapping, toggleable);
            return this;
        }

        @Override
        public ControllerBindingBuilder<T> vanillaOverride(KeyMapping keyMapping) {
            return vanillaOverride(keyMapping, () -> false);
        }

        @Override
        public ControllerBinding build() {
            Validate.notNull(id, "Identifier must be set");
            Validate.notNull(bind, "Default bind must be set");
            Validate.notNull(category, "Category must be set");

            if (name == null)
                name = Component.translatable("controlify.binding." + id.getNamespace() + "." + id.getPath());
            if (description == null) {
                var descKey = "controlify.binding." + id.getNamespace() + "." + id.getPath() + ".desc";
                if (Language.getInstance().has(descKey)) {
                    description = Component.translatable(descKey);
                } else {
                    description = Component.empty();
                }
            }

            return new ControllerBindingImpl<>(controller, bind, id, override, name, description, category, contexts);
        }
    }

    private record BindRendererImpl(IBind<?> bind) implements BindRenderer {
        @Override
        public void render(GuiGraphics graphics, int x, int centerY) {
            bind.draw(graphics, x, centerY);
        }

        @Override
        public DrawSize size() {
            return bind.drawSize();
        }
    }
}
