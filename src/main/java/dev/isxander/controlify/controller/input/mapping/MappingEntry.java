package dev.isxander.controlify.controller.input.mapping;

import dev.isxander.controlify.controller.input.ControllerState;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.ModifiableControllerState;
import dev.isxander.controlify.utils.MthExt;
import net.minecraft.resources.Identifier;

public sealed interface MappingEntry {
    void apply(ControllerState oldState, ModifiableControllerState newState);

    MapType inputType();

    MapType outputType();

    sealed interface FromButton extends MappingEntry {
        record ToButton(Identifier from, Identifier to, boolean invert, MapType inputType, MapType outputType) implements FromButton {
            public ToButton(Identifier from, Identifier to, boolean invert) {
                this(from, to, invert, MapType.BUTTON, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                boolean fromState = oldState.isButtonDown(from);
                if (invert()) fromState = !fromState;
                newState.setButton(to, fromState);
            }
        }

        record ToAxis(Identifier from, Identifier to, float offState, float onState, MapType inputType, MapType outputType) implements FromButton {
            public ToAxis(Identifier from, Identifier to, float offState, float onState) {
                this(from, to, offState, onState, MapType.BUTTON, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setAxis(to, oldState.isButtonDown(from) ? onState : offState);
            }
        }

        record ToHat(Identifier from, Identifier to, HatState offState, HatState onState, MapType inputType, MapType outputType) implements FromButton {
            public ToHat(Identifier from, Identifier to, HatState offState, HatState onState) {
                this(from, to, offState, onState, MapType.BUTTON, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setHat(to, oldState.isButtonDown(from) ? onState : offState);
            }
        }
    }

    sealed interface FromAxis extends MappingEntry {
        record ToButton(Identifier from, Identifier to, float threshold, MapType inputType, MapType outputType) implements FromAxis {
            public ToButton(Identifier from, Identifier to, float threshold) {
                this(from, to, threshold, MapType.AXIS, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setButton(to, oldState.getAxisState(from) >= threshold);
            }
        }

        record ToAxis(Identifier from, Identifier to, float minIn, float minOut, float maxIn, float maxOut, MapType inputType, MapType outputType) implements FromAxis {
            public ToAxis(Identifier from, Identifier to, float minIn, float minOut, float maxIn, float maxOut) {
                this(from, to, minIn, minOut, maxIn, maxOut, MapType.AXIS, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                float oldVal = oldState.getAxisState(from);
                float newVal = MthExt.remap(oldVal, minIn, maxIn, minOut, maxOut);
                newState.setAxis(to, newVal);
            }
        }

        record ToHat(Identifier from, Identifier to, float threshold, HatState targetState, MapType inputType, MapType outputType) implements FromAxis {
            public ToHat(Identifier from, Identifier to, float threshold, HatState targetState) {
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
        record ToButton(Identifier from, Identifier to, HatState targetState, MapType inputType, MapType outputType) implements FromHat {
            public ToButton(Identifier from, Identifier to, HatState targetState) {
                this(from, to, targetState, MapType.HAT, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setButton(to, oldState.getHatState(from) == targetState);
            }
        }

        record ToAxis(Identifier from, Identifier to, HatState targetState, float onState, float offState, MapType inputType, MapType outputType) implements FromHat {
            public ToAxis(Identifier from, Identifier to, HatState targetState, float onState, float offState) {
                this(from, to, targetState, onState, offState, MapType.HAT, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setAxis(to, oldState.getHatState(from) == targetState ? onState : offState);
            }
        }

        record ToHat(Identifier from, Identifier to, MapType inputType, MapType outputType) implements FromHat {
            public ToHat(Identifier from, Identifier to) {
                this(from, to, MapType.HAT, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setHat(to, oldState.getHatState(from));
            }
        }
    }

    sealed interface FromNothing extends MappingEntry {
        record ToButton(Identifier to, boolean state, MapType inputType, MapType outputType) implements FromNothing {
            public ToButton(Identifier to, boolean state) {
                this(to, state, MapType.NOTHING, MapType.BUTTON);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setButton(to, state);
            }
        }

        record ToAxis(Identifier to, float state, MapType inputType, MapType outputType) implements FromNothing {
            public ToAxis(Identifier to, float state) {
                this(to, state, MapType.NOTHING, MapType.AXIS);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setAxis(to, state);
            }
        }

        record ToHat(Identifier to, MapType inputType, MapType outputType) implements FromNothing {
            public ToHat(Identifier to) {
                this(to, MapType.NOTHING, MapType.HAT);
            }

            @Override
            public void apply(ControllerState oldState, ModifiableControllerState newState) {
                newState.setHat(to, HatState.CENTERED);
            }
        }
    }
}
