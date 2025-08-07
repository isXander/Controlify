package dev.isxander.controlify.screenkeyboard;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents a keyboard layout.
 * A keyboard layout consists of keys within rows, it is a column-aligned layout,
 * meaning keys cannot span multiple columns.
 * A keyboard layout has a single shift-layer, defined by the {@link Key}.
 * @param width the unit width of the keyboard layout. all rows must sum to this width.
 * @param keys the rows of keys in the keyboard layout, row-major order.
 */
public record KeyboardLayout(float width, List<List<Key>> keys) {
    public static final Codec<KeyboardLayout> CODEC = RecordCodecBuilder.<KeyboardLayout>create(instance -> instance.group(
            Codec.floatRange(1, Float.MAX_VALUE).fieldOf("width").forGetter(KeyboardLayout::width),
            Key.CODEC
                    .listOf(1, Integer.MAX_VALUE)
                    .listOf(1, Integer.MAX_VALUE)
                    .fieldOf("keys").forGetter(KeyboardLayout::keys)
    ).apply(instance, KeyboardLayout::new)).validate(
            layout ->
                validateRowWidths(layout)
                        ? DataResult.success(layout)
                        : DataResult.error(() -> "Row widths do not match the specified row width: " + layout.width())
    );

    public static boolean validateRowWidths(KeyboardLayout layout) {
        return layout.keys().stream()
                .mapToDouble(row -> row.stream()
                        .mapToDouble(Key::width)
                        .sum())
                .allMatch(rowWidth -> rowWidth == layout.width());
    }

    @SafeVarargs
    public static KeyboardLayout of(float width, List<Key>... keys) {
        var layout = new KeyboardLayout(width, List.of(keys));
        Validate.isTrue(validateRowWidths(layout), "All row widths do not match the specified row width: " + width);
        return layout;
    }

    /**
     * Represents a key within a keyboard layout.
     * <p>
     * All keys have a regular and a shifted function, a shifted function can be created
     * from a regular function with {@link KeyFunction#createShifted()} if it is not explicitly defined.
     * Regular functions can return themselves as shifted functions if they do not support shifting.
     * <p>
     * Keys have a unit width, all keys within a row must sum to the width of the keyboard, defined by {@link KeyboardLayout#width()}.
     * When rendered, the unit width of the key is multiplied by the real width of the keyboard to determine the actual pixel width of the key.
     * <p>
     * Keys can also have an optional shortcut binding, which is used to display a shortcut for the key in the UI.
     * <p>
     * Each key function provides the display name of the key with {@link KeyFunction#displayName()}.
     * @param regular the regular key function, which is used when the shift is not enabled.
     * @param shifted the shifted key function, which is used when the shift is enabled
     * @param width the unit width of the key, which is multiplied by the keyboard width to determine the actual pixel width of the key
     * @param shortcutBinding an optional shortcut binding for the key, used to display a shortcut in the UI
     * @param identifier an optional identifier for the key, used when changing layouts to focus a specific key with a matching identifier
     */
    public record Key(KeyFunction regular, KeyFunction shifted, float width, Optional<InputBindingSupplier> shortcutBinding, @Nullable String identifier) {
        private static final Codec<Float> WIDTH_CODEC = Codec.floatRange(0.1f, Float.MAX_VALUE);

        private static final Codec<Key> PAIR_CODEC = Codec.withAlternative(
                RecordCodecBuilder.create(instance -> instance.group(
                        KeyFunction.CODEC.fieldOf("regular").forGetter(Key::regular),
                        KeyFunction.CODEC.optionalFieldOf("shifted").forGetter(k -> Optional.of(k.shifted)),
                        WIDTH_CODEC.optionalFieldOf("width", 1f).forGetter(Key::width),
                        InputBindingSupplier.CODEC.optionalFieldOf("shortcut").forGetter(Key::shortcutBinding),
                        Codec.STRING.optionalFieldOf("identifier").forGetter(k -> Optional.ofNullable(k.identifier))
                ).apply(instance, Key::fromCodec)),
                CExtraCodecs.arrayPair(KeyFunction.CODEC, Key::regular, Key::shifted, Key::new)
        );

        public static Codec<Key> CODEC = Codec.either(KeyFunction.CODEC, PAIR_CODEC)
                .xmap(
                        either -> either.map(Key::new, Function.identity()),
                        Either::right
                );

        public Key(KeyFunction keyFunction) {
            this(keyFunction, keyFunction.createShifted());
        }

