package dev.isxander.controlify.apiimpl.v1;

import dev.isxander.controlify.api.v1.bindings.BuiltinBindings;
import dev.isxander.controlify.api.v1.bindings.InputBindingSupplier;
import dev.isxander.controlify.bindings.ControlifyBindings;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Map;

class BuiltinBindingsImpl implements BuiltinBindings {
    static final BuiltinBindingsImpl INSTANCE = new BuiltinBindingsImpl();

    private BuiltinBindingsImpl() {
    }

    private final Map<dev.isxander.controlify.api.bind.InputBindingSupplier, InputBindingSupplier> CACHE =
            new Reference2ObjectOpenHashMap<>();

    private InputBindingSupplier wrap(dev.isxander.controlify.api.bind.InputBindingSupplier implSupplier) {
        return CACHE.computeIfAbsent(implSupplier, InputBindingSupplierImpl::new);
    }

    @Override
    public InputBindingSupplier walkForward() {
        return wrap(ControlifyBindings.WALK_FORWARD);
    }

    @Override
    public InputBindingSupplier walkBackward() {
        return wrap(ControlifyBindings.WALK_BACKWARD);
    }

    @Override
    public InputBindingSupplier walkLeft() {
        return wrap(ControlifyBindings.WALK_LEFT);
    }

    @Override
    public InputBindingSupplier walkRight() {
        return wrap(ControlifyBindings.WALK_RIGHT);
    }

    @Override
    public InputBindingSupplier lookUp() {
        return wrap(ControlifyBindings.LOOK_UP);
    }

    @Override
    public InputBindingSupplier lookDown() {
        return wrap(ControlifyBindings.LOOK_DOWN);
    }

    @Override
    public InputBindingSupplier lookLeft() {
        return wrap(ControlifyBindings.LOOK_LEFT);
    }

    @Override
    public InputBindingSupplier lookRight() {
        return wrap(ControlifyBindings.LOOK_RIGHT);
    }

    @Override
    public InputBindingSupplier jump() {
        return wrap(ControlifyBindings.JUMP);
    }

    @Override
    public InputBindingSupplier sprint() {
        return wrap(ControlifyBindings.SPRINT);
    }

    @Override
    public InputBindingSupplier sneak() {
        return wrap(ControlifyBindings.SNEAK);
    }

    @Override
    public InputBindingSupplier attack() {
        return wrap(ControlifyBindings.ATTACK);
    }

    @Override
    public InputBindingSupplier use() {
        return wrap(ControlifyBindings.USE);
    }

    @Override
    public InputBindingSupplier dropIngame() {
        return wrap(ControlifyBindings.DROP_INGAME);
    }

    @Override
    public InputBindingSupplier dropStackIngame() {
        return wrap(ControlifyBindings.DROP_STACK);
    }

    @Override
    public InputBindingSupplier pause() {
        return wrap(ControlifyBindings.PAUSE);
    }

    @Override
    public InputBindingSupplier changePerspective() {
        return wrap(ControlifyBindings.CHANGE_PERSPECTIVE);
    }

    @Override
    public InputBindingSupplier swapHands() {
        return wrap(ControlifyBindings.SWAP_HANDS);
    }

    @Override
    public InputBindingSupplier nextHotbarSlot() {
        return wrap(ControlifyBindings.NEXT_SLOT);
    }

    @Override
    public InputBindingSupplier previousHotbarSlot() {
        return wrap(ControlifyBindings.PREV_SLOT);
    }

    @Override
    public InputBindingSupplier openInventory() {
        return wrap(ControlifyBindings.INVENTORY);
    }

    @Override
    public InputBindingSupplier containerSelect() {
        return wrap(ControlifyBindings.INV_SELECT);
    }

    @Override
    public InputBindingSupplier containerQuickMove() {
        return wrap(ControlifyBindings.INV_QUICK_MOVE);
    }

    @Override
    public InputBindingSupplier containerTakeHalf() {
        return wrap(ControlifyBindings.INV_TAKE_HALF);
    }

    @Override
    public InputBindingSupplier containerDrop() {
        return wrap(ControlifyBindings.DROP_INVENTORY);
    }

    @Override
    public InputBindingSupplier pickBlock() {
        return wrap(ControlifyBindings.PICK_BLOCK);
    }

    @Override
    public InputBindingSupplier pickBlockNbt() {
        return wrap(ControlifyBindings.PICK_BLOCK_NBT);
    }

    @Override
    public InputBindingSupplier openChat() {
        return wrap(ControlifyBindings.OPEN_CHAT);
    }

    @Override
    public InputBindingSupplier toggleHudVisibility() {
        return wrap(ControlifyBindings.TOGGLE_HUD_VISIBILITY);
    }

    @Override
    public InputBindingSupplier playerList() {
        return wrap(ControlifyBindings.SHOW_PLAYER_LIST);
    }

    @Override
    public InputBindingSupplier takeScreenshot() {
        return wrap(ControlifyBindings.TAKE_SCREENSHOT);
    }

    @Override
    public InputBindingSupplier guiPress() {
        return wrap(ControlifyBindings.GUI_PRESS);
    }

    @Override
    public InputBindingSupplier guiBack() {
        return wrap(ControlifyBindings.GUI_BACK);
    }

    @Override
    public InputBindingSupplier guiNextTab() {
        return wrap(ControlifyBindings.GUI_NEXT_TAB);
    }

    @Override
    public InputBindingSupplier guiPreviousTab() {
        return wrap(ControlifyBindings.GUI_PREV_TAB);
    }

    @Override
    public InputBindingSupplier guiAbstract1() {
        return wrap(ControlifyBindings.GUI_ABSTRACT_ACTION_1);
    }

    @Override
    public InputBindingSupplier guiAbstract2() {
        return wrap(ControlifyBindings.GUI_ABSTRACT_ACTION_2);
    }

    @Override
    public InputBindingSupplier guiAbstract3() {
        return wrap(ControlifyBindings.GUI_ABSTRACT_ACTION_3);
    }

    @Override
    public InputBindingSupplier guiNavigateUp() {
        return wrap(ControlifyBindings.GUI_NAVI_UP);
    }

    @Override
    public InputBindingSupplier guiNavigateDown() {
        return wrap(ControlifyBindings.GUI_NAVI_DOWN);
    }

    @Override
    public InputBindingSupplier guiNavigateLeft() {
        return wrap(ControlifyBindings.GUI_NAVI_LEFT);
    }

    @Override
    public InputBindingSupplier guiNavigateRight() {
        return wrap(ControlifyBindings.GUI_NAVI_RIGHT);
    }

    @Override
    public InputBindingSupplier guiSecondaryNavigateUp() {
        return wrap(ControlifyBindings.GUI_SECONDARY_NAVI_UP);
    }

    @Override
    public InputBindingSupplier guiSecondaryNavigateDown() {
        return wrap(ControlifyBindings.GUI_SECONDARY_NAVI_DOWN);
    }

    @Override
    public InputBindingSupplier guiSecondaryNavigateLeft() {
        return wrap(ControlifyBindings.GUI_SECONDARY_NAVI_LEFT);
    }

    @Override
    public InputBindingSupplier guiSecondaryNavigateRight() {
        return wrap(ControlifyBindings.GUI_SECONDARY_NAVI_RIGHT);
    }
}
