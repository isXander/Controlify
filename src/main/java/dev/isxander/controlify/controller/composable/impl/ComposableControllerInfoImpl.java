package dev.isxander.controlify.controller.composable.impl;

import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.controller.composable.ComposableController;
import dev.isxander.controlify.controller.composable.ComposableControllerInfo;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.driver.GUIDProvider;
import dev.isxander.controlify.driver.NameProviderDriver;

public class ComposableControllerInfoImpl implements ComposableControllerInfo {
    private final String uid;
    private final UniqueControllerID ucid;
    private final String guid;
    private final String driverName;

    public ComposableControllerInfoImpl(String uid, UniqueControllerID ucid, GUIDProvider guidDriver, NameProviderDriver nameDriver) {
        this.uid = uid;
        this.ucid = ucid;
        this.guid = guidDriver.getGUID();
        this.driverName = nameDriver.getName();
    }

    @Override
    public String uid() {
        return uid;
    }

    @Override
    public UniqueControllerID ucid() {
        return ucid;
    }

    @Override
    public String guid() {
        return guid;
    }

    @Override
    public String createName(ComposableController<?> controller) {
        if (controller.config().customName != null)
            return controller.config().customName;
        if (controller.type() != ControllerType.UNKNOWN && controller.type().friendlyName() != null)
            return controller.type().friendlyName();
        return driverName;
    }
}
