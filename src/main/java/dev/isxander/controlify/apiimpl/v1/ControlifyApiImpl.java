package dev.isxander.controlify.apiimpl.v1;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.v1.ControlifyApi;
import dev.isxander.controlify.api.v1.bindings.BuiltinBindings;
import dev.isxander.controlify.api.v1.widgetguide.WidgetGuideApi;

public class ControlifyApiImpl implements ControlifyApi {
    public static final ControlifyApiImpl INSTANCE = new ControlifyApiImpl();

    private ControlifyApiImpl() {
    }

    @Override
    public WidgetGuideApi widgetGuide() {
        return WidgetGuideApiImpl.INSTANCE;
    }

    @Override
    public BuiltinBindings builtinBindings() {
        return BuiltinBindingsImpl.INSTANCE;
    }

    @Override
    public boolean isControllerMode() {
        return Controlify.instance().currentInputMode().isController();
    }
}
