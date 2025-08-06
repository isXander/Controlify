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

public record KeyboardLayout(float width, List<List<ShiftableKey>> keys) {
    public static final Codec<KeyboardLayout> CODEC = RecordCodecBuilder.<KeyboardLayout>create(instance -> instance.group(
            Codec.floatRange(1, Float.MAX_VALUE).fieldOf("width").forGetter(KeyboardLayout::width),
            ShiftableKey.CODEC
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
                        .mapToDouble(ShiftableKey::width)
                        .sum())
                .allMatch(rowWidth -> rowWidth == layout.width());
    }

    @SafeVarargs
    public static KeyboardLayout of(float width, List<ShiftableKey>... keys) {
        var layout = new KeyboardLayout(width, List.of(keys));
        Validate.isTrue(validateRowWidths(layout), "All row widths do not match the specified row width: " + width);
        return layout;
    }

    public record ShiftableKey(Key regular, Key shifted, float width, Optional<InputBindingSupplier> shortcutBinding) {
        private static final Codec<Float> WIDTH_CODEC = Codec.floatRange(0.1f, Float.MAX_VALUE);

        private static final Codec<ShiftableKey> PAIR_CODEC = Codec.withAlternative(
                RecordCodecBuilder.create(instance -> instance.group(
                        Key.CODEC.fieldOf("regular").forGetter(ShiftableKey::regular),
                        Key.CODEC.optionalFieldOf("shifted").forGetter(k -> Optional.of(k.shifted)),
                        WIDTH_CODEC.optionalFieldOf("width", 1f).forGetter(ShiftableKey::width),
                        InputBindingSupplier.CODEC.optionalFieldOf("shortcut").forGetter(ShiftableKey::shortcutBinding)
                ).apply(instance, ShiftableKey::fromCodec)),
                CExtraCodecs.arrayPair(Key.CODEC, ShiftableKey::regular, ShiftableKey::shifted, ShiftableKey::new)
        );

        public static Codec<ShiftableKey> CODEC = Codec.either(Key.CODEC, PAIR_CODEC)
                .xmap(
                        either -> either.map(ShiftableKey::new, Function.identity()),
                        Either::right
                );

        public ShiftableKey(Key key) {
            this(key, key.createUppercase());
        }

        public ShiftableKey(Key key, float width) {
            this(key, width, Optional.empty());
        }

        public ShiftableKey(Key key, float width, Optional<InputBindingSupplier> shortcut) {
            this(key, key.createUppercase(), width, shortcut);
        }

        public ShiftableKey(Key regular, Key shifted) {
            this(regular, shifted, 1f, Optional.empty());
        }

        private static ShiftableKey fromCodec(Key regular, Optional<Key> shifted, float width, Optional<InputBindingSupplier> shortcut) {
            return new ShiftableKey(
                    regular,
                    shifted.orElseGet(regular::createUppercase),
                    width,
                    shortcut
            );
        }

        public Key get(boolean shifted) {
            return shifted ? this.shifted : this.regular;
        }
    }

    public sealed interface Key {
        Component displayName();

        @Nullable String identifier();

        default Key createUppercase() {
            return this;
        }

        Codec<Key> CODEC = new Codec<>() {
            @Override
            public <T> DataResult<T> encode(Key input, DynamicOps<T> ops, T prefix) {
                return switch (input) {
                    case StringKey stringKey -> StringKey.CODEC.encode(stringKey, ops, prefix);
                    case CodeKey codeKey -> CodeKey.CODEC.encode(codeKey, ops, prefix);
                    case SpecialKey specialKey -> SpecialKey.CODEC.encode(specialKey, ops, prefix);
                    case ChangeLayoutKey changeLayoutKey -> ChangeLayoutKey.CODEC.encode(changeLayoutKey, ops, prefix);
                };
            }

            @Override
            public <T> DataResult<Pair<Key, T>> decode(DynamicOps<T> ops, T input) {
                return Stream.of(StringKey.CODEC, CodeKey.CODEC, SpecialKey.CODEC, ChangeLayoutKey.CODEC)
                        .map(decoder -> decoder.decode(ops, input))
                        .map(r -> r.map(p -> p.mapFirst(t -> (Key) t)))
                        .filter(DataResult::isSuccess)
                        .findFirst()
                        .orElseGet(() -> DataResult.error(() -> "No decoder matched."));
            }
        };

