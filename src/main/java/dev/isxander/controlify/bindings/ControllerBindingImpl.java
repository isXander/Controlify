package dev.isxander.controlify.bindings;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.bind.ControllerBindingBuilder;
import dev.isxander.controlify.bindings.v2.input.EmptyInput;
import dev.isxander.controlify.bindings.v2.input.Input;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.gui.controllers.BindController;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.KeyMapping;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class ControllerBindingImpl implements ControllerBinding {
    private final ControllerEntity controller;
    private Input input;
    private final Input defaultInput;
    private final Function<ControllerStateView, Float> hardcodedBind;
    private final ResourceLocation id;
    private final Component name, description, category;
    private final Set<BindContext> contexts;
    private final ResourceLocation radialIcon;
    private final KeyMappingOverride override;

    private static final Map<ControllerEntity, Set<Input>> pressedBinds = new Object2ObjectOpenHashMap<>();

    private byte fakePressState = 0;

    private ControllerBindingImpl(ControllerEntity controller, Input defaultInput, Function<ControllerStateView, Float> hardcodedBind, ResourceLocation id, KeyMappingOverride vanillaOverride, Component name, Component description, Component category, Set<BindContext> contexts, ResourceLocation icon) {
        this.controller = controller;
        this.input = this.defaultInput = defaultInput;
        this.hardcodedBind = hardcodedBind;
        this.id = id;
        this.override = vanillaOverride;
        this.name = name;
        this.description = description;
        this.category = category;
        this.contexts = ImmutableSet.copyOf(contexts);
        this.radialIcon = icon;
    }

    @Override
    public float state() {
        if (fakePressState == 1)
            return 1f;
        return Math.max(input.state(input().stateNow()), hardcodedBind.apply(input().stateNow()));
    }

    @Override
    public float prevState() {
        if (fakePressState == 2)
            return 1f;
        return Math.max(input.state(input().stateThen()), hardcodedBind.apply(input().stateThen()));
    }

    @Override
    public boolean held() {
        return fakePressState == 2 || analogue2Digital(input.state(input().stateNow()));
    }

    @Override
    public boolean prevHeld() {
        return fakePressState == 3 || analogue2Digital(input.state(input().stateThen()));
    }

    private boolean analogue2Digital(float analogue) {
        return analogue > input().config().config().buttonActivationThreshold;
    }

    @Override
    public boolean justPressed() {
        if (hasBindPressed(this)) return false;

        if ((held() && !prevHeld()) || fakePressState == 2) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean justReleased() {
        if (hasBindPressed(this)) return false;

        if ((!held() && prevHeld()) || fakePressState == 3) {
            addPressedBind(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void fakePress() {
        this.fakePressState = 1;
    }

    @Override
    public void tick() {
        if (fakePressState > 0)
            fakePressState++;
        if (fakePressState >= 4)
            fakePressState = 0;
    }

    @Override
    public Optional<ResourceLocation> radialIcon() {
        return Optional.ofNullable(this.radialIcon);
    }

    public void setCurrentBind(Input input) {
        this.input = input;
        Controlify.instance().config().setDirty();
    }

    @Override
    public Input defaultBind() {
        return defaultInput;
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
    public Input getBind() {
        return input;
    }

    @Override
    public boolean isUnbound() {
        return input instanceof EmptyInput;
    }

    @Override
    public KeyMappingOverride override() {
        return override;
    }

    @Override
    public JsonElement toJson() {
        return Input.CODEC.encodeStart(JsonOps.INSTANCE, getBind()).getOrThrow();
    }

    @Override
    public Option.Builder<Input> startYACLOption() {
        return Option.<Input>createBuilder()
                .name(name())
                .binding(new EmptyInput(), this::getBind, this::setCurrentBind)
                .description(OptionDescription.of(this.description()))
                .customController(opt -> new BindController(opt, controller));
    }

    private InputComponent input() {
        return this.controller.input().orElseThrow();
    }

    // FIXME: very hack solution please remove me

    public static void clearPressedBinds(ControllerEntity controller) {
        if (pressedBinds.containsKey(controller)) {
            pressedBinds.get(controller).clear();
        }
    }

    private static boolean hasBindPressed(ControllerBindingImpl binding) {
        var pressed = pressedBinds.getOrDefault(binding.controller, Set.of());
        return pressed.containsAll(getBinds(binding.input));
    }

    private static void addPressedBind(ControllerBindingImpl binding) {
        pressedBinds.computeIfAbsent(binding.controller, c -> new ObjectOpenHashSet<>()).addAll(getBinds(binding.input));
    }

    private static Set<Input> getBinds(Input input) {
        return Set.of(input);
    }

    @ApiStatus.Internal
    public static final class ControllerBindingBuilderImpl implements ControllerBindingBuilder {
        private final ControllerEntity controller;
        private Input input;
        private Function<ControllerStateView, Float> hardcodedBind = state -> 0f;
        private ResourceLocation id;
        private Component name = null, description = null, category = null;
        private KeyMappingOverride override = null;
        private final Set<BindContext> contexts = new HashSet<>();
        private ResourceLocation radialIcon = null;

        public ControllerBindingBuilderImpl(ControllerEntity controller) {
            this.controller = controller;
        }

        @Override
        public ControllerBindingBuilder identifier(ResourceLocation id) {
            this.id = id;
            return this;
        }

        @Override
        public ControllerBindingBuilder identifier(String namespace, String path) {
            return identifier(new ResourceLocation(namespace, path));
        }

        @Override
        public ControllerBindingBuilder defaultBind(Input input) {
            this.input = input;
            return this;
        }

        @Override
        public ControllerBindingBuilder hardcodedBind(Function<ControllerStateView, Float> bind) {
            this.hardcodedBind = bind;
            return this;
        }

        @Override
        public ControllerBindingBuilder name(Component name) {
            this.name = name;
            return this;
        }

        @Override
        public ControllerBindingBuilder description(Component description) {
            this.description = description;
            return this;
        }

        @Override
        public ControllerBindingBuilder category(Component category) {
            this.category = category;
            return this;
        }

        @Override
        public ControllerBindingBuilder context(BindContext... contexts) {
            this.contexts.addAll(Set.of(contexts));
            return this;
        }

        @Override
        public ControllerBindingBuilder radialCandidate(ResourceLocation icon) {
            this.radialIcon = icon;
            return this;
        }

        @Override
        public ControllerBindingBuilder vanillaOverride(KeyMapping keyMapping, BooleanSupplier toggleable) {
            this.override = new KeyMappingOverride(keyMapping, toggleable);
            return this;
        }

        @Override
        public ControllerBindingBuilder vanillaOverride(KeyMapping keyMapping) {
            return vanillaOverride(keyMapping, () -> false);
        }

        @Override
        public ControllerBinding build() {
            Validate.notNull(id, "Identifier must be set");
            Validate.notNull(input, "Default bind must be set");
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

            return new ControllerBindingImpl(controller, input, hardcodedBind, id, override, name, description, category, contexts, radialIcon);
        }
    }
}
