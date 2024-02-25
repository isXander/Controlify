package dev.isxander.controlify.controller.input.mapping;

import dev.isxander.controlify.controller.input.ControllerState;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.ModifiableControllerState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public sealed interface MappingEntry {
    void apply(ControllerState oldState, ModifiableControllerState newState);

    MapType outputType();

    sealed interface FromButton extends MappingEntry {
        record ToButton(ResourceLocation from, ResourceLocation to, boolean invert, MapType outputType) implements FromButton {
            public ToButton(ResourceLocation from, ResourceLocation to, boolean invert) {
                this(from, to, invert, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                boolean fromState = oldState.isButtonDown(from);
                if (invert()) fromState = !fromState;
                newState.setButton(to, fromState);
            }
        }

        record ToAxis(ResourceLocation from, ResourceLocation to, float offState, float onState, MapType outputType) implements FromButton {
            public ToAxis(ResourceLocation from, ResourceLocation to, float offState, float onState) {
                this(from, to, offState, onState, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setAxis(to, oldState.isButtonDown(from) ? onState : offState);
            }
        }

        record ToHat(ResourceLocation from, ResourceLocation to, HatState offState, HatState onState, MapType outputType) implements FromButton {
            public ToHat(ResourceLocation from, ResourceLocation to, HatState offState, HatState onState) {
                this(from, to, offState, onState, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setHat(to, oldState.isButtonDown(from) ? onState : offState);
            }
        }
    }

    sealed interface FromAxis extends MappingEntry {
        record ToButton(ResourceLocation from, ResourceLocation to, float threshold, MapType outputType) implements FromAxis {
            public ToButton(ResourceLocation from, ResourceLocation to, float threshold) {
                this(from, to, threshold, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setButton(to, oldState.getAxisState(from) >= threshold);
            }
        }

        record ToAxis(ResourceLocation from, ResourceLocation to, float minIn, float minOut, float maxIn, float maxOut, MapType outputType) implements FromAxis {
            public ToAxis(ResourceLocation from, ResourceLocation to, float minIn, float minOut, float maxIn, float maxOut) {
                this(from, to, minIn, minOut, maxIn, maxOut, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                float oldVal = oldState.getAxisState(from);
                float newVal = Mth.lerp(Mth.inverseLerp(oldVal, minIn, maxIn), minOut, maxOut);
                newState.setAxis(to, newVal);
            }
        }

        record ToHat(ResourceLocation from, ResourceLocation to, float threshold, HatState targetState, MapType outputType) implements FromAxis {
            public ToHat(ResourceLocation from, ResourceLocation to, float threshold, HatState targetState) {
                this(from, to, threshold, targetState, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                float oldVal = oldState.getAxisState(from);
                newState.setHat(to, oldVal >= threshold ? targetState : HatState.CENTERED);
            }
        }
    }

    sealed interface FromHat extends MappingEntry {
        record ToButton(ResourceLocation from, ResourceLocation to, HatState targetState, MapType outputType) implements FromHat {
            public ToButton(ResourceLocation from, ResourceLocation to, HatState targetState) {
                this(from, to, targetState, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setButton(to, oldState.getHatState(from) == targetState);
            }
        }

        record ToAxis(ResourceLocation from, ResourceLocation to, HatState targetState, float onState, float offState, MapType outputType) implements FromHat {
            public ToAxis(ResourceLocation from, ResourceLocation to, HatState targetState, float onState, float offState) {
                this(from, to, targetState, onState, offState, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setAxis(to, oldState.getHatState(from) == targetState ? onState : offState);
            }
        }

        record ToHat(ResourceLocation from, ResourceLocation to, MapType outputType) implements FromHat {
            public ToHat(ResourceLocation from, ResourceLocation to) {
                this(from, to, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setHat(to, oldState.getHatState(from));
            }
        }
    }

    sealed interface FromNothing extends MappingEntry {
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