        record StringKey(String string, @Nullable Component manualDisplayName, @Nullable String identifier) implements Key {
            public static final Codec<StringKey> CODEC = Codec.withAlternative(
                    RecordCodecBuilder.create(instance -> instance.group(
                            Codec.STRING.fieldOf("chars").forGetter(StringKey::string),
                            ComponentSerialization.CODEC.optionalFieldOf("display_name").forGetter(k -> Optional.of(k.displayName())),
                            Codec.STRING.optionalFieldOf("identifier").forGetter(k -> Optional.ofNullable(k.identifier))
                    ).apply(instance, StringKey::fromCodec)),
                    Codec.STRING.xmap(StringKey::new, StringKey::string)
            );

            public StringKey(String string) {
                this(string, null, null);
            }

            private static StringKey fromCodec(String string, Optional<Component> displayName, Optional<String> identifier) {
                return new StringKey(string, displayName.orElse(null), identifier.orElse(null));
            }

            @Override
            public Component displayName() {
                return manualDisplayName != null ? manualDisplayName : Component.literal(string);
            }

            @Override
            public Key createUppercase() {
                return new StringKey(string.toUpperCase(), manualDisplayName, identifier);
            }
        }

        record CodeKey(int keycode, int scancode, int modifier, Component displayName, @Nullable String identifier) implements Key {
            public static final Codec<CodeKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("keycode").forGetter(CodeKey::keycode),
                    Codec.INT.optionalFieldOf("scancode", 0).forGetter(CodeKey::scancode),
                    Codec.INT.optionalFieldOf("modifier", 0).forGetter(CodeKey::modifier),
                    ComponentSerialization.CODEC.fieldOf("display_name").forGetter(CodeKey::displayName),
                    Codec.STRING.optionalFieldOf("identifier").forGetter(k -> Optional.ofNullable(k.identifier))
            ).apply(instance, CodeKey::fromCodec));

            public CodeKey(int keycode, int scancode, int modifier, Component displayName) {
                this(keycode, scancode, modifier, displayName, null);
            }

            private static CodeKey fromCodec(int keycode, int scancode, int modifier, Component displayName, Optional<String> identifier) {
                return new CodeKey(keycode, scancode, modifier, displayName, identifier.orElse(null));
            }
        }

        record SpecialKey(Action action, @Nullable String identifier) implements Key {
            public static final Codec<SpecialKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    StringRepresentable.fromEnum(Action::values).fieldOf("action").forGetter(SpecialKey::action),
                    Codec.STRING.optionalFieldOf("identifier").forGetter(k -> Optional.ofNullable(k.identifier))
            ).apply(instance, SpecialKey::fromCodec));

            public SpecialKey(Action action) {
                this(action, null);
            }

            private static SpecialKey fromCodec(Action action, Optional<String> identifier) {
                return new SpecialKey(action, identifier.orElse(null));
            }

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

        record ChangeLayoutKey(ResourceLocation otherLayout, Component displayName, @Nullable String identifier) implements Key {
            public static final Codec<ChangeLayoutKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("layout").forGetter(ChangeLayoutKey::otherLayout),
                    ComponentSerialization.CODEC.fieldOf("display_name").forGetter(ChangeLayoutKey::displayName),
                    Codec.STRING.optionalFieldOf("identifier").forGetter(k -> Optional.ofNullable(k.identifier))
            ).apply(instance, ChangeLayoutKey::fromCodec));

            private static ChangeLayoutKey fromCodec(ResourceLocation layout, Component displayName, Optional<String> identifier) {
                return new ChangeLayoutKey(layout, displayName, identifier.orElse(null));
            }
        }
    }
}
