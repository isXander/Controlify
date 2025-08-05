package dev.isxander.controlify.screenkeyboard;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public record KeyboardLayout(float rowWidth, List<List<ShiftableKey>> keys) {
    public static final Codec<KeyboardLayout> CODEC = RecordCodecBuilder.<KeyboardLayout>create(instance -> instance.group(
            Codec.floatRange(1, Float.MAX_VALUE).fieldOf("width").forGetter(KeyboardLayout::rowWidth),
            ShiftableKey.CODEC
                    .listOf(1, Integer.MAX_VALUE)
                    .listOf(1, Integer.MAX_VALUE)
                    .fieldOf("keys").forGetter(KeyboardLayout::keys)
    ).apply(instance, KeyboardLayout::new)).validate(
            layout ->
                validateRowWidths(layout)
                        ? DataResult.success(layout)
                        : DataResult.error(() -> "Row widths do not match the specified row width: " + layout.rowWidth())
    );

    public static boolean validateRowWidths(KeyboardLayout layout) {
        return layout.keys().stream()
                .mapToDouble(row -> row.stream()
                        .mapToDouble(ShiftableKey::width)
                        .sum())
                .allMatch(rowWidth -> rowWidth == layout.rowWidth());
    }

    public record ShiftableKey(Key regular, Key shifted, Optional<InputBindingSupplier> shortcutBinding) {
        private static final Codec<ShiftableKey> PAIR_CODEC = Codec.withAlternative(
                RecordCodecBuilder.create(instance -> instance.group(
                        Key.CODEC.fieldOf("regular").forGetter(ShiftableKey::regular),
                        Key.CODEC.optionalFieldOf("shifted").forGetter(k -> Optional.of(k.shifted)),
                        InputBindingSupplier.CODEC.optionalFieldOf("shortcut").forGetter(ShiftableKey::shortcutBinding)
                ).apply(instance, ShiftableKey::new)),
                Key.CODEC.listOf(2, 2).xmap(
                        list -> new ShiftableKey(list.get(0), list.get(1), Optional.empty()),
                        key -> List.of(key.regular(), key.shifted())
                )
        );

        public static Codec<ShiftableKey> CODEC = Codec.either(Key.CODEC, PAIR_CODEC)
                .xmap(
                        either -> either.map(ShiftableKey::new, Function.identity()),
                        Either::right
                ).validate(key -> key.validateWidths()
                        ? DataResult.success(key)
                        : DataResult.error(() -> "Regular and shifted keys have different widths: " + key)
                );

        public ShiftableKey(Key key) {
            this(key, key.createUppercase().orElse(key), Optional.empty());
        }

        public ShiftableKey(Key regular, Optional<Key> shifted, Optional<InputBindingSupplier> shortcutBinding) {
            this(regular, shifted.orElse(regular.createUppercase().orElse(regular)), shortcutBinding);
        }

        public Key get(boolean shifted) {
            return shifted ? this.shifted : this.regular;
        }

        public float width() {
            return regular.width();
        }

        public boolean validateWidths() {
            return regular().width() == shifted().width();
        }
    }

    public sealed interface Key {
        float width();

        Component displayName();

        default Optional<Key> createUppercase() {
            return Optional.empty();
        }

        Codec<Key> CODEC = new Codec<>() {
            @Override
            public <T> DataResult<T> encode(Key input, DynamicOps<T> ops, T prefix) {
                return switch (input) {
                    case StringKey stringKey -> StringKey.CODEC.encode(stringKey, ops, prefix);
                    case CodeKey codeKey -> CodeKey.CODEC.encode(codeKey, ops, prefix);
                    case SpecialKey specialKey -> SpecialKey.CODEC.encode(specialKey, ops, prefix);
                };
            }

            @Override
            public <T> DataResult<Pair<Key, T>> decode(DynamicOps<T> ops, T input) {
                return Stream.of(StringKey.CODEC, CodeKey.CODEC, SpecialKey.CODEC)
                        .map(decoder -> decoder.decode(ops, input))
                        .map(r -> r.map(p -> p.mapFirst(t -> (Key) t)))
                        .filter(DataResult::isSuccess)
                        .findFirst()
                        .orElseGet(() -> DataResult.error(() -> "No decoder matched."));
            }
        };

        record StringKey(String string, float width) implements Key {
            private static final Codec<String> STR_CODEC = Codec.string(1, Integer.MAX_VALUE);

            public static final Codec<StringKey> CODEC = Codec.withAlternative(
                    RecordCodecBuilder.create(instance -> instance.group(
                            STR_CODEC.fieldOf("chars").forGetter(StringKey::string),
                            Codec.FLOAT.optionalFieldOf("width", 1.0f).forGetter(StringKey::width)
                    ).apply(instance, StringKey::new)),
                    STR_CODEC.xmap(StringKey::new, StringKey::string)
            );

            public StringKey(String string) {
                this(string, 1.0f);
            }

            @Override
            public Component displayName() {
                return Component.literal(string);
            }

            @Override
            public Optional<Key> createUppercase() {
                return Optional.of(new StringKey(string.toUpperCase(), width));
            }
        }

        record CodeKey(int keycode, int scancode, int modifier, Component displayName, float width) implements Key {
            public static final Codec<CodeKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("keycode").forGetter(CodeKey::keycode),
                    Codec.INT.optionalFieldOf("scancode", 0).forGetter(CodeKey::scancode),
                    Codec.INT.optionalFieldOf("modifier", 0).forGetter(CodeKey::modifier),
                    ComponentSerialization.CODEC.fieldOf("display_name").forGetter(CodeKey::displayName),
                    Codec.FLOAT.optionalFieldOf("width", 1.0f).forGetter(CodeKey::width)
            ).apply(instance, CodeKey::new));

            public CodeKey(int keycode, int scancode, int modifier, Component displayName) {
                this(keycode, scancode, modifier, displayName, 1.0f);
            }
        }

        record SpecialKey(Action action, float width) implements Key {
            public static final Codec<SpecialKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    StringRepresentable.fromEnum(Action::values).fieldOf("action").forGetter(SpecialKey::action),
                    Codec.FLOAT.optionalFieldOf("width", 1.0f).forGetter(SpecialKey::width)
            ).apply(instance, SpecialKey::new));

            public SpecialKey(Action action) {
                this(action, 1.0f);
            }

            @Override
            public Component displayName() {
                return action().displayName();
            }

            public enum Action implements StringRepresentable {
                SHIFT("shift"),
                ENTER("enter"),
                BACKSPACE("backspace"),
                TAB("tab"),
                LEFT_ARROW("left_arrow"),
                RIGHT_ARROW("right_arrow"),
                UP_ARROW("up_arrow"),
                DOWN_ARROW("down_arrow");

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
    }
}
