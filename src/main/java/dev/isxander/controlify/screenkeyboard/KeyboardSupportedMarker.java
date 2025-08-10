package dev.isxander.controlify.screenkeyboard;

public interface KeyboardSupportedMarker {

    boolean controlify$isKeyboardSupported();

    static boolean isKeyboardSupported(Object obj) {
        return obj instanceof KeyboardSupportedMarker marker && marker.controlify$isKeyboardSupported();
    }

    static boolean setKeyboardSupported(Object obj, boolean supported) {
        if (obj instanceof Mutable mutable) {
            mutable.controlify$setKeyboardSupported(supported);
            return true;
        }
        return false;
    }

    interface Mutable extends KeyboardSupportedMarker {
        void controlify$setKeyboardSupported(boolean supported);
    }
}
