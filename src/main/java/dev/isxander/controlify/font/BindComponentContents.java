package dev.isxander.controlify.font;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.ControlifyApi;
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

public record BindComponentContents(ResourceLocation binding) implements ComponentContents {
    public static final MapCodec<BindComponentContents> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("binding")
                            .forGetter(BindComponentContents::binding)
            ).apply(instance, BindComponentContents::new)
    );
    public static final Type<BindComponentContents> TYPE = new Type<>(CODEC, "controlify:binding");

    @Override
    public <T> @NotNull Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return this.getComponent().flatMap(c -> c.visit(contentConsumer));
    }

    @Override
    public <T> @NotNull Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return this.getComponent().flatMap(c -> c.visit(styledContentConsumer, style));
    }

    private Optional<Component> getComponent() {
        return ControlifyApi.get().getCurrentController()
                .map(controller -> Controlify.instance().inputFontMapper().getComponentFromBinding(
                        controller.info().type().namespace(), controller.bindings().get(binding)));
    }

    @Override
    public @NotNull Type<?> type() {
        return TYPE;
    }

}
