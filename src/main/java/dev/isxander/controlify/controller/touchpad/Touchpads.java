package dev.isxander.controlify.controller.touchpad;

import org.joml.Vector2fc;

import java.util.ArrayList;
import java.util.List;

public record Touchpads(Touchpad[] touchpads) {
    public static final class Touchpad {
        private List<Finger> fingers;
        private List<Finger> prevFingers;

        private final int maxFingers;

        public Touchpad(int maxFingers) {
            this.fingers = new ArrayList<>();
            this.prevFingers = new ArrayList<>();
            this.maxFingers = maxFingers;
        }

        public List<Finger> fingersNow() {
            return fingers;
        }

        public List<Finger> fingersThen() {
            return prevFingers;
        }

        public void pushFingers(List<Finger> fingers) {
            prevFingers = this.fingers;
            this.fingers = List.copyOf(fingers);
        }

        public int maxFingers() {
            return maxFingers;
        }
    }

    /**
     * @param position position of finger on touchpad, in range [0, 1] where (0, 0) is top-left and (1, 1) is bottom-right
     * @param pressure pressure of finger on touchpad, in range [0, 1]. can be 0 if finger is resting on touchpad
     */
    public record Finger(int id, Vector2fc position, float pressure) {

    }
}
