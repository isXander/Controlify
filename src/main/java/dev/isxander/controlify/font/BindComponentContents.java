package dev.isxander.controlify.font;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class BindComponentContents implements ComponentContents {
    public static final MapCodec<BindComponentContents> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("binding")
                            .forGetter(BindComponentContents::binding)
            ).apply(instance, BindComponentContents::new)
    );
    public static final Type<BindComponentContents> TYPE = new Type<>(CODEC, "controlify:binding");

    private final ResourceLocation binding;
    private @Nullable Supplier<Component> inputSupplier;

    public BindComponentContents(ResourceLocation binding) {
        this.binding = binding;
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return inputSupplier.get().visit(contentConsumer);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return inputSupplier.get().visit(styledContentConsumer, style);
    }

    @Override
    public @NotNull Type<?> type() {
        return TYPE;
    }

    public ResourceLocation binding() {
        return binding;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof BindComponentContents contents) {
            return Objects.equals(binding, contents.binding);
        }
        return false;
    }

    @Override
    public String toString() {
        return "BindComponentContents[" +
                "binding=" + binding + ']';
    }

}
