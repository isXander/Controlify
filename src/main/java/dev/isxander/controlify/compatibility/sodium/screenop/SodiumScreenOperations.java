//? if sodium {
package dev.isxander.controlify.compatibility.sodium.screenop;

import me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget;

public interface SodiumScreenOperations {
    void controlify$nextPage();

    void controlify$prevPage();

    FlatButtonWidget controlify$getApplyButton();

    FlatButtonWidget controlify$getCloseButton();

    FlatButtonWidget controlify$getUndoButton();
}
//?}
