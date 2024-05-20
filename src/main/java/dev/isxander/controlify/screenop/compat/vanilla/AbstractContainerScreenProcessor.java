package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.dualsense.HapticEffects;
import dev.isxander.controlify.gui.guide.ContainerGuideCtx;
import dev.isxander.controlify.gui.guide.GuideAction;
import dev.isxander.controlify.gui.guide.GuideActionRenderer;
import dev.isxander.controlify.gui.layout.AnchorPoint;
import dev.isxander.controlify.gui.layout.ColumnLayoutComponent;
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
    private PositionedComponent<ColumnLayoutComponent<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>>> leftLayout;
    private PositionedComponent<ColumnLayoutComponent<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>>> rightLayout;

    private final Supplier<Slot> hoveredSlot;
    private final ClickSlotFunction clickSlotFunction;

    public AbstractContainerScreenProcessor(T screen, Supplier<Slot> hoveredSlot, ClickSlotFunction clickSlotFunction) {
        super(screen);
        this.hoveredSlot = hoveredSlot;
        this.clickSlotFunction = clickSlotFunction;
    }

    @Override
    protected void handleScreenVMouse(ControllerEntity controller, VirtualMouseHandler vmouse) {
        var accessor = (AbstractContainerScreenAccessor) screen;
        ContainerGuideCtx ctx = new ContainerGuideCtx(hoveredSlot.get(), screen.getMenu().getCarried(), accessor.invokeHasClickedOutside(vmouse.getCurrentX(1f), vmouse.getCurrentY(1f), accessor.getLeftPos(), accessor.getTopPos(), 0));

        Slot hoveredSlot = this.hoveredSlot.get();
        if (!screen.getMenu().getCarried().isEmpty()) {
            if (ControlifyBindings.DROP_INVENTORY.on(controller).justPressed()) {
                clickSlotFunction.clickSlot(null, -999, 0, ClickType.PICKUP);
                hapticNavigate();
            }
        }
        if (hoveredSlot != null) {
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

        if (leftLayout != null && rightLayout != null) {
            for (var row : leftLayout.getComponent().getChildComponents()) {
                for (var element : row.getChildComponents()) {
                    element.updateName(ctx);
                }
            }
            for (var row : rightLayout.getComponent().getChildComponents()) {
                for (var element : row.getChildComponents()) {
                    element.updateName(ctx);
                }
            }

            leftLayout.updatePosition(screen.width, screen.height);
            rightLayout.updatePosition(screen.width, screen.height);
        }
    }

    @Override
    public void onWidgetRebuild() {
        ControllerEntity controller = ControlifyApi.get().getCurrentController()
                .filter(c -> c.input().isPresent()).orElse(null);
        if (controller == null) {
            return;
        }

        leftLayout = new PositionedComponent<>(
                ColumnLayoutComponent.<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>>builder()
                        .spacing(2)
                        .elementPosition(ColumnLayoutComponent.ElementPosition.LEFT)
                        .colPadding(2)
                        .element(RowLayoutComponent.<GuideActionRenderer<ContainerGuideCtx>>builder()
                                .spacing(5)
                                .rowPadding(0)
                                .elementPosition(RowLayoutComponent.ElementPosition.MIDDLE)
                                .element(new GuideActionRenderer<>(
                                        new GuideAction<>(ControlifyBindings.INV_SELECT.on(controller), ctx -> {
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
                                        new GuideAction<>(ControlifyBindings.GUI_BACK.on(controller), ctx -> {
                                            return Optional.of(Component.translatable("controlify.guide.container.exit"));
                                        }),
                                        false, false
                                ))
                                .build())
                        .build(),
                AnchorPoint.BOTTOM_LEFT,
                0, 0,
                AnchorPoint.BOTTOM_LEFT
        );

        rightLayout = new PositionedComponent<>(
                ColumnLayoutComponent.<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>>builder()
                        .spacing(2)
                        .elementPosition(ColumnLayoutComponent.ElementPosition.RIGHT)
                        .colPadding(2)
                        .element(RowLayoutComponent.<GuideActionRenderer<ContainerGuideCtx>>builder()
                                .spacing(5)
                                .rowPadding(0)
                                .elementPosition(RowLayoutComponent.ElementPosition.MIDDLE)
                                .element(new GuideActionRenderer<>(
                                        new GuideAction<>(ControlifyBindings.DROP_INVENTORY.on(controller), ctx -> {
                                            if (!ctx.holdingItem().isEmpty())
                                                return Optional.of(Component.translatable("controlify.guide.container.drop"));
                                            return Optional.empty();
                                        }),
                                        true, false
                                ))
                                .build())
                        .element(RowLayoutComponent.<GuideActionRenderer<ContainerGuideCtx>>builder()
                                .spacing(5)
                                .rowPadding(0)
                                .elementPosition(RowLayoutComponent.ElementPosition.MIDDLE)
                                .element(new GuideActionRenderer<>(
                                        new GuideAction<>(ControlifyBindings.INV_TAKE_HALF.on(controller), ctx -> {
                                            if (ctx.hoveredSlot() != null && ctx.hoveredSlot().getItem().getCount() > 1 && ctx.holdingItem().isEmpty())
                                                return Optional.of(Component.translatable("controlify.guide.container.take_half"));
                                            if (ctx.hoveredSlot() != null && !ctx.holdingItem().isEmpty() && ctx.hoveredSlot().mayPlace(ctx.holdingItem()))
                                                return Optional.of(Component.translatable("controlify.guide.container.take_one"));
                                            return Optional.empty();
                                        }),
                                        true, false
                                ))
                                .element(new GuideActionRenderer<>(
                                        new GuideAction<>(ControlifyBindings.INV_QUICK_MOVE.on(controller), ctx -> {
                                            if (ctx.hoveredSlot() != null && ctx.hoveredSlot().hasItem() && ctx.holdingItem().isEmpty())
                                                return Optional.of(Component.translatable("controlify.guide.container.quick_move"));
                                            return Optional.empty();
                                        }),
                                        true, false
                                ))
                                .build())
                        .build(),
                AnchorPoint.BOTTOM_RIGHT,
                0, 0,
                AnchorPoint.BOTTOM_RIGHT
        );

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
