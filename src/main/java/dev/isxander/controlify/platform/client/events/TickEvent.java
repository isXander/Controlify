package dev.isxander.controlify.platform.client.events;

import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface TickEvent {
    void onTick(Minecraft minecraft);

    static TickEvent interval(int interval, TickEvent event) {
        return new IntervalTickEventHandler(event, interval);
    }

    class IntervalTickEventHandler implements TickEvent {
        private final TickEvent event;
        private int tickCount = 0;
        private final int interval;

        public IntervalTickEventHandler(TickEvent event, int interval) {
            this.event = event;
            this.interval = interval;
        }

        @Override
        public void onTick(Minecraft minecraft) {
            if (this.tickCount++ >= this.interval) {
                this.tickCount = 0;
                this.event.onTick(minecraft);
            }
        }
    }
}