        public Key(KeyFunction keyFunction, float width) {
            this(keyFunction, width, Optional.empty());
        }

        public Key(KeyFunction keyFunction, float width, Optional<InputBindingSupplier> shortcut) {
            this(keyFunction, keyFunction.createShifted(), width, shortcut, null);
        }

        public Key(KeyFunction regular, KeyFunction shifted) {
            this(regular, shifted, 1f, Optional.empty(), null);
        }

        private static Key fromCodec(KeyFunction regular, Optional<KeyFunction> shifted, float width, Optional<InputBindingSupplier> shortcut, Optional<String> identifier) {
            return new Key(
                    regular,
                    shifted.orElseGet(regular::createShifted),
                    width,
                    shortcut,
                    identifier.orElse(null)
            );
        }

        public KeyFunction getFunction(boolean shifted) {
            return shifted ? this.shifted : this.regular;
        }
    }

    /**
     * Represents a function of a key, which could be inserting a string,
     * imitating a key code, performing a special action, or changing the keyboard layout.
     * <p>
     * Functions are responsible for defining what happens when a key is pressed,
     * as well as proving a display name for the key.
     * <p>
     * The implementation of the function is not defined here, and is up to the consumer,
     * in this case, {@link KeyWidget}, to handle each sealed implementation of this interface.
     */
    public sealed interface KeyFunction {
        /**
         * Returns the display name of the key function.
         * Used to display the key in the UI.
         * @return the display name of the key function
         */
        Component displayName();

        /**
         * Creates a shifted version of this key function.
         * May return itself if the function does not support shifting.
         * @return the shifted key function
         */
        default KeyFunction createShifted() {
            return this;
        }

        Codec<KeyFunction> CODEC = new Codec<>() {
            @Override
            public <T> DataResult<T> encode(KeyFunction input, DynamicOps<T> ops, T prefix) {
                return switch (input) {
                    case StringFunc stringKey -> StringFunc.CODEC.encode(stringKey, ops, prefix);
                    case CodeFunc codeKey -> CodeFunc.CODEC.encode(codeKey, ops, prefix);
                    case SpecialFunc specialKey -> SpecialFunc.CODEC.encode(specialKey, ops, prefix);
                    case ChangeLayoutFunc changeLayoutKey -> ChangeLayoutFunc.CODEC.encode(changeLayoutKey, ops, prefix);
                };
            }

            @Override
            public <T> DataResult<Pair<KeyFunction, T>> decode(DynamicOps<T> ops, T input) {
                return Stream.of(StringFunc.CODEC, CodeFunc.CODEC, SpecialFunc.CODEC, ChangeLayoutFunc.CODEC)
                        .map(decoder -> decoder.decode(ops, input))
                        .map(r -> r.map(p -> p.mapFirst(t -> (KeyFunction) t)))
                        .filter(DataResult::isSuccess)
                        .findFirst()
                        .orElseGet(() -> DataResult.error(() -> "No decoder matched."));
            }
        };

        /**
         * A key function that inserts a string when pressed.
         * <p>
         * This function supports {@link #createShifted()} which uses {@link String#toUpperCase()} to create a shifted version of the string.
         *
         * @param string the string to insert when the key is pressed
         * @param manualDisplayName an optional manual display name for the key, if not provided, the string itself is used when returning {@link #displayName()}
         */
        record StringFunc(String string, @Nullable Component manualDisplayName) implements KeyFunction {
            public static final Codec<StringFunc> CODEC = Codec.withAlternative(
                    RecordCodecBuilder.create(instance -> instance.group(
                            Codec.STRING.fieldOf("chars").forGetter(StringFunc::string),
                            ComponentSerialization.CODEC.optionalFieldOf("display_name").forGetter(k -> Optional.of(k.displayName()))
                    ).apply(instance, StringFunc::fromCodec)),
                    Codec.STRING.xmap(StringFunc::new, StringFunc::string)
            );

            public StringFunc(String string) {
                this(string, null);
            }

            private static StringFunc fromCodec(String string, Optional<Component> displayName) {
                return new StringFunc(string, displayName.orElse(null));
            }

            @Override
            public Component displayName() {
                return manualDisplayName != null ? manualDisplayName : Component.literal(string);
            }

            @Override
            public KeyFunction createShifted() {
                return new StringFunc(string.toUpperCase(), manualDisplayName);
            }
        }

