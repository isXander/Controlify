package dev.isxander.controlify.controller.joystick.mapping;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.JoystickAxisBind;
import dev.isxander.controlify.controller.ControllerType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPJoystickMapping implements JoystickMapping {
    private static final Gson gson = new Gson();

    private final Map<Integer, AxisMapping> axisMappings;
    private final Map<Integer, ButtonMapping> buttonMappings;
    private final Map<Integer, HatMapping> hatMappings;

    public RPJoystickMapping(JsonObject object, ControllerType type) {
        axisMappings = new HashMap<>();
        object.getAsJsonArray("axes").forEach(element -> {
            var axis = element.getAsJsonObject();
            List<Integer> ids = axis.getAsJsonArray("ids").asList().stream().map(JsonElement::getAsInt).toList();

            Vec2 inpRange = null;
            Vec2 outRange = null;
            if (axis.has("range")) {
                var rangeElement = axis.get("range");
                if (rangeElement.isJsonArray()) {
                    var rangeArray = rangeElement.getAsJsonArray();
                    outRange = new Vec2(rangeArray.get(0).getAsFloat(), rangeArray.get(1).getAsFloat());
                    inpRange = new Vec2(-1, 1);
                } else if (rangeElement.isJsonObject()) {
                    var rangeObject = rangeElement.getAsJsonObject();

                    var inpRangeArray = rangeObject.getAsJsonArray("in");
                    inpRange = new Vec2(inpRangeArray.get(0).getAsFloat(), inpRangeArray.get(1).getAsFloat());

                    var outRangeArray = rangeObject.getAsJsonArray("out");
                    outRange = new Vec2(outRangeArray.get(0).getAsFloat(), outRangeArray.get(1).getAsFloat());
                }
            }
            var restState = axis.get("rest").getAsFloat();
            var deadzone = axis.get("deadzone").getAsBoolean();
            var identifier = axis.get("identifier").getAsString();

            var axisNames = axis.getAsJsonArray("axis_names").asList().stream()
                    .map(JsonElement::getAsJsonArray)
                    .map(JsonArray::asList)
                    .map(list -> list.stream().map(JsonElement::getAsString).toList())
                    .toList();

            for (var id : ids) {
                axisMappings.put(id, new AxisMapping(ids, identifier, inpRange, outRange, restState, deadzone, type.identifier(), axisNames));
            }
        });

        buttonMappings = new HashMap<>();
        object.getAsJsonArray("buttons").forEach(element -> {
            var button = element.getAsJsonObject();
            buttonMappings.put(button.get("button").getAsInt(), new ButtonMapping(button.get("name").getAsString(), type.identifier()));
        });

        hatMappings = new HashMap<>();
        object.getAsJsonArray("hats").forEach(element -> {
            var hat = element.getAsJsonObject();
            hatMappings.put(hat.get("hat").getAsInt(), new HatMapping(hat.get("name").getAsString(), type.identifier()));
        });
    }

    @Override
    public Axis axis(int axis) {
        if (!axisMappings.containsKey(axis))
            return UnmappedJoystickMapping.INSTANCE.axis(axis);
        return axisMappings.get(axis);
    }

    @Override
    public Button button(int button) {
        if (!buttonMappings.containsKey(button))
            return UnmappedJoystickMapping.INSTANCE.button(button);
        return buttonMappings.get(button);
    }

    @Override
    public Hat hat(int hat) {
        if (!hatMappings.containsKey(hat))
            return UnmappedJoystickMapping.INSTANCE.hat(hat);
        return hatMappings.get(hat);
    }

    public static JoystickMapping fromType(ControllerType type) {
        var resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("controlify", "mappings/" + type.identifier() + ".json"));
        if (resource.isEmpty()) {
            Controlify.LOGGER.warn("No joystick mapping found for controller: '" + type.identifier() + "'");
            return UnmappedJoystickMapping.INSTANCE;
        }

        try (var reader = resource.get().openAsReader()) {
            return new RPJoystickMapping(gson.fromJson(reader, JsonObject.class), type);
        } catch (Exception e) {
            Controlify.LOGGER.error("Failed to load joystick mapping for controller: '" + type.identifier() + "'", e);
            return UnmappedJoystickMapping.INSTANCE;
        }
    }

    private record AxisMapping(List<Integer> ids, String identifier, Vec2 inpRange, Vec2 outRange, float restingValue, boolean requiresDeadzone, String typeId, List<List<String>> axisNames) implements Axis {
        @Override
        public float modifyAxis(float value) {
            if (inpRange() == null || outRange() == null)
                return value;

            return (value + (outRange().x - inpRange().x)) / (inpRange().y - inpRange().x) * (outRange().y - outRange().x);
        }

        @Override
        public boolean isAxisResting(float value) {
            return value == restingValue();
        }

        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping." + typeId() + ".axis." + identifier());
        }

        @Override
        public String getDirectionIdentifier(int axis, JoystickAxisBind.AxisDirection direction) {
            return this.axisNames().get(ids.indexOf(axis)).get(direction.ordinal());
        }
    }

    private record ButtonMapping(String identifier, String typeId) implements Button {
        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping." + typeId() + ".button." + identifier());
        }


    }

    private record HatMapping(String identifier, String typeId) implements Hat {
        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping." + typeId() + ".hat." + identifier());
        }
    }
}
