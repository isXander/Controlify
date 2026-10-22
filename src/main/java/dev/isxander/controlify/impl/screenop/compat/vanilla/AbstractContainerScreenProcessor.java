/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.contextual.ContainerContext;
import dev.isxander.controlify.api.contextual.GuideInstance;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.contextual.ContextualDomains;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.haptic.HapticEffects;
import dev.isxander.controlify.impl.gui.guide.GuideRenderer;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.virtualmouse.VirtualMouseBehaviour;
import dev.isxander.controlify.impl.virtualmouse.VirtualMouseHandler;
import dev.isxander.controlify.mixins.feature.guide.screen.AbstractContainerScreenAccessor;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AbstractContainerScreenProcessor<T extends AbstractContainerScreen<?>> extends ScreenProcessor<T> {

	private final GuideInstance<ContainerContext> guideInstance;
	private GuideRenderer.Renderable guideRenderable;

	private final Supplier<Slot> hoveredSlot;
	private final ClickSlotFunction clickSlotFunction;

	private final Predicate<ControllerImpl> doItemSlotActions;

	public AbstractContainerScreenProcessor(
			T screen,
			Supplier<Slot> hoveredSlot,
			ClickSlotFunction clickSlotFunction,
			Predicate<ControllerImpl> doItemSlotActions
	) {
		super(screen);
		this.hoveredSlot = hoveredSlot;
		this.clickSlotFunction = clickSlotFunction;
		this.doItemSlotActions = doItemSlotActions;
		this.guideInstance = ContextualDomains.INSTANCE.container().createGuideInstance(minecraft.font);
	}

	@Override
	protected void handleScreenVMouse(ControllerImpl controller, VirtualMouseHandler vmouse) {
		var accessor = (AbstractContainerScreenAccessor) screen;

		var ctx = new ContainerContext(
				minecraft.player,
				hoveredSlot.get(),
				screen.getMenu().getCarried(),
				accessor.controlify$invokeHasClickedOutside(
						vmouse.getCurrentX(1f),
						vmouse.getCurrentY(1f),
						accessor.controlify$getLeftPos(),
						accessor.controlify$getTopPos()
				),
				controller,
				controller.settings().generic.guide.verbosity
		);
		this.guideInstance.update(ctx);

		Slot hoveredSlot = this.hoveredSlot.get();
		if (hoveredSlot != null) {
			if (hoveredSlot.hasItem()) {
				if (doItemSlotActions.test(controller)) {
					return;
				}
			}

			if (ControlifyActions.INV_SELECT.on(controller).pulse().consumePulse()) {
				clickSlotFunction.clickSlot(hoveredSlot, hoveredSlot.index, 0, ContainerInput.PICKUP);
				hapticNavigate();
			}

			if (ControlifyActions.INV_QUICK_MOVE.on(controller).pulse().consumePulse()) {
				clickSlotFunction.clickSlot(hoveredSlot, hoveredSlot.index, 0, ContainerInput.QUICK_MOVE);
				hapticNavigate();
			}

			if (ControlifyActions.INV_TAKE_HALF.on(controller).pulse().consumePulse()) {
				clickSlotFunction.clickSlot(hoveredSlot, hoveredSlot.index, 1, ContainerInput.PICKUP);
				hapticNavigate();
			}

//            if (ControlifyActions.SWAP_HANDS.on(controller).pulse().consumePulse()) {
//                clickSlotFunction.clickSlot(hoveredSlot, hoveredSlot.index, 40, ContainerInput.SWAP);
//                hapticNavigate();
//            }
		} else {
			vmouse.handleCompatibilityBinds(controller);
		}

		if (!screen.getMenu().getCarried().isEmpty()) {
			if (ControlifyActions.DROP_INVENTORY.on(controller).pulse().consumePulse()) {
				clickSlotFunction.clickSlot(null, -999, 0, ContainerInput.PICKUP);
				hapticNavigate();
			}
		}
	}

	@Override
	public void onWidgetRebuild() {
		this.guideRenderable = (GuideRenderer.Renderable) guideInstance.renderable(true, false);

		if (Controlify.instance().currentInputMode().isController()) {
			setRenderGuide(true);
		}
	}

	@Override
	public void onInputModeChanged(InputMode mode) {
		setRenderGuide(mode.isController());
	}

	private void setRenderGuide(boolean render) {
		Controlify.instance().getCurrentController()
			.ifPresent(c -> this.guideRenderable.setGuiScale(c.settings().generic.guide.screenGuiScale));

		render &= Controlify.instance().getCurrentController().map(c -> c.settings().generic.guide.showScreenGuides).orElse(false);

		List<Renderable> renderables = ((ScreenAccessor) screen).controlify$getRenderables();

		if (render) {
			renderables.add(this.guideRenderable);
		} else if (this.guideRenderable != null) {
			renderables.remove(this.guideRenderable);
		}
	}

	public void onHoveredSlotChanged(Slot newSlot, Slot oldSlot) {
		if (Controlify.instance().currentInputMode().isController()) {
			hapticNavigate();
		}
	}

	private void hapticNavigate() {
		Controlify.instance().getCurrentController().flatMap(ControllerImpl::hdHaptics).ifPresent(hh -> {
			hh.playHaptic(HapticEffects.NAVIGATE);
		});
	}

	@Override
	public VirtualMouseBehaviour virtualMouseBehaviour() {
		return VirtualMouseBehaviour.CURSOR_SCROLL;
	}

	@FunctionalInterface
	public interface ClickSlotFunction {
		void clickSlot(
				Slot slot,
				int slotId,
				int button,
				ContainerInput containerInput
		);
	}
}
