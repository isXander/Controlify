package dev.isxander.controlify.config.settings.profile;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.config.dto.profile.ProfileConfig;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class ProfileSettings {
    public final GenericControllerSettings generic;
    public final InputSettings input;
    public final RumbleSettings rumble;
    public final HDHapticSettings hdHaptic;
    public final GyroSettings gyro;
    public final BluetoothDeviceSettings bluetoothDevice;

    public ProfileSettings(
            GenericControllerSettings generic,
            InputSettings input,
            RumbleSettings rumble,
            HDHapticSettings hdHaptic,
            GyroSettings gyro,
            BluetoothDeviceSettings bluetoothDevice
    ) {
        this.generic = generic;
        this.input = input;
        this.rumble = rumble;
        this.hdHaptic = hdHaptic;
        this.gyro = gyro;
        this.bluetoothDevice = bluetoothDevice;
    }

    public static ProfileSettings fromDTO(ProfileConfig dto) {
        return new ProfileSettings(
                GenericControllerSettings.fromDTO(dto.generic()),
                InputSettings.fromDTO(dto.input()),
                RumbleSettings.fromDTO(dto.rumble()),
                HDHapticSettings.fromDTO(dto.hdHaptic()),
                GyroSettings.fromDTO(dto.gyro()),
                BluetoothDeviceSettings.fromDTO(dto.bluetoothDevice())
        );
    }

    public ProfileConfig toDTO() {
        return new ProfileConfig(
                generic.toDTO(),
                input.toDTO(),
                rumble.toDTO(),
                hdHaptic.toDTO(),
                gyro.toDTO(),
                bluetoothDevice.toDTO()
        );
    }

    public static ProfileSettings createDefault(@Nullable Identifier controllerType) {
        var dto = Controlify.instance()
                .defaultConfigManager()
                .getDefaultForNamespace(controllerType);
        return ProfileSettings.fromDTO(dto);
    }
}
