package dev.isxander.controlify.controller.gyro;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.config.settings.profile.GyroSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class GyroComponent extends ECSComponentImpl {
    public static final Identifier ID = CUtil.rl("gyro");

    private GyroStateC gyroState = GyroStateC.ZERO;
    private final GyroCalibrator calibrator = new GyroCalibrator();

    // Save calibration every 5 seconds (100 ticks)
    private static final int CALIBRATION_SAVE_INTERVAL = 100;
    private int ticksSinceLastSave = 0;
    private GyroStateC lastSavedBias = GyroStateC.ZERO;

    @Override
    public void attach(ControllerEntity controller) {
        super.attach(controller);

        // Load the calibration from device settings
        DeviceSettings deviceSettings = Controlify.instance().config().getSettings()
                .getOrCreateDeviceSettings(controller.uid());
        GyroStateC savedOffset = deviceSettings.gyroCalibration.offset;

        // Initialize the calibrator with the saved offset
        calibrator.setBias(savedOffset);
        lastSavedBias = savedOffset;
    }

    public GyroStateC getState() {
        return this.gyroState;
    }

    public void setState(GyroStateC state) {
        // Update the calibrator with the raw reading
        calibrator.update(state);
        // Store the calibrated state
        this.gyroState = calibrator.applyCalibration(state);

        // Periodically save calibration if it has changed significantly
        ticksSinceLastSave++;
        if (ticksSinceLastSave >= CALIBRATION_SAVE_INTERVAL) {
            ticksSinceLastSave = 0;
            saveCalibrationIfChanged();
        }
    }

    private void saveCalibrationIfChanged() {
        GyroStateC currentBias = calibrator.getCurrentBias();

        // Check if bias has changed significantly (more than 0.001 rad/s on any axis)
        float threshold = 0.001f;
        if (Math.abs(currentBias.pitch() - lastSavedBias.pitch()) > threshold ||
            Math.abs(currentBias.yaw() - lastSavedBias.yaw()) > threshold ||
            Math.abs(currentBias.roll() - lastSavedBias.roll()) > threshold) {

            // Save to device settings
            DeviceSettings deviceSettings = Controlify.instance().config().getSettings()
                    .getOrCreateDeviceSettings(controller().uid());
            deviceSettings.gyroCalibration.offset = new GyroState(currentBias);

            // Mark config as dirty so it gets saved at some point
            // We don't really care if it never gets saved if the user never changes any other config
            // that triggers a save, we just recalibrate from last saved on next launch
            Controlify.instance().config().markDirty();

            lastSavedBias = currentBias;
        }
    }

    public GyroCalibrator getCalibrator() {
        return this.calibrator;
    }

    public GyroSettings settings() {
        return this.controller().settings().gyro;
    }

    public GyroSettings defaultSettings() {
        return this.controller().defaultSettings().gyro;
    }

    @Override
    public Identifier id() {
        return ID;
    }

}
