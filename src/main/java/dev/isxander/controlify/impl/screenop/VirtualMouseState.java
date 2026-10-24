/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.*;

/// Shared cursor state and operations. Input interpretation, frame scheduling and
/// rendering are separate screen behaviours.
public final class VirtualMouseState implements VirtualMouse {
	private final ScreenContextImpl<?> context;
	private final Minecraft minecraft = Minecraft.getInstance();
	private VirtualMouseMode mode = VirtualMouseMode.DEFAULT;
	private boolean modeConfigured;
	private boolean active;
	private boolean enabled;
	private boolean suspended;
	private double targetX;
	private double targetY;
	private double currentX;
	private double currentY;
	private double scrollX;
	private double scrollY;
	private boolean leftClickDown;
	private boolean rightClickDown;
	private Set<SnapPoint> snapPoints = Set.of();
	private @Nullable SnapPoint lastSnappedPoint;
	private final Vector2d previousMoveImpulse = new Vector2d();

	VirtualMouseState(ScreenContextImpl<?> context) {
		this.context = context;
	}

	@Override
	public VirtualMouseMode mode() {
		return mode;
	}

	@Override
	public void setMode(VirtualMouseMode mode) {
		Objects.requireNonNull(mode);
		modeConfigured = true;
		if (this.mode != mode) {
			releaseClicks();
			if (!mode.scrolls()) {
				preventScrollingThisTick();
			}
		}
		this.mode = mode;
		synchronizeMode();
	}

	/// A built-in default must not replace a mode explicitly selected by a more
	/// specific factory, including an explicit selection of DEFAULT.
	void setDefaultMode(VirtualMouseMode mode) {
		if (!modeConfigured) {
			setMode(mode);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public boolean isRequired() {
		if (!Controlify.instance().currentInputMode().isController() || Controlify.instance().getCurrentController().isEmpty()) {
			return false;
		}
		return switch (mode) {
			case DISABLED -> false;
			case ENABLED, CURSOR_ONLY, CURSOR_SCROLL -> true;
			case DEFAULT -> Controlify.instance().config().getSettings().globalSettings().virtualMouseScreens
					.stream().anyMatch(type -> type.isInstance(context.screen()));
		};
	}

	public void activate() {
		active = true;
		suspended = false;
		initializePosition();
		synchronizeMode();
	}

	public void deactivate() {
		active = false;
		setEnabled(false);
		snapPoints = Set.of();
		lastSnappedPoint = null;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
		synchronizeMode();
	}

	public void synchronizeMode() {
		setEnabled(active && !suspended && isRequired());
	}

	private void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			initializePosition();
		} else {
			releaseClicks();
			scrollX = scrollY = 0;
			previousMoveImpulse.zero();
			targetX = currentX;
			targetY = currentY;
			lastSnappedPoint = null;
		}
	}

	private void initializePosition() {
		double x = minecraft.mouseHandler.xpos(), y = minecraft.mouseHandler.ypos();
		if (x == -50 && y == -50) {
			x = minecraft.getWindow().getScreenWidth() / 2.0;
			y = minecraft.getWindow().getScreenHeight() / 2.0;
		}
		currentX = targetX = x;
		currentY = targetY = y;
	}

	public Vector2d previousMoveImpulse() {
		return new Vector2d(previousMoveImpulse);
	}

	public void setMoveImpulse(Vector2d impulse) {
		previousMoveImpulse.set(impulse);
	}

	public void releaseUnlatchedClicks(Controller controller) {
		if (leftClickDown && !ControlifyActions.VMOUSE_LCLICK.on(controller).latch().isLatchActive()) {
			leftClickDown = false;
			simulateMousePress(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.RELEASE, 0);
		}
		if (rightClickDown && !ControlifyActions.VMOUSE_RCLICK.on(controller).latch().isLatchActive()) {
			rightClickDown = false;
			simulateMousePress(InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.RELEASE, 0);
		}
	}

	public void refreshSnapPoints() {
		snapPoints = context.collectSnapPoints();
	}

	public Set<SnapPoint> snapPoints() {
		return snapPoints;
	}

	public @Nullable SnapPoint lastSnappedPoint() {
		return lastSnappedPoint;
	}

	public double x() {
		return currentX;
	}

	public double y() {
		return currentY;
	}

	public void move(double x, double y) {
		targetX = Mth.clamp(targetX + x, 0, minecraft.getWindow().getWidth());
		targetY = Mth.clamp(targetY + y, 0, minecraft.getWindow().getHeight());
	}

