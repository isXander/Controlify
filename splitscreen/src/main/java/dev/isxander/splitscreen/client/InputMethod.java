package dev.isxander.splitscreen.client;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.ipc.utils.ExtraStreamCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public sealed interface InputMethod {
    static Controller controller(ControllerUID uid) {
        return new Controller(uid);
    }

    static KeyboardAndMouse keyboardAndMouse() {
        return KeyboardAndMouse.UNIT;
    }

    record KeyboardAndMouse() implements InputMethod {
        public static final KeyboardAndMouse UNIT = new KeyboardAndMouse();
        public static final StreamCodec<FriendlyByteBuf, KeyboardAndMouse> STREAM_CODEC =
                StreamCodec.unit(UNIT);

        @Override
        public boolean isKeyboardAndMouse() {
            return true;
        }

        @Override
        public boolean isController() {
            return false;
        }

        @Override
        public Optional<ControllerUID> getControllerUID() {
            return Optional.empty();
        }

        @Override
        public String asString() {
            return "kbm";
        }
    }

    record Controller(ControllerUID uid) implements InputMethod {
        public static final StreamCodec<FriendlyByteBuf, Controller> STREAM_CODEC =
                StreamCodec.composite(
                        ExtraStreamCodecs.CONTROLLER_UID,
                        Controller::uid,
                        Controller::new
                );

        @Override
        public boolean isKeyboardAndMouse() {
            return false;
        }

        @Override
        public boolean isController() {
            return true;
        }

        @Override
        public Optional<ControllerUID> getControllerUID() {
            return Optional.of(uid());
        }

        public Optional<ControllerEntity> findControllerEntity() {
            return Controlify.instance().getControllerManager().orElseThrow()
                    .getConnectedControllers().stream()
                    .filter(c -> c.uid().equals(uid))
                    .findAny();
        }

        @Override
        public String asString() {
            return "c" + uid.string();
        }
    }

    boolean isKeyboardAndMouse();
    boolean isController();

    Optional<ControllerUID> getControllerUID();

    String asString();

    static InputMethod fromString(String string) {
        if (string.startsWith("c")) {
            return new Controller(new ControllerUID(string.substring(1)));
        } else if (string.startsWith("kbm")) {
            return KeyboardAndMouse.UNIT;
        } else {
            throw new IllegalArgumentException("Could not parse '" + string + "' as an InputMethod.");
        }
    }

    StreamCodec<FriendlyByteBuf, InputMethod> STREAM_CODEC =
            ByteBufCodecs.optional(Controller.STREAM_CODEC)
                    .map(
                            opt -> opt.map(c -> (InputMethod) c).orElse(KeyboardAndMouse.UNIT),
                            method -> method instanceof Controller ? Optional.of((Controller) method) : Optional.empty()
                    );
}
