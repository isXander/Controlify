package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.guide.ContainerCtx;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.haptic.HapticEffects;
import dev.isxander.controlify.gui.guide.GuideDomains;
import dev.isxander.controlify.gui.guide.GuideRenderer;
import dev.isxander.controlify.mixins.feature.guide.screen.AbstractContainerScreenAccessor;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AbstractContainerScreenProcessor<T extends AbstractContainerScreen<?>> extends ScreenProcessor<T> {

    private final GuideRenderer.Renderable guideRenderable;

    private final Supplier<Slot> hoveredSlot;
    private final ClickSlotFunction clickSlotFunction;

    private final Predicate<ControllerEntity> doItemSlotActions;

    public AbstractContainerScreenProcessor(
            T screen,
            Supplier<Slot> hoveredSlot,
            ClickSlotFunction clickSlotFunction,
            Predicate<ControllerEntity> doItemSlotActions
    ) {
        super(screen);
        this.hoveredSlot = hoveredSlot;
        this.clickSlotFunction = clickSlotFunction;
        this.doItemSlotActions = doItemSlotActions;
        this.guideRenderable = new GuideRenderer.Renderable(
                GuideDomains.CONTAINER,
                minecraft,
                true,
                false
        );
    }

    @Override
    protected void handleScreenVMouse(ControllerEntity controller, VirtualMouseHandler vmouse) {
        var accessor = (AbstractContainerScreenAccessor) screen;

        var ctx = new ContainerCtx(hoveredSlot.get(), screen.getMenu().getCarried(), accessor.invokeHasClickedOutside(vmouse.getCurrentX(1f), vmouse.getCurrentY(1f), accessor.getLeftPos(), accessor.getTopPos() /*? if <1.21.9 >>*/ /*,0*/ ), controller, controller.genericConfig().config().guideVerbosity);
        GuideDomains.CONTAINER.updateGuides(ctx, minecraft.font);

        Slot hoveredSlot = this.hoveredSlot.get();
        if (hoveredSlot != null) {
            if (hoveredSlot.hasItem()) {
                if (doItemSlotActions.test(controller)) {
                    return;
                }
            }

            if (ControlifyBindings.INV_SELECT.on(controller).justPressed()) {
                clickSlotFunction.clickSlot(hoveredSlot, hoveredSlot.index, 0, ClickType.PICKUP);
                hapticNavigate();
            }

            if (ControlifyBindings.INV_QUICK_MOVE.on(controller).justPressed()) {
                clickSlotFunction.clickSlot(hoveredSlot, hoveredSlot.index, 0, ClickType.QUICK_MOVE);
                hapticNavigate();
            }

            if (ControlifyBindings.INV_TAKE_HALF.on(controller).justPressed()) {
                clickSlotFunction.clickSlot(hoveredSlot, hoveredSlot.index, 1, ClickType.PICKUP);
                hapticNavigate();
            }

//            if (ControlifyBindings.SWAP_HANDS.on(controller).justPressed()) {
//                clickSlotFunction.clickSlot(hoveredSlot, hoveredSlot.index, 40, ClickType.SWAP);
//                hapticNavigate();
//            }
        } else {
            vmouse.handleCompatibilityBinds(controller);
        }

        if (!screen.getMenu().getCarried().isEmpty()) {
            if (ControlifyBindings.DROP_INVENTORY.on(controller).justPressed()) {
                clickSlotFunction.clickSlot(null, -999, 0, ClickType.PICKUP);
                hapticNavigate();
            }
        }
    }

    @Override
    public void onWidgetRebuild() {
        if (ControlifyApi.get().currentInputMode().isController()) {
            setRenderGuide(true);
        }
    }

    @Override
    public void onInputModeChanged(InputMode mode) {
        setRenderGuide(mode.isController());
    }

    private void setRenderGuide(boolean render) {
        render &= ControlifyApi.get().getCurrentController().map(c -> c.genericConfig().config().showScreenGuides).orElse(false);

        List<Renderable> renderables = ((ScreenAccessor) screen).getRenderables();

        if (render) {
            renderables.add(guideRenderable);
        } else if (this.guideRenderable != null) {
            renderables.remove(this.guideRenderable);
        }
    }

    public void onHoveredSlotChanged(Slot newSlot, Slot oldSlot) {
        if (ControlifyApi.get().currentInputMode().isController()) {
            hapticNavigate();
        }
    }

    private void hapticNavigate() {
        ControlifyApi.get().getCurrentController().flatMap(ControllerEntity::hdHaptics).ifPresent(hh -> {
            hh.playHaptic(HapticEffects.NAVIGATE);
        });
    }

    @Override
    public VirtualMouseBehaviour virtualMouseBehaviour() {
        return VirtualMouseBehaviour.CURSOR_ONLY;
    }

    @FunctionalInterface
    public interface ClickSlotFunction {
        void clickSlot(Slot slot, int slotId, int button, ClickType clickType);
    }
}
