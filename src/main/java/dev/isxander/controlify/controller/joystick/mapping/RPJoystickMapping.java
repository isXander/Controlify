package dev.isxander.controlify.controller.joystick.mapping;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.JoystickAxisBind;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.controller.joystick.render.JoystickRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;

import java.io.IOException;
import java.util.*;

public class RPJoystickMapping implements JoystickMapping {

    private final AxisMapping[] axes;
    private final ButtonMapping[] buttons;
    private final HatMapping[] hats;

    public RPJoystickMapping(JsonReader reader, ControllerType type) throws IOException {
        AxisMapping[] axes = null;
        ButtonMapping[] buttons = null;
        HatMapping[] hats = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "axes" -> {
                    if (axes != null)
                        throw new IllegalStateException("Axes defined twice.");
                    axes = readAxes(reader, type);
                }
                case "buttons" -> {
                    if (buttons != null)
                        throw new IllegalStateException("Buttons defined twice.");
                    buttons = readButtons(reader, type);
                }
                case "hats" -> {
                    if (hats != null)
                        throw new IllegalStateException("Hats defined twice.");
                    hats = readHats(reader, type);
                }
                default -> {
                    Controlify.LOGGER.warn("Unknown field in joystick mapping: " + name + ". Expected values: ['axes', 'buttons', 'hats']");
                    reader.skipValue();
                }
            }
        }
        reader.endObject();

        this.axes = axes;
        this.buttons = buttons;
        this.hats = hats;
    }

    private AxisMapping[] readAxes(JsonReader reader, ControllerType type) throws IOException {
        List<AxisMapping> axes = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            List<Integer> ids = new ArrayList<>();
            Vec2 inpRange = null;
            Vec2 outRange = null;
            boolean deadzone = false;
            float restState = 0f;
            String identifier = null;
            List<String[]> axisNames = new ArrayList<>();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "ids" -> {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            ids.add(reader.nextInt());
                        }
                        reader.endArray();
                    }
                    case "identifier" -> {
                        identifier = reader.nextString();
                    }
                    case "range" -> {
                        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                            reader.beginArray();
                            outRange = new Vec2((float) reader.nextDouble(), (float) reader.nextDouble());
                            inpRange = new Vec2(-1, 1);
                            reader.endArray();
                        } else {
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String rangeName = reader.nextName();

                                switch (rangeName) {
                                    case "in" -> {
                                        reader.beginArray();
                                        inpRange = new Vec2((float) reader.nextDouble(), (float) reader.nextDouble());
                                        reader.endArray();
                                    }
                                    case "out" -> {
                                        reader.beginArray();
                                        outRange = new Vec2((float) reader.nextDouble(), (float) reader.nextDouble());
                                        reader.endArray();
                                    }
                                    default -> {
                                        reader.skipValue();
                                        Controlify.LOGGER.info("Unknown axis range property: " + rangeName + ". Expected are ['in', 'out']");
                                    }
                                }
                            }
                            reader.endObject();
                        }
                    }
                    case "rest" -> {
                        restState = (float) reader.nextDouble();
                    }
                    case "deadzone" -> {
                        deadzone = reader.nextBoolean();
                    }
                    case "axis_names" -> {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            reader.beginArray();
                            axisNames.add(new String[] { reader.nextString(), reader.nextString() });
                            reader.endArray();
                        }
                        reader.endArray();
                    }
                    default -> {
                        reader.skipValue();
                        Controlify.LOGGER.info("Unknown axis property: " + name + ". Expected are ['identifier', 'axis_names', 'ids', 'range', 'rest', 'deadzone']");
                    }
                }
            }
            reader.endObject();

            for (var id : ids) {
                axes.add(new AxisMapping(id, identifier, inpRange, outRange, restState, deadzone, type.mappingId(), axisNames.get(ids.indexOf(id))));
            }
        }
        reader.endArray();

        return axes.toArray(new AxisMapping[0]);
    }

    private ButtonMapping[] readButtons(JsonReader reader, ControllerType type) throws IOException {
        List<ButtonMapping> buttons = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            int id = -1;
            String btnName = null;

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "button" -> id = reader.nextInt();
                    case "name" -> btnName = reader.nextString();
                    default -> {
                        reader.skipValue();
                        Controlify.LOGGER.info("Unknown button property: " + name + ". Expected are ['button', 'name']");
                    }
                }
            }
            reader.endObject();

            buttons.add(new ButtonMapping(id, btnName, type.mappingId()));
        }
        reader.endArray();

        return buttons.toArray(new ButtonMapping[0]);
    }

    private HatMapping[] readHats(JsonReader reader, ControllerType type) throws IOException {
        List<HatMapping> hats = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            int id = -1;
            String hatName = null;
            HatMapping.EmulatedAxis axis = null;

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "hat" -> id = reader.nextInt();
                    case "name" -> hatName = reader.nextString();
                    case "emulated_axis" -> {
                        int axisId = -1;
                        Map<Float, JoystickState.HatState> states = new HashMap<>();

                        reader.beginObject();
                        while (reader.hasNext()) {
                            String emulatedName = reader.nextName();
                            for (var hatState : JoystickState.HatState.values()) {
                                if (hatState.name().equalsIgnoreCase(emulatedName)) {
                                    states.put((float) reader.nextDouble(), hatState);
                                }
                            }

                            if (emulatedName.equalsIgnoreCase("axis")) {
                                axisId = reader.nextInt();
                            }
                        }
                        reader.endObject();

                        if (axisId == -1) {
                            Controlify.LOGGER.error("No axis id defined for emulated hat " + hatName + "! Skipping.");
                            continue;
                        }
                        if (states.size() != JoystickState.HatState.values().length) {
                            Controlify.LOGGER.error("Not all hat states are defined for emulated hat " + hatName + "! Skipping.");
                            continue;
                        }

                        axis = new HatMapping.EmulatedAxis(axisId, states);
                    }
                    default -> {
                        reader.skipValue();
                        Controlify.LOGGER.info("Unknown hat property: " + name + ". Expected are ['hat', 'name']");
                    }
                }
            }
            reader.endObject();

            hats.add(new HatMapping(id, hatName, type.mappingId(), axis));
        }
        reader.endArray();

        return hats.toArray(new HatMapping[0]);
    }

    @Override
    public Axis[] axes() {
        return axes;
    }

    @Override
    public Button[] buttons() {
        return buttons;
    }

    @Override
    public Hat[] hats() {
        return hats;
    }

    public static JoystickMapping fromType(JoystickController<?> joystick) {
        var resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("controlify", "mappings/" + joystick.type().mappingId() + ".json"));
        if (resource.isEmpty()) {
            Controlify.LOGGER.warn("No joystick mapping found for controller: '" + joystick.type().mappingId() + "'");
            return new UnmappedJoystickMapping(joystick.joystickId());
        }

        try (var reader = JsonReader.json5(resource.get().openAsReader())) {
            return new RPJoystickMapping(reader, joystick.type());
        } catch (Exception e) {
            Controlify.LOGGER.error("Failed to load joystick mapping for controller: '" + joystick.type().mappingId() + "'", e);
            return new UnmappedJoystickMapping(joystick.joystickId());
        }
    }

    private record AxisMapping(int id, String identifier, Vec2 inpRange, Vec2 outRange, float restingValue, boolean requiresDeadzone, String theme, String[] axisNames) implements Axis {
        @Override
        public float getAxis(JoystickData data) {
            float rawAxis = data.axes()[id];

            if (inpRange() == null || outRange() == null)
                return rawAxis;

            return (rawAxis + (outRange().x - inpRange().x)) / (inpRange().y - inpRange().x) * (outRange().y - outRange().x);
        }

        @Override
        public boolean isAxisResting(float value) {
            return value == restingValue();
        }

        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping." + theme() + ".axis." + identifier());
        }

        @Override
        public String getDirectionIdentifier(int axis, JoystickAxisBind.AxisDirection direction) {
            return this.axisNames()[direction.ordinal()];
        }

        @Override
        public JoystickRenderer renderer() {
            return null;
        }
    }

    private record ButtonMapping(int id, String identifier, String typeId) implements Button {
        @Override
        public boolean isPressed(JoystickData data) {
            return data.buttons()[id];
        }

        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping." + typeId() + ".button." + identifier());
        }

        @Override
        public JoystickRenderer renderer() {
            return null;
        }
    }

    private record HatMapping(int hatId, String identifier, String typeId, @Nullable EmulatedAxis emulatedAxis) implements Hat {
        @Override
        public JoystickState.HatState getHatState(JoystickData data) {
            if (emulatedAxis() != null) {
                var axis = emulatedAxis();
                var axisValue = data.axes()[axis.axisId()];
                return emulatedAxis().states().get(axisValue);
            }

            return data.hats()[hatId()];
        }

        @Override
        public Component name() {
            return Component.translatable("controlify.joystick_mapping." + typeId() + ".hat." + identifier());
        }

        @Override
        public JoystickRenderer renderer(JoystickState.HatState state) {
            return null;
        }

        private record EmulatedAxis(int axisId, Map<Float, JoystickState.HatState> states) {
        }
    }
}
