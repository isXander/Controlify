---
title: Controllers
---

# Controllers

You may be wondering how Controlify actually works. This page will explain how Controlify handles and uses
controllers internally.

## Libraries

Controlify makes great use of [SDL3](https://wiki.libsdl.org/SDL3/FrontPage) for handling controllers.
It provides a simple but extensive API that supports advanced features like rumble, sensors like gyro and
accelerometer, touchpads and gamepad effects like LEDs and haptics. SDL does this all whilst supporting an
extensive list of controllers over many APIs: DirectInput, XInput, HIDAPI, WGI, and system-specific APIs like
those found on Android and iOS.

If for whatever reason Controlify fails to load SDL or the user chooses not to download it, Controlify will fall back
on the Minecraft-provided [GLFW](https://www.glfw.org/) controller API. This API does not do much more than basic
gamepad and joystick input, not even rumble. It can, however, prove more reliable and serves as a solid fallback.

For [Steam Deck](https://www.steamdeck.com/) users, a custom-built library is used
([steamdeck4j](https://github.com/isxander/steamdeck4j)) to interface with SteamOS (which gathers input) via the OS's
embedded CEF debugger (Chromium Embedded Framework). For those that didn't know, SteamOS's UI is built on a browser
engine that is based on Chromium. This communication requires the Homebrew effort for Steam Deck,
[Decky](https://decky.xyz/), that enables and manages the debugger in a secure fashion
(ensuring it isn't exposed on the network).

## Controlify's Controller System

Uncommon in Java-land, Controlify uses a composable system for handling controllers with different feature-sets.

The `ControllerEntity` is simply a container for the components and driver of a controller. These components are:

- `InputComponent` - handles and exposes inputs from the controller, pressing buttons and the state of axes.
- `BatteryComponent` - exposes the battery state of the controller (wired, not so, percentage).
- `DualSenseComponent` - specific to the DualSense controller, exposes adaptive triggers and controls the mute-light LED state.
- `GyroComponent` - exposes the state of the gyro sensor.
- `HDHapticComponent` - allows playing HD haptic effects on controllers that support it (only DualSense as of now)
- `DriverNameComponent` - exposes the driver-provided name of the controller
- `GUIDComponent` - exposes the GUID of the controller
- `UIDComponent` - exposes the Controlify-generated unique ID of the controller (used for things like config keys)
- `NativeKeyboardComponent` - allows Controlify to open the native keyboard modal on the system, relevant to the specific controller
- `BluetoothDeviceComponent` - a marker component that indicates the controller is connected via Bluetooth
- `RumbleComponent` - allows playing simple dual-motor rumble effects on the controller
- `TriggerRumbleComponent` - allows playing rumble effects on the triggers of the controller
- `SteamDeckComponent` - a marker component that indicates the controller is a Steam Deck
- `TouchpadComponent` - exposes the state of touchpads on the controller

Each component may have an attached config object, that is used to store settings for the component.

### Drivers

Controlify uses a sort of driver system to attach, handle, and supply data to the controller components.
For example, there is `SDLGamepadDriver`, `GLFWGamepadDriver`, `SteamDeckDriver`, and more. Drivers can be compounded
since they contain no reference to the controller entity. This allows functionality from one underlying library (SDL)
to be used in conjunction with another (GLFW or steamdeck4j) to provide a more complete experience.

### Controller Manager

The `ControllerManager` has many jobs, it handles discovery, creation, destruction and storage of controller entities,
as well as constructing and assigning drivers to said entities.

Since discovery is a library-specific task, the manager is abstracted to allow for different implementations: SDL and
GLFW. Using both at the same time would result in duplicate discoveries, as both libraries may both detect the same
controller.
