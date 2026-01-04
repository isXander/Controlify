package dev.isxander.controlify.api.v1.bindings;

public interface BuiltinBindings {
    InputBindingSupplier walkForward();
    InputBindingSupplier walkBackward();
    InputBindingSupplier walkLeft();
    InputBindingSupplier walkRight();

    InputBindingSupplier lookUp();
    InputBindingSupplier lookDown();
    InputBindingSupplier lookLeft();
    InputBindingSupplier lookRight();

    InputBindingSupplier jump();
    InputBindingSupplier sprint();
    InputBindingSupplier sneak();

    InputBindingSupplier attack();
    InputBindingSupplier use();

    InputBindingSupplier dropIngame();
    InputBindingSupplier dropStackIngame();

    InputBindingSupplier pause();

    InputBindingSupplier changePerspective();
    InputBindingSupplier swapHands();

    InputBindingSupplier nextHotbarSlot();
    InputBindingSupplier previousHotbarSlot();

    InputBindingSupplier openInventory();

    InputBindingSupplier containerSelect();
    InputBindingSupplier containerQuickMove();
    InputBindingSupplier containerTakeHalf();
    InputBindingSupplier containerDrop();

    InputBindingSupplier pickBlock();
    InputBindingSupplier pickBlockNbt();

    InputBindingSupplier openChat();
    InputBindingSupplier toggleHudVisibility();
    InputBindingSupplier playerList();
    InputBindingSupplier takeScreenshot();

    InputBindingSupplier guiPress();
    InputBindingSupplier guiBack();
    InputBindingSupplier guiNextTab();
    InputBindingSupplier guiPreviousTab();
    InputBindingSupplier guiAbstract1();
    InputBindingSupplier guiAbstract2();
    InputBindingSupplier guiAbstract3();
    InputBindingSupplier guiNavigateUp();
    InputBindingSupplier guiNavigateDown();
    InputBindingSupplier guiNavigateLeft();
    InputBindingSupplier guiNavigateRight();
    InputBindingSupplier guiSecondaryNavigateUp();
    InputBindingSupplier guiSecondaryNavigateDown();
    InputBindingSupplier guiSecondaryNavigateLeft();
    InputBindingSupplier guiSecondaryNavigateRight();

}
