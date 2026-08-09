package dev.isxander.controlify.gametest.framework;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controllermanager.SDLControllerManager;
import dev.isxander.controlify.driver.sdl.SDLException;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.sdl.Sdl;
import dev.isxander.sdl.SdlGamepad;
import dev.isxander.sdl.SdlJoystickHandle;
import dev.isxander.sdl.SdlJoystickId;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;

@SuppressWarnings("UnstableApiUsage")
public class TestControllerContext<E> implements AutoCloseable {
	private final ClientGameTestContext context;
	private final Sdl sdl;
	private final SdlJoystickHandle joystickHandle;
	private final SdlJoystickId joystickId;
	private final ControllerInputDefinition inputs;
	private final TestControllerState<E> state;

	public TestControllerContext(
		ClientGameTestContext context,
		Sdl sdl,
		SdlJoystickHandle joystickHandle,
		SdlJoystickId joystickId,
		ControllerInputDefinition inputs,
		TestControllerState<E> state
	) {
		this.context = context;
		this.sdl = sdl;
		this.joystickHandle = joystickHandle;
		this.joystickId = joystickId;
		this.inputs = inputs;
		this.state = state;
	}

	public ControllerEntity getControllerEntity() {
		return Controlify.instance().getControllerManager().orElseThrow()
			.getConnectedControllers().stream()
			.filter(c -> c.info().ucid().equals(new SDLControllerManager.SDLUniqueControllerID(this.joystickId)))
			.findAny()
			.orElseThrow();
	}

	public void holdButton(int button) {
		this.setButton(button, true);
	}

	public void releaseButton(int button) {
		this.setButton(button, false);
	}

	public void tapButton(int button) {
		this.holdButton(button);
		this.context.waitTick();
		this.releaseButton(button);
		this.context.waitTick();
	}

	private void setButton(int button, boolean down) {
		this.sdl.joystick().SDL_SetJoystickVirtualButton(
			this.joystickHandle,
			this.inputs.joystickButton(button),
			down
		);
	}

	public void holdAxis(int axis, float state) {
		short stateShort = CUtil.mapFloatToShort(state);

		this.sdl.joystick().SDL_SetJoystickVirtualAxis(
			this.joystickHandle,
			this.inputs.joystickAxis(axis),
			stateShort
		);
	}

	public void releaseAxis(int axis) {
		this.holdAxis(axis, 0f);
	}

	public void tapAxis(int axis, float state) {
		this.holdAxis(axis, state);
		context.waitTick();
		this.releaseAxis(axis);
	}

	public void sendSensorData(int sensorType, float... data) {
		try (var arena = Arena.ofConfined()) {
			var memory = arena.allocateFrom(ValueLayout.JAVA_FLOAT, data);
			var floatBuffer = memory.asByteBuffer().asFloatBuffer();

			this.sdl.joystick().SDL_SendJoystickVirtualSensorData(
				this.joystickHandle,
				sensorType,
				0,
				floatBuffer
			);
		}
	}

	public void sendGyroData(float x, float y, float z) {
		this.sendSensorData(SdlGamepad.SDL_SENSOR_GYRO, x, y, z);
	}

	public TestControllerStateView<E> state() {
		return this.state;
	}

	@Override
	public void close() {
		this.sdl.joystick().SDL_CloseJoystick(this.joystickHandle);

		if (!this.sdl.joystick().SDL_DetachVirtualJoystick(joystickId)) {
			throw SDLException.useSDLError(this.sdl, "Failed to detach virtual joystick");
		}

		// wait for controlify event loop to realise its gone
		this.context.waitTick();
	}
}
