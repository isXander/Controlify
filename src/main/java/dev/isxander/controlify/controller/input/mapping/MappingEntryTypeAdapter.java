package dev.isxander.controlify.controller.input.mapping;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class MappingEntryTypeAdapter implements JsonDeserializer<MappingEntry> {
    @Override
    public MappingEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        InOutRecord inOut = context.deserialize(json, InOutRecord.class);

        Type type = switch (inOut.inputType()) {
            case BUTTON -> switch (inOut.outputType()) {
                case BUTTON -> MappingEntry.FromButton.ToButton.class;
                case AXIS -> MappingEntry.FromButton.ToAxis.class;
                case HAT -> MappingEntry.FromButton.ToHat.class;
                case NOTHING -> throw new IllegalStateException();
            };
            case AXIS -> switch (inOut.outputType()) {
                case BUTTON -> MappingEntry.FromAxis.ToButton.class;
                case AXIS -> MappingEntry.FromAxis.ToAxis.class;
                case HAT -> MappingEntry.FromAxis.ToHat.class;
                case NOTHING -> throw new IllegalStateException();
            };
            case HAT -> switch (inOut.outputType()) {
                case BUTTON -> MappingEntry.FromHat.ToButton.class;
                case AXIS -> MappingEntry.FromHat.ToAxis.class;
                case HAT -> MappingEntry.FromHat.ToHat.class;
                case NOTHING -> throw new IllegalStateException();
            };
            case NOTHING -> switch (inOut.outputType()) {
                case BUTTON -> MappingEntry.FromNothing.ToButton.class;
                case AXIS -> MappingEntry.FromNothing.ToAxis.class;
                case HAT -> MappingEntry.FromNothing.ToHat.class;
                case NOTHING -> throw new IllegalStateException();
            };
        };

        return context.deserialize(json, type);
    }

    private record InOutRecord(MapType inputType, MapType outputType) {
    }
}
