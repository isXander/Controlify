package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.gui.guide.ContainerGuideCtx;
import dev.isxander.controlify.gui.guide.GuideAction;
import dev.isxander.controlify.gui.guide.GuideActionRenderer;
import dev.isxander.controlify.gui.layout.AnchorPoint;
import dev.isxander.controlify.gui.layout.PositionedComponent;
import dev.isxander.controlify.gui.layout.RowLayoutComponent;
import dev.isxander.controlify.mixins.feature.guide.screen.AbstractContainerScreenAccessor;
import dev.isxander.controlify.mixins.feature.screenop.ScreenAccessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AbstractContainerScreenProcessor<T extends AbstractContainerScreen<?>> extends ScreenProcessor<T> {
    private PositionedComponent<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>> leftLayout;
    private PositionedComponent<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>> rightLayout;

    private final Supplier<Slot> hoveredSlot;
    private final ClickSlotFunction clickSlotFunction;

    public AbstractContainerScreenProcessor(T screen, Supplier<Slot> hoveredSlot, ClickSlotFunction clickSlotFunction) {
        super(screen);
        this.hoveredSlot = hoveredSlot;
        this.clickSlotFunction = clickSlotFunction;
    }

    @Override
    protected void handleScreenVMouse(Controller<?, ?> controller, VirtualMouseHandler vmouse) {
        if (controller.bindings().DROP.justPressed()) {
            Slot slot = hoveredSlot.get();
            if (slot != null && slot.hasItem()) {
                clickSlotFunction.clickSlot(slot, slot.index, 0, ClickType.THROW);
            }
        }

        if (leftLayout != null && rightLayout != null) {
            var accessor = (AbstractContainerScreenAccessor) screen;

            ContainerGuideCtx ctx = new ContainerGuideCtx(hoveredSlot.get(), screen.getMenu().getCarried(), accessor.invokeHasClickedOutside(vmouse.getCurrentX(1f), vmouse.getCurrentY(1f), accessor.getLeftPos(), accessor.getTopPos(), 0));

            for (var element : leftLayout.getComponent().getChildComponents()) {
                element.updateName(ctx);
            }
            for (var element : rightLayout.getComponent().getChildComponents()) {
                element.updateName(ctx);
            }

            leftLayout.updatePosition();
            rightLayout.updatePosition();
        }
    }

    @Override
    public void onWidgetRebuild() {
        ControllerBindings<?> bindings = ControlifyApi.get().getCurrentController()
                .map(Controller::bindings)
                .orElse(null);

        if (bindings == null) {
            return;
        }

        leftLayout = new PositionedComponent<>(
                RowLayoutComponent.<GuideActionRenderer<ContainerGuideCtx>>builder()
                        .spacing(5)
                        .rowPadding(0)
                        .elementPosition(RowLayoutComponent.ElementPosition.MIDDLE)
                        .element(new GuideActionRenderer<>(
                                new GuideAction<>(bindings.VMOUSE_LCLICK, ctx -> {
                                    if (!ctx.holdingItem().isEmpty())
                                        if (ctx.hoveredSlot() != null && ctx.hoveredSlot().hasItem())
                                            if (ctx.hoveredSlot().mayPlace(ctx.holdingItem()))
                                                if (ctx.holdingItem().getCount() > 1)
                                                    return Optional.of(Component.translatable("controlify.guide.container.place_all"));
                                                else
                                                    return Optional.of(Component.translatable("controlify.guide.container.place_one"));
                                            else
                                                return Optional.of(Component.translatable("controlify.guide.container.swap"));
                                        else if (ctx.cursorOutsideContainer())
                                            return Optional.of(Component.translatable("controlify.guide.container.drop"));
                                    if (ctx.hoveredSlot() != null && ctx.hoveredSlot().hasItem())
                                        return Optional.of(Component.translatable("controlify.guide.container.take"));
                                    return Optional.empty();
                                }),
                                false, false
                        ))
                        .element(new GuideActionRenderer<>(
                                new GuideAction<>(bindings.GUI_BACK, ctx -> {
                                    return Optional.of(Component.translatable("controlify.guide.container.exit"));
                                }),
                                false, false
                        ))
                        .build(),
                AnchorPoint.BOTTOM_LEFT,
                0, 0,
                AnchorPoint.BOTTOM_LEFT
        );

        rightLayout = new PositionedComponent<>(
                RowLayoutComponent.<GuideActionRenderer<ContainerGuideCtx>>builder()
                        .spacing(5)
                        .rowPadding(0)
                        .elementPosition(RowLayoutComponent.ElementPosition.MIDDLE)
                        .element(new GuideActionRenderer<>(
                                new GuideAction<>(bindings.VMOUSE_RCLICK, ctx -> {
                                    if (ctx.hoveredSlot() != null && ctx.hoveredSlot().getItem().getCount() > 1 && ctx.holdingItem().isEmpty())
                                        return Optional.of(Component.translatable("controlify.guide.container.take_half"));
                                    if (ctx.hoveredSlot() != null && !ctx.holdingItem().isEmpty() && ctx.hoveredSlot().mayPlace(ctx.holdingItem()))
                                        return Optional.of(Component.translatable("controlify.guide.container.take_one"));
                                    return Optional.empty();
                                }),
                                true, false
                        ))
                        .element(new GuideActionRenderer<>(
                                new GuideAction<>(bindings.VMOUSE_SHIFT_CLICK, ctx -> {
                                    return Optional.of(Component.translatable("controlify.guide.container.quick_move"));
                                }),
                                true, false
                        ))
                        .build(),
                AnchorPoint.BOTTOM_RIGHT,
                0, 0,
                AnchorPoint.BOTTOM_RIGHT
        );

        if (ControlifyApi.get().currentInputMode() == InputMode.CONTROLLER) {
            setRenderGuide(true);
        }
    }

    @Override
    public void onInputModeChanged(InputMode mode) {
        setRenderGuide(mode == InputMode.CONTROLLER);
    }

    private void setRenderGuide(boolean render) {
        List<Renderable> renderables = ((ScreenAccessor) screen).getRenderables();

        if (leftLayout == null || rightLayout == null)
            return;

        if (render) {
            if (!renderables.contains(leftLayout))
                renderables.add(leftLayout);
            if (!renderables.contains(rightLayout))
                renderables.add(rightLayout);
        } else {
            renderables.remove(leftLayout);
            renderables.remove(rightLayout);
        }
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