	public void updatePosition(float delta) {
		double newX = Mth.lerp(Math.clamp(delta, 0, 1), currentX, targetX);
		double newY = Mth.lerp(Math.clamp(delta, 0, 1), currentY, targetY);
		if (newX != currentX || newY != currentY) {
			double relX = newX - currentX, relY = newY - currentY;
			currentX = newX;
			currentY = newY;
			((MouseHandlerAccessor) minecraft.mouseHandler).controlify$invokeOnMove(
					minecraft.getWindow().handle(), newX, newY /*? if >=26.3 {*/, relX, relY/*?}*/);
		}
	}

	public void addScroll(double y) {
		scrollY += y;
	}

	public void updateScroll(float delta) {
		double y = scrollY * Math.clamp(delta, 0, 1), x = scrollX * Math.clamp(delta, 0, 1);
		scrollY -= y;
		scrollX -= x;
		if (Math.abs(scrollY) < 0.01) {
			scrollY = 0;
		}
		if (Math.abs(scrollX) < 0.01) {
			scrollX = 0;
		}
		if (x != 0 || y != 0) {
			((MouseHandlerAccessor) minecraft.mouseHandler).controlify$invokeOnScroll(minecraft.getWindow().handle(), x, y);
		}
	}

	private void simulateMousePress(int button, int action, int modifiers) {
		((MouseHandlerAccessor) minecraft.mouseHandler).controlify$invokeOnButton(
				minecraft.getWindow().handle(), new MouseButtonInfo(button, modifiers), action);
	}

	public void releaseClicks() {
		boolean left = leftClickDown, right = rightClickDown;
		leftClickDown = rightClickDown = false;
		if (left) {
			simulateMousePress(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.RELEASE, 0);
		}
		if (right) {
			simulateMousePress(InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.RELEASE, 0);
		}
	}

	@Override
	public void emulateClicks(Controller controller) {
		if (!isEnabled() || !context.isCurrentScreen()) {
			return;
		}

		if (ControlifyActions.VMOUSE_LCLICK.on(controller).pulse().consumeAllPulses()) {
			leftClickDown = true;
			this.simulateMousePress(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.PRESS, 0);
		} else if (leftClickDown && !ControlifyActions.VMOUSE_LCLICK.on(controller).latch().isLatchActive()) {
			this.simulateMousePress(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.RELEASE, 0);
			leftClickDown = false;
		}

		if (!isEnabled() || !context.isCurrentScreen()) {
			return;
		}
		if (ControlifyActions.VMOUSE_RCLICK.on(controller).pulse().consumeAllPulses()) {
			rightClickDown = true;
			this.simulateMousePress(InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.PRESS, 0);
		} else if (rightClickDown && !ControlifyActions.VMOUSE_RCLICK.on(controller).latch().isLatchActive()) {
			this.simulateMousePress(InputConstants.MOUSE_BUTTON_RIGHT, InputConstants.RELEASE, 0);
			rightClickDown = false;
		}

		while (isEnabled() && context.isCurrentScreen() && ControlifyActions.VMOUSE_SHIFT_CLICK.on(controller).pulse().consumePulse()) {
			this.simulateMousePress(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.PRESS, InputConstants.MOD_SHIFT);
			this.simulateMousePress(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.RELEASE, InputConstants.MOD_SHIFT);
		}
	}

