package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.tab.ListHolderWidget;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(YACLScreen.CategoryTab.class)
public interface YACLScreenCategoryTabAccessor {
    @Accessor
    Button getSaveFinishedButton();

    @Accessor
    ListHolderWidget<OptionListWidget> getOptionList();

}
