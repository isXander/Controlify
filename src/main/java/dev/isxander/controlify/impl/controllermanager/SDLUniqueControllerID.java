package dev.isxander.controlify.impl.controllermanager;

import dev.isxander.sdl.SdlJoystickId;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SDLUniqueControllerID(@NotNull SdlJoystickId jid) implements UniqueControllerID {
	@Override
	public boolean equals(Object obj) {
		return obj instanceof SDLUniqueControllerID && ((SDLUniqueControllerID) obj).jid.equals(jid);
	}

	@Override
	public String toString() {
		return "SDL-" + jid.value();
	}

	@Override
	public int hashCode() {
		return Objects.hash(jid.value());
	}
}
