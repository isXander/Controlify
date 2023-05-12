package dev.isxander.controlify.mixins.feature.guide.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.gui.guide.ContainerGuideCtx;
import dev.isxander.controlify.gui.guide.GuideAction;
import dev.isxander.controlify.gui.guide.GuideActionRenderer;
import dev.isxander.controlify.gui.layout.AnchorPoint;
import dev.isxander.controlify.gui.layout.ColumnLayoutComponent;
import dev.isxander.controlify.gui.layout.PositionedComponent;
import dev.isxander.controlify.gui.layout.RowLayoutComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

// TODO: Move out of mixin
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
    @Shadow @Nullable protected Slot hoveredSlot;
    @Shadow @Final protected T menu;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected abstract boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button);

    @Unique private PositionedComponent<ColumnLayoutComponent<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>>> leftLayout;
    @Unique private PositionedComponent<ColumnLayoutComponent<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>>> rightLayout;

    @Inject(method = "init", at = @At("RETURN"))
    private void initButtonGuide(CallbackInfo ci) {
        ControllerBindings<?> bindings = ControlifyApi.get().getCurrentController()
                .map(Controller::bindings)
                .orElse(null);

        if (bindings == null)
            return;

        leftLayout = new PositionedComponent<>(
                ColumnLayoutComponent.<RowLayoutComponent<GuideActionRenderer<ContainerGuideCtx>>>builder()
                        .spacing(2)
                        .colPadding(2, 2)
                        .elementPosition(ColumnLayoutComponent.ElementPosition.LEFT)
                        .element(RowLayoutComponent.<GuideActionRenderer<ContainerGuideCtx>>builder()
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
                                .elements(new GuideActionRenderer<>(
                                        new GuideAction<>(bindings.GUI_BACK, ctx -> {
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
                        .colPadding(2, 2)
                        .element(RowLayoutComponent.<GuideActionRenderer<ContainerGuideCtx>>builder()
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
                                .build())
                        .build(),
                AnchorPoint.BOTTOM_RIGHT,
                0, 0,
                AnchorPoint.BOTTOM_RIGHT
        );
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderButtonGuide(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ControlifyApi.get().getCurrentController().map(controller -> controller.config().showScreenGuide).orElse(false)
                || ControlifyApi.get().currentInputMode() != InputMode.CONTROLLER
        ) {
            return;
        }

        ContainerGuideCtx ctx = new ContainerGuideCtx(hoveredSlot, menu.getCarried(), hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, 0));

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

        leftLayout.updatePosition();
        rightLayout.updatePosition();

        leftLayout.render(matrices, delta);
        rightLayout.render(matrices, delta);
    }
}
