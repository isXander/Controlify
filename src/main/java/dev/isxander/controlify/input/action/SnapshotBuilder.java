package dev.isxander.controlify.input.action;

import java.util.IdentityHashMap;
import java.util.Map;

class SnapshotBuilder {
    private static final class Acc {
        private boolean pulse = false;
        private boolean latch = false;
        private float continuous = Float.NaN;
    }

    private final Map<ActionId<?>, Acc> map = new IdentityHashMap<>();

    private Acc acc(ActionId<?> id) {
        return map.computeIfAbsent(id, __ -> new Acc());
    }

    private final class A implements Accumulator {
        private final ActionId<?> id;
        private A(ActionId<?> id) {
            this.id = id;
        }

        @Override
        public void firePulse() {
            acc(id).pulse = true;
        }

        @Override
        public void setLatch(boolean active) {
            acc(id).latch = active;
        }

        @Override
        public void toggleLatch() {
            this.setLatch(!acc(id).latch);
        }

        @Override
        public void setAxis(float value) {
            acc(id).continuous = value;
        }
    }

    public Accumulator accumulateFor(ActionId<?> id) {
        return new A(id);
    }

    public ActionSnapshot build(long timeNanos) {
        final var frozen = new IdentityHashMap<>(this.map);
        return new ActionSnapshot() {
            @Override
            public boolean pulse(ActionId<Channel.Pulse> id) {
                var a = frozen.get(id);
                return a != null && a.pulse;
            }

            @Override
            public boolean latch(ActionId<Channel.Latch> id) {
                var a = frozen.get(id);
                return a != null && a.latch;
            }

            @Override
            public float continuous(ActionId<Channel.Continuous> id) {
                var a = frozen.get(id);
                return a != null ? a.continuous : Float.NaN;
            }

            @Override
            public long timeNanos() {
                return timeNanos;
            }
        };
    }
}
