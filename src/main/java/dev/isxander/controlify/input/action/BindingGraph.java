package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.action.gesture.Accumulator;
import dev.isxander.controlify.input.pipeline.EventBuffer;
import dev.isxander.controlify.input.pipeline.EventSink;
import dev.isxander.controlify.input.pipeline.SimpleEventBuffer;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BindingGraph implements EventSink<Signal> {

    // Context ID -> Input ID -> Set of CompiledBindings
    // the same compiled binding can be registered for multiple contexts and inputs
    // this allows for more efficient lookup when processing signals
    // and avoids the need to iterate over all bindings for each signal
    private final Map<ResourceLocation, Map<ResourceLocation, Set<CompiledBinding>>> byCtxByInput = new HashMap<>();
    private final Set<CompiledBinding> allBindings = new TreeSet<>();

    private final SimpleEventBuffer<Signal> signalBuffer = new SimpleEventBuffer<>();

    private ContextStack contextStack = ContextStack.NONE;

    public void addBinding(CompiledBinding binding) {
        for (ResourceLocation contextId : binding.contexts()) {
            var byInputMap = this.byCtxByInput
                    .computeIfAbsent(contextId, k -> new HashMap<>());
            for (ResourceLocation inputId : binding.gesture().monitoredInputs()) {
                byInputMap
                        .computeIfAbsent(inputId, k -> new TreeSet<>())
                        .add(binding);
            }
        }
        this.allBindings.add(binding);
    }

    @Override
    public void accept(Signal event) {
        this.signalBuffer.accept(event);

        if (event instanceof Signal.Tick(long timeNanos)) {
            this.createSnapshot(this.signalBuffer, timeNanos);
        }
    }

    private ActionSnapshot createSnapshot(EventBuffer<Signal> signalDrain, long timeNanos) {
        var sb = new SnapshotBuilder();
        var wrappedAcc = new GateProbeAcc();

        Signal signal;
        while ((signal = signalDrain.poll()) != null) {
            boolean consumed = false;

            switch (signal) {
                case Signal.Tick tickSig -> {
                    // if the signal is a tick, it does not have an associated input,
                    // so we can directly process all
                    for (CompiledBinding binding : this.allBindings) {
                        // even if the signal has been consumed, all gestures still need to receive it
                        // so they don't become desynchronized. instead, wrap the accumulator
                        // so prevent any further writes if the signal has already been consumed
                        wrappedAcc.retarget(!consumed ? sb.accumulateFor(binding.id()) : null);

                        // allow the binding's gesture to process the signal
                        binding.gesture().onSignal(tickSig, wrappedAcc);

                        // if the gesture wrote to the accumulator,
                        // we mark the signal as consumed
                        consumed |= wrappedAcc.wrote();
                    }
                }

                case Signal.InputSignal inputSig -> {
                    ResourceLocation inputId = inputSig.input();
                    Set<CompiledBinding> seenBindings = Collections.newSetFromMap(new IdentityHashMap<>());

                    // loop through each priority-ordered context so we consume the signals
                    // as according to which context is most important
                    for (Context context : this.contextStack.ordered()) {
                        Map<ResourceLocation, Set<CompiledBinding>> byInputMap = this.byCtxByInput.get(context.id());
                        if (byInputMap == null) continue;

                        Set<CompiledBinding> bindings = byInputMap.get(inputId);
                        if (bindings == null) continue;

                        for (CompiledBinding binding : bindings) {
                            // since the same binding can be registered for multiple contexts,
                            // we need to ensure a binding processes a signal only once
                            // this is done by checking if the binding has already been seen
                            // if it has, we skip it
                            if (seenBindings.contains(binding)) continue;
                            seenBindings.add(binding);

                            // even if the signal has been consumed, all gestures still need to receive it
                            // so they don't become desynchronized. instead, wrap the accumulator
                            // so prevent any further writes if the signal has already been consumed
                            wrappedAcc.retarget(!consumed ? sb.accumulateFor(binding.id()) : null);

                            // allow the binding's gesture to process the signal
                            binding.gesture().onSignal(inputSig, wrappedAcc);

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

        return sb.build(timeNanos);
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
