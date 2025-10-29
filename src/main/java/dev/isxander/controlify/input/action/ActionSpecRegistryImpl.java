package dev.isxander.controlify.input.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionSpecRegistryImpl implements ActionSpecRegistry {
    public static final ActionSpecRegistryImpl INSTANCE = new ActionSpecRegistryImpl();

    private final List<ActionSpec> registeredSpecs = new ArrayList<>();

    private boolean locked = false;

    @Override
    public void register(ActionSpec spec) {
        this.checkLocked(false);

        this.registeredSpecs.add(spec);
    }

    private void checkLocked(boolean required) {
        if (required) {
            if (!locked) {
                throw new IllegalStateException("Registry must be locked to perform this operation.");
            }
        } else {
            if (locked) {
                throw new IllegalStateException("Registry must be unlocked to perform this operation.");
            }
        }
    }

    public List<ActionSpec> getActionSpecs() {
        this.checkLocked(true);
        return Collections.unmodifiableList(this.registeredSpecs);
    }

    public void lock() {
        this.locked = true;
    }
}
