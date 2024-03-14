package dev.isxander.controlify.controller.input.mapping;

import dev.isxander.controlify.controller.input.ControllerState;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.ModifiableControllerState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public sealed interface MappingEntry {
    void apply(ControllerState oldState, ModifiableControllerState newState);

    MapType inputType();

    MapType outputType();

    sealed interface FromButton extends MappingEntry {
        record ToButton(ResourceLocation from, ResourceLocation to, boolean invert, MapType inputType, MapType outputType) implements FromButton {
            public ToButton(ResourceLocation from, ResourceLocation to, boolean invert) {
                this(from, to, invert, MapType.BUTTON, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                boolean fromState = oldState.isButtonDown(from);
                if (invert()) fromState = !fromState;
                newState.setButton(to, fromState);
            }
        }

        record ToAxis(ResourceLocation from, ResourceLocation to, float offState, float onState, MapType inputType, MapType outputType) implements FromButton {
            public ToAxis(ResourceLocation from, ResourceLocation to, float offState, float onState) {
                this(from, to, offState, onState, MapType.BUTTON, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setAxis(to, oldState.isButtonDown(from) ? onState : offState);
            }
        }

        record ToHat(ResourceLocation from, ResourceLocation to, HatState offState, HatState onState, MapType inputType, MapType outputType) implements FromButton {
            public ToHat(ResourceLocation from, ResourceLocation to, HatState offState, HatState onState) {
                this(from, to, offState, onState, MapType.BUTTON, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setHat(to, oldState.isButtonDown(from) ? onState : offState);
            }
        }
    }

    sealed interface FromAxis extends MappingEntry {
        record ToButton(ResourceLocation from, ResourceLocation to, float threshold, MapType inputType, MapType outputType) implements FromAxis {
            public ToButton(ResourceLocation from, ResourceLocation to, float threshold) {
                this(from, to, threshold, MapType.AXIS, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setButton(to, oldState.getAxisState(from) >= threshold);
            }
        }

        record ToAxis(ResourceLocation from, ResourceLocation to, float minIn, float minOut, float maxIn, float maxOut, MapType inputType, MapType outputType) implements FromAxis {
            public ToAxis(ResourceLocation from, ResourceLocation to, float minIn, float minOut, float maxIn, float maxOut) {
                this(from, to, minIn, minOut, maxIn, maxOut, MapType.AXIS, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                float oldVal = oldState.getAxisState(from);
                float newVal = Mth.lerp(Mth.inverseLerp(oldVal, minIn, maxIn), minOut, maxOut);
                newState.setAxis(to, newVal);
            }
        }

        record ToHat(ResourceLocation from, ResourceLocation to, float threshold, HatState targetState, MapType inputType, MapType outputType) implements FromAxis {
            public ToHat(ResourceLocation from, ResourceLocation to, float threshold, HatState targetState) {
                this(from, to, threshold, targetState, MapType.AXIS, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                float oldVal = oldState.getAxisState(from);
                newState.setHat(to, oldVal >= threshold ? targetState : HatState.CENTERED);
            }
        }
    }

    sealed interface FromHat extends MappingEntry {
        record ToButton(ResourceLocation from, ResourceLocation to, HatState targetState, MapType inputType, MapType outputType) implements FromHat {
            public ToButton(ResourceLocation from, ResourceLocation to, HatState targetState) {
                this(from, to, targetState, MapType.HAT, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setButton(to, oldState.getHatState(from) == targetState);
            }
        }

        record ToAxis(ResourceLocation from, ResourceLocation to, HatState targetState, float onState, float offState, MapType inputType, MapType outputType) implements FromHat {
            public ToAxis(ResourceLocation from, ResourceLocation to, HatState targetState, float onState, float offState) {
                this(from, to, targetState, onState, offState, MapType.HAT, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setAxis(to, oldState.getHatState(from) == targetState ? onState : offState);
            }
        }

        record ToHat(ResourceLocation from, ResourceLocation to, MapType inputType, MapType outputType) implements FromHat {
            public ToHat(ResourceLocation from, ResourceLocation to) {
                this(from, to, MapType.HAT, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setHat(to, oldState.getHatState(from));
            }
        }
    }

    sealed interface FromNothing extends MappingEntry {
        @Override
        default MapType inputType() {
            return MapType.NOTHING;
        }

        record ToButton(ResourceLocation to, boolean state) implements FromNothing {
            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setButton(to, state);
            }

            @Override
            public MapType outputType() {
                return MapType.BUTTON;
            }
        }

        record ToAxis(ResourceLocation to, float state) implements FromNothing {
            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setAxis(to, state);
            }

            @Override
            public MapType outputType() {
                return MapType.AXIS;
            }
        }

        record ToHat(ResourceLocation to) implements FromNothing {
            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setHat(to, HatState.CENTERED);
            }

            @Override
            public MapType outputType() {
                return MapType.HAT;
            }
        }
    }
}
