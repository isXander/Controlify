package dev.isxander.controlify.bindings.v2;

public class SimpleAnalogueOutput implements AnalogueOutput {
    private final StateAccess stateAccess;
    private final int history;

    public SimpleAnalogueOutput(InputBinding binding, int history) {
        this.stateAccess = binding.createStateAccess(history + 1);
        this.history = history;
    }

    @Override
    public float get() {
        return stateAccess.analogue(history);
    }
}
