package dev.isxander.controlify.input.action;

public final class ActionState implements Accumulator, Channel.Continuous, Channel.Latch, Channel.Pulse {
	private int pulses = 0;
	private boolean latchActive = false;
	private float continuousValue = 0;

	@Override
	public float getContinuous() {
		return this.continuousValue;
	}

	@Override
	public boolean isLatchActive() {
		return this.latchActive;
	}

	@Override
	public boolean consumePulse() {
		if (this.pulses > 0) {
			this.pulses--;
			return true;
		}
		return false;
	}

	@Override
	public void toggleLatch() {
		this.setLatch(!this.isLatchActive());
	}

	@Override
	public void setLatch(boolean active) {
		this.latchActive = active;
	}

	@Override
	public void firePulse() {
		this.pulses++;
	}

	@Override
	public void setContinuous(float value) {
		this.continuousValue = Math.clamp(value, 0f, 1f);
	}
}