	public void snapToClosestPoint() {
		if (!enabled || !context.isCurrentScreen()) {
			return;
		}
		refreshSnapPoints();
		var window = minecraft.getWindow();
		var scaleFactor = new Vector2d((double) window.getGuiScaledWidth() / (double) window.getScreenWidth(), (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());
		var target = new Vector2d(targetX, targetY).mul(scaleFactor);

		if (lastSnappedPoint != null) {
			if (lastSnappedPoint.position().distanceSquared(new Vector2i(target, RoundingMode.FLOOR)) > (long) lastSnappedPoint.range() * lastSnappedPoint.range()) {
				lastSnappedPoint = null;
			}
		}

		var closestSnapPoint = snapPoints.stream()
				.filter(snapPoint -> !snapPoint.equals(lastSnappedPoint)) // don't snap to the point currently over snapped point
				.map(snapPoint -> new Pair<>(snapPoint, snapPoint.position().distanceSquared(new Vector2i(target, RoundingMode.FLOOR)))) // map with distance to current pos
				.filter(point -> point.getSecond() <= (long) point.getFirst().range() * point.getFirst().range()) // filter out of range options
				.min(Comparator.comparingLong(Pair::getSecond)) // find the closest point
				.orElse(new Pair<>(null, Long.MAX_VALUE)).getFirst(); // retrieve point

		if (closestSnapPoint != null) {
			snapToPoint(closestSnapPoint, scaleFactor);
		}
	}

	public void snapInDirection(ScreenDirection direction) {
		if (!enabled || !context.isCurrentScreen()) {
			return;
		}
		refreshSnapPoints();
		var window = minecraft.getWindow();
		var scaleFactor = new Vector2d((double) window.getGuiScaledWidth() / (double) window.getScreenWidth(), (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());
		var target = new Vector2d(targetX, targetY).mul(scaleFactor);

		Optional<SnapPoint> closestSnapPoint = findOrthogonalSnapPoint(lastSnappedPoint, direction, target, snapPoints)
				.or(() -> {
					Vector2d orthogonalTarget = new Vector2d(target);
					switch (direction) {
						case UP -> orthogonalTarget.y = window.getGuiScaledHeight();
						case DOWN -> orthogonalTarget.y = 0;
						case LEFT -> orthogonalTarget.x = window.getGuiScaledWidth();
						case RIGHT -> orthogonalTarget.x = 0;
					}

					return findOrthogonalSnapPoint(lastSnappedPoint, direction, orthogonalTarget, snapPoints);
				});

		closestSnapPoint.ifPresent(snapPoint -> {
			snapToPoint(snapPoint, scaleFactor);
		});
	}

	private static Optional<SnapPoint> findOrthogonalSnapPoint(SnapPoint from, ScreenDirection direction, Vector2d target, Collection<SnapPoint> snapPoints) {
		return snapPoints.stream()
				.filter(snapPoint -> !snapPoint.equals(from)) // don't snap to the point currently over snapped point
				.map(snapPoint -> new Pair<>(snapPoint, new Vector2d(snapPoint.position().x() - target.x(), snapPoint.position().y() - target.y()))) // map with distance to current pos
				// filter points that are not in the correct direction
				.filter(pair -> {
					Vector2d dist = pair.getSecond();

					double axis = direction.getAxis() == ScreenAxis.HORIZONTAL ? dist.x : dist.y;
					double positive = direction.isPositive() ? 1 : -1;

					return axis * positive > 0;
				})
				.filter(pair -> {
					SnapPoint snapPoint = pair.getFirst();
					Vector2d dist = pair.getSecond();

					// distance in the correct orthogonal direction
					double distance = Math.abs(direction.getAxis() == ScreenAxis.HORIZONTAL ? dist.x : dist.y);
					// distance in the incorrect orthogonal direction
					double deviation = Math.abs(direction.getAxis() == ScreenAxis.HORIZONTAL ? dist.y : dist.x);

					// punish deviation significantly
					pair.getSecond().set(distance, deviation * 4);

					// reject if the deviation is double the correct direction away
					return distance >= snapPoint.range() && (deviation < distance * 2);
				})
				// pick the closest point
				.min(Comparator.comparingDouble(pair -> {
					Vector2d distDev = pair.getSecond();
					// x = dist, y = deviation
					return distDev.x + distDev.y;
				}))
				.map(Pair::getFirst);
	}

	public void snapToPoint(SnapPoint snapPoint, Vector2dc scaleFactor) {
		lastSnappedPoint = snapPoint;

		double newX = snapPoint.position().x() / scaleFactor.x();
		double newY = snapPoint.position().y() / scaleFactor.y();
		double relX = newX - currentX;
		double relY = newY - currentY;

		var windowHandle = minecraft.getWindow().handle();
		((MouseHandlerAccessor) minecraft.mouseHandler).controlify$invokeOnMove(windowHandle, newX, newY /*? if >=26.3 {*/, relX, relY/*?}*/);

		currentX = targetX = newX;
		currentY = targetY = newY;
	}

	@Override
	public int getCurrentX(float deltaTime) {
		return (int) Mth.lerp(deltaTime, currentX, targetX);
	}

	@Override
	public int getCurrentY(float deltaTime) {
		return (int) Mth.lerp(deltaTime, currentY, targetY);
	}

	@Override
	public void preventScrollingThisTick() {
		scrollX = scrollY = 0;
	}
}
