package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.pipeline.EventBuffer;
import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.SimpleEventBuffer;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ActionGraph implements EventSink<Signal> {

    // Context ID -> Input ID -> Set of ActionImpl
    // the same compiled binding can be registered for multiple contexts and inputs
    // this allows for more efficient lookup when processing signals
    // and avoids the need to iterate over all bindings for each signal
    private final Map<ResourceLocation, Map<ResourceLocation, Set<ActionImpl>>> byCtxByInput = new HashMap<>();
    private final Set<ActionImpl> allActions = new TreeSet<>(Comparator.comparingInt(a -> a.spec().priority()));

    private final SimpleEventBuffer<Signal> signalBuffer = new SimpleEventBuffer<>();

    private ContextStack contextStack = ContextStack.NONE;

    public void addAction(ActionImpl action) {
        this.allActions.add(action);
        for (ResourceLocation contextId : action.spec().contexts()) {
            var byInputMap = this.byCtxByInput
                    .computeIfAbsent(contextId, k -> new HashMap<>());
            for (ResourceLocation inputId : action.gesture().monitoredInputs()) {
                byInputMap
                        .computeIfAbsent(inputId, k -> new TreeSet<>(Comparator.comparingInt(a -> a.spec().priority())))
                        .add(action);
            }
        }
    }

    @Override
    public void accept(Signal event) {
        this.signalBuffer.accept(event);

        if (event instanceof Signal.Tick(long timeNanos)) {
            this.updateActionState(this.signalBuffer, timeNanos);
        }
    }

    private void updateActionState(EventBuffer<Signal> signalDrain, long timeNanos) {
        var wrappedAcc = new GateProbeAcc();

        // first, reset the state of actions, such as signal firing,
        // and unfreeze their state
        this.allActions.forEach(a -> a.state().next());

        Signal signal;
        while ((signal = signalDrain.poll()) != null) {
            boolean consumed = false;

            switch (signal) {
                case Signal.Tick tickSig -> {
                    // if the signal is a tick, it does not have an associated input,
                    // so we can directly process all
                    for (ActionImpl action : this.allActions) {
                        // even if the signal has been consumed, all gestures still need to receive it
                        // so they don't become desynchronized. instead, wrap the accumulator
                        // so prevent any further writes if the signal has already been consumed
                        wrappedAcc.retarget(!consumed ? action.state() : null);

                        // allow the binding's gesture to process the signal
                        action.gesture().onSignal(tickSig, wrappedAcc);

                        // if the gesture wrote to the accumulator,
                        // we mark the signal as consumed
                        consumed |= wrappedAcc.wrote();
                    }
                }

                case Signal.InputSignal inputSig -> {
                    ResourceLocation inputId = inputSig.input();
                    Set<ResourceLocation> seenBindings = Collections.newSetFromMap(new IdentityHashMap<>());

                    // loop through each priority-ordered context so we consume the signals
                    // as according to which context is most important
                    for (Context context : this.contextStack.ordered()) {
                        Map<ResourceLocation, Set<ActionImpl>> byInputMap = this.byCtxByInput.get(context.id());
                        if (byInputMap == null) continue;

                        Set<ActionImpl> actions = byInputMap.get(inputId);
                        if (actions == null) continue;

                        for (ActionImpl action : actions) {
                            // since the same binding can be registered for multiple contexts,
                            // we need to ensure a binding processes a signal only once
                            // this is done by checking if the binding has already been seen
                            // if it has, we skip it
                            if (seenBindings.contains(action.spec().id())) continue;
                            seenBindings.add(action.spec().id());

                            // even if the signal has been consumed, all gestures still need to receive it
                            // so they don't become desynchronized. instead, wrap the accumulator
                            // so prevent any further writes if the signal has already been consumed
                            wrappedAcc.retarget(!consumed ? action.state() : null);

                            // allow the binding's gesture to process the signal
                            action.gesture().onSignal(inputSig, wrappedAcc);

                            // if the gesture wrote to the accumulator,
                            // we mark the signal as consumed
                            if (context.consumePolicy() == ConsumePolicy.CONSUME) {
                                consumed |= wrappedAcc.wrote();
                            }
                        }
                    }
                }
            }
        }
        this.allActions.forEach(s -> s.state().commit());
    }

    public void setContextStack(ContextStack contextStack) {
        this.contextStack = contextStack;
    }

    private static class GateProbeAcc implements Accumulator {
        private Accumulator target = null;
        private boolean wrote = false;

        @Override
        public void firePulse() {
            if (this.target != null) {
                this.target.firePulse();
                this.wrote = true;
            }
        }

        @Override
        public void setLatch(boolean active) {
            if (this.target != null) {
                this.target.setLatch(active);
                this.wrote = true;
            }
        }

        @Override
        public void toggleLatch() {
            if (this.target != null) {
                this.target.toggleLatch();
                this.wrote = true;
            }
        }

        @Override
        public void setAxis(float value) {
            if (this.target != null) {
                this.target.setAxis(value);
                this.wrote = true;
            }
        }

        public void retarget(@Nullable Accumulator target) {
            this.target = target;
        }

        public boolean wrote() {
            return this.wrote;
        }
    }
}
