package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.contextual.ContainerContext;
import dev.isxander.controlify.api.contextual.GuideInstance;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.SnapPoint;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.contextual.ContextualDomains;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.gui.guide.GuideRenderer;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.mixins.feature.guide.screen.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/// Inventory interaction, slot snap targets and the contextual inventory guide.
/// Recipe books and creative tabs add independent behaviours to this one.
public final class ContainerScreenBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends AbstractContainerScreen<?>> context;
	private final AbstractContainerScreen<?> screen;
	private final Access access;
	private final AbstractContainerScreenAccessor geometry;
	private final GuideInstance<ContainerContext> guide;
	private final GuideRenderer.Renderable guideRenderer;
	private @Nullable Slot previousSlot;

	public ContainerScreenBehaviour(ScreenContext<? extends AbstractContainerScreen<?>> context) {
		this.context = context;
		this.screen = context.screen();
		this.access = (Access) screen;
		this.geometry = (AbstractContainerScreenAccessor) screen;
		this.guide = ContextualDomains.INSTANCE.container().createGuideInstance(Minecraft.getInstance().font);
		this.guideRenderer = (GuideRenderer.Renderable) guide.renderable(true, false);
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (!context.isVirtualMouseEnabled()) {
			next.run();
			return;
		}
		var slot = geometry.controlify$getHoveredSlot();
		if (slot != null) {
			if (slot.hasItem() && access.controlify$handleItemSlotActions((ControllerImpl) controller)) {
				return;
			}
			if (ControlifyActions.INV_SELECT.on(controller).pulse().consumeAllPulses()) {
				access.controlify$clickSlot(slot, slot.index, 0, ContainerInput.PICKUP);
				context.playNavigationHaptic(controller);
			}
			while (MinecraftUtil.getScreen() == screen && ControlifyActions.INV_QUICK_MOVE.on(controller).pulse().consumePulse()) {
				access.controlify$clickSlot(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
				context.playNavigationHaptic(controller);
			}
			if (ControlifyActions.INV_TAKE_HALF.on(controller).pulse().consumeAllPulses()) {
				access.controlify$clickSlot(slot, slot.index, 1, ContainerInput.PICKUP);
				context.playNavigationHaptic(controller);
			}
		} else {
			context.virtualMouse().emulateClicks(controller);
		}
		if (MinecraftUtil.getScreen() == screen && !screen.getMenu().getCarried().isEmpty()
			&& ControlifyActions.DROP_INVENTORY.on(controller).pulse().consumeAllPulses()) {
			access.controlify$clickSlot(null, -999, 0, ContainerInput.PICKUP);
			context.playNavigationHaptic(controller);
		}
		next.run();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller controller, Runnable next) {
		next.run();
		if (MinecraftUtil.getScreen() != screen || !context.inputMode().isController()) {
			return;
		}
		var slot = geometry.controlify$getHoveredSlot();
		if (slot != previousSlot) {
			context.playNavigationHaptic(controller);
			previousSlot = slot;
		}
		var settings = ((ControllerImpl) controller).settings().generic.guide;
		if (!settings.showScreenGuides) {
			return;
		}
		var mouse = context.virtualMouse();
		guide.update(new ContainerContext(
				Minecraft.getInstance().player, slot, screen.getMenu().getCarried(),
				geometry.controlify$invokeHasClickedOutside(mouse.getCurrentX(1f), mouse.getCurrentY(1f),
						geometry.controlify$getLeftPos(), geometry.controlify$getTopPos()),
				(ControllerImpl) controller, settings.verbosity
		));
		guideRenderer.setGuiScale(settings.screenGuiScale);
		guideRenderer.extractRenderState(graphics, (int) mouse.getCurrentX(tickDelta), (int) mouse.getCurrentY(tickDelta), tickDelta);
	}

	@Override
	public void collectSnapPoints(Consumer<SnapPoint> output, Runnable next) {
		for (var slot : screen.getMenu().slots) {
			if (slot.isActive()) {
				output.accept(new SnapPoint(geometry.controlify$getLeftPos() + slot.x + 8,
						geometry.controlify$getTopPos() + slot.y + 8, 17));
			}
		}
		next.run();
	}

	public interface Access {
		void controlify$clickSlot(@Nullable Slot slot, int slotId, int button, ContainerInput input);

		boolean controlify$handleItemSlotActions(ControllerImpl controller);
	}
}
