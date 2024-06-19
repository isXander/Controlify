package dev.isxander.controlify.controller.battery;

public sealed interface PowerState {
    default int percent() {
        return -1;
    }

    record Depleting(int percent) implements PowerState {}
    record Charging(int percent) implements PowerState {}
    record Full() implements PowerState {
        @Override
        public int percent() {
            return 100;
        }
    }
    record WiredOnly() implements PowerState {}
    record Unknown() implements PowerState {}
}
