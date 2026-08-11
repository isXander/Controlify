---
title: Stadia Controller
---

# Stadia Controller

<ControllerCompatibility guide="/users/controller-setup-guides/stadia-controller" />

In order to use your Stadia controller with Bluetooth, you must patch the firmware to give it Bluetooth support.

Stadia controllers were shipped with only Wi-Fi support, for the now dead Stadia streaming platform.

[You can switch to Bluetooth mode here](https://christopherklay.github.io/stadiacontroller/)

## Windows rumble support

Rumble is unsupported when the Stadia controller is connected to Windows over Bluetooth. The Windows Bluetooth stack
blocks the controller's rumble packet because it is not included in the device's HID descriptor. See
[SDL issue #7224](https://github.com/libsdl-org/SDL/issues/7224) for the investigation.
