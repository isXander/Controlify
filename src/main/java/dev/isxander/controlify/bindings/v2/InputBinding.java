package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.v2.inputmask.Bind;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface InputBinding {
    ResourceLocation id();

    Component name();
    Component description();

    Component bindIcon();

    StateAccess createStateAccess(int historyRequired);
    StateAccess createStateAccess(int historyRequired, Consumer<StateAccess> pushEvent);
    void returnStateAccess(StateAccess stateAccess);

    void pushState(float state);

    void setBoundBind(Bind bind);
    Bind boundBind();

    Bind defaultBind();

    Set<BindContext> contexts();

    Optional<RadialIcon> radialIcon();

    AnalogueOutput analogueNow();
    AnalogueOutput analoguePrev();

    DigitalOutput digitalNow();
    DigitalOutput digitalThen();

    DigitalOutput justPressed();
    DigitalOutput justReleased();
    DigitalOutput justTapped();

    GuiPressOutput guiPressed();
}
