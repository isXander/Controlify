package dev.isxander.controlify.bindings;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.BindRenderer;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.bind.ControllerBindingBuilder;
import dev.isxander.controlify.bindings.bind.BindModifier;
import dev.isxander.controlify.bindings.bind.BindModifiers;
import dev.isxander.controlify.bindings.bind.BindType;
import dev.isxander.controlify.bindings.bind.BindValue;
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
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.BooleanSupplier;

public class ControllerBindingImpl<T extends ControllerState> implements ControllerBinding {
    private final Controller<T, ?> controller;
    private IBind<T> bind;
    private final IBind<T> defaultBind;
    private BindRenderer renderer;
    private final ResourceLocation id;
    private final Component name, description, category;
    private final KeyMappingOverride override;
    private final BindType preferredType;
    private final ImmutableList<BindModifier> modifiers;

    private static final Map<Controller<?, ?>, Set<IBind<?>>> pressedBinds = new HashMap<>();

    private ControllerBindingImpl(Controller<T, ?> controller, IBind<T> defaultBind, ResourceLocation id, BindType preferredType, Collection<BindModifier> modifiers, KeyMappingOverride vanillaOverride, Component name, Component description, Component category) {
        this.controller = controller;
        this.bind = this.defaultBind = defaultBind;
        this.renderer = new BindRendererImpl(bind);
        this.id = id;
        this.override = vanillaOverride;
        this.name = name;
        this.description = description;
        this.category = category;
        this.preferredType = preferredType;
        this.modifiers = ImmutableList.copyOf(modifiers);
    }

    @Override
    public BindValue value() {
        return bind.value(controller.state()).modify(modifiers);
    }

    @Override
    public BindValue prevValue() {
        return bind.value(controller.prevState()).modify(modifiers);
    }

    @Deprecated
    @Override
    public float state() {
        return value().analogue();
    }

    @Deprecated
    @Override
    public float prevState() {
        return prevValue().analogue();
    }

    @Deprecated
    @Override
    public boolean held() {
        return value().digital();
    }

    @Deprecated
    @Override
    public boolean prevHeld() {
        return prevValue().digital();
    }

    @Override
    public BindType preferredType() {
        return preferredType;
    }

    @Override
    public boolean justPressed() {
        if (hasBindPressed(this)) return false;

        if (value().digital() && !prevValue().digital()) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean justReleased() {
        if (hasBindPressed(this)) return false;

        if (!value().digital() && prevValue().digital()) {
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
    public Option<?> generateYACLOption() {
        Option.Builder<IBind<T>> option = Option.createBuilder((Class<IBind<T>>) (Class<?>) IBind.class)
                .name(name())
                .binding(defaultBind(), this::currentBind, this::setCurrentBind)
                .tooltip(this.description());

        if (controller instanceof GamepadController gamepad) {
            ((Option.Builder<IBind<GamepadState>>) (Object) option).controller(opt -> new GamepadBindController(opt, gamepad, this.preferredType()));
        } else if (controller instanceof JoystickController<?> joystick) {
            ((Option.Builder<IBind<JoystickState>>) (Object) option).controller(opt -> new JoystickBindController(opt, joystick));
        }

        return option.build();
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
        private BindType type = null;
        private final List<BindModifier> modifiers = new ArrayList<>();

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
        public ControllerBindingBuilder<T> preferredBind(BindType type) {
            this.type = type;
            return this;
        }

        @Override
        public ControllerBindingBuilder<T> modifyBind(BindModifier... modifiers) {
            this.modifiers.addAll(List.of(modifiers));
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

            if (type == null) {
                type = BindType.ANALOGUE;
                Controlify.LOGGER.warn("Preferred bind type not set for binding {}, defaulting to analogue", id);
            }

            return new ControllerBindingImpl<>(controller, bind, id, type, override, name, description, category);
        }
    }

    private record BindRendererImpl(IBind<?> bind) implements BindRenderer {
        @Override
        public void render(PoseStack poseStack, int x, int centerY) {
            bind.draw(poseStack, x, centerY);
        }

        @Override
        public DrawSize size() {
            return bind.drawSize();
        }
    }
}