        /**
         * A key function that inserts a list of key codes when pressed.
         * <p>
         * This function does not support {@link #createShifted()} as key codes cannot be automatically upper-cased.
         *
         * @param codes the list of key codes to insert when the key is pressed
         * @param displayName the display name of the key function, used to display the key in the UI
         */
        record CodeFunc(List<KeyCode> codes, Component displayName) implements KeyFunction {
            public static final Codec<CodeFunc> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    KeyCode.CODEC.listOf().fieldOf("codes").forGetter(CodeFunc::codes),
                    ComponentSerialization.CODEC.fieldOf("display_name").forGetter(CodeFunc::displayName)
            ).apply(instance, CodeFunc::new));

            /**
             * A key code is a combination of a keycode, scancode, and modifier.
             * @param keycode is the logical key code, platform agnostic. use {@link com.mojang.blaze3d.platform.InputConstants} to get the key code.
             * @param scancode is the physical key code, platform specific. usually leaving blank is fine since no one ever looks at it.
             * @param modifier is the modifier bitset, which can be used to specify additional key modifiers like shift, ctrl, alt, etc.
             *                 use {@link org.lwjgl.glfw.GLFW#GLFW_MOD_SHIFT} etc
             */
            record KeyCode(int keycode, int scancode, int modifier) {
                public static final Codec<KeyCode> CODEC = Codec.withAlternative(
                        RecordCodecBuilder.create(instance -> instance.group(
                                Codec.INT.fieldOf("keycode").forGetter(KeyCode::keycode),
                                Codec.INT.optionalFieldOf("scancode", 0).forGetter(KeyCode::scancode),
                                Codec.INT.optionalFieldOf("modifier", 0).forGetter(KeyCode::modifier)
                        ).apply(instance, KeyCode::new)),
                        Codec.INT.xmap(keycode -> new KeyCode(keycode, 0, 0), KeyCode::keycode)
                );

                public KeyCode(int keycode) {
                    this(keycode, 0, 0);
                }
            }
        }

        /**
         * A key function that performs a special action when pressed.
         * <p>
         * Some of these special actions may be shorthands for other key functions,
         * such as inserting specific key codes like {@link Action#LEFT_ARROW}.
         * <p>
         * This function does not support {@link #createShifted()} as special actions cannot be automatically upper-cased.
         *
         * @param action the action to perform when the key is pressed
         */
        record SpecialFunc(Action action) implements KeyFunction {
            public static final Codec<SpecialFunc> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    StringRepresentable.fromEnum(Action::values).fieldOf("action").forGetter(SpecialFunc::action)
            ).apply(instance, SpecialFunc::new));

            @Override
            public Component displayName() {
                return action().displayName();
            }

            public enum Action implements StringRepresentable {
                SHIFT("shift"),
                SHIFT_LOCK("shift_lock"),
                ENTER("enter"),
                BACKSPACE("backspace"),
                TAB("tab"),
                LEFT_ARROW("left_arrow"),
                RIGHT_ARROW("right_arrow"),
                UP_ARROW("up_arrow"),
                DOWN_ARROW("down_arrow"),
                COPY_ALL("copy_all"),
                PASTE("paste"),
                PREVIOUS_LAYOUT("previous_layout");

                private final String serialName;

                Action(String serialName) {
                    this.serialName = serialName;
                }

                @Override
                public @NotNull String getSerializedName() {
                    return this.serialName;
                }

                public Component displayName() {
                    return Component.translatable("controlify.keyboard.special." + this.serialName);
                }
            }
        }

        /**
         * A key function that changes the keyboard layout when pressed.
         * It can be any layout found by the resource reloader. A resource pack
         * could make their own arbitrarily named layout and reference it here.
         * <p>
         * If the layout referenced is not found, the {@link FallbackKeyboardLayout fallback layout} will be used instead.
         * <p>
         * This function does not support {@link #createShifted()} as layouts cannot be automatically upper-cased.
         *
         * @param layout the layout id to switch to when the key is pressed.
         * @param displayName the display name of the key function, used to display the key in the UI
         */
        record ChangeLayoutFunc(ResourceLocation layout, Component displayName) implements KeyFunction {

            public static final Codec<ChangeLayoutFunc> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("layout").forGetter(ChangeLayoutFunc::layout),
                    ComponentSerialization.CODEC.fieldOf("display_name").forGetter(ChangeLayoutFunc::displayName)
            ).apply(instance, ChangeLayoutFunc::fromCodec));

            private static ChangeLayoutFunc fromCodec(ResourceLocation layout, Component displayName) {
                return new ChangeLayoutFunc(layout, displayName);
            }
        }
    }
}
