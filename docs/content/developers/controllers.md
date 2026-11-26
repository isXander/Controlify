---
title: Controllers
---

# Controllers

_Find out how the player is playing, and what their controller can do._

## Input mode

The input mode says whether the player is using a controller. `ControlifyApi.get().currentInputMode()` returns one
of:

| Mode | Meaning | `isController()` | `isKeyboardMouse()` |
|---|---|---|---|
| `KEYBOARD_MOUSE` | Keyboard and mouse | false | true |
| `CONTROLLER` | A controller | true | false |
| `MIXED` | A controller, while still accepting keyboard and mouse input | true | true |

Mixed input is a setting that lets a controller and a keyboard and mouse be used at the same time without
conflicting. The game looks like it's being played with a controller, but keeps accepting keyboard and mouse input,
which is why both checks are true.

Check the mode for anything controller-specific in your UI. The example only shows the horn's button when the player
boards a boat with a controller:

<<< @/../../src/example/java/dev/isxander/controlify/example/actions/BoatActions.java#hint

`ControlifyEvents.INPUT_MODE_CHANGED` fires when the mode changes. `setInputMode` exists too, though mods rarely
need to change the mode themselves.

## Finding controllers

`ControlifyApi` gives you the connected controllers:

| Method | Returns |
|---|---|
| `getCurrentController()` | The controller the player is using, if any |
| `getConnectedControllers()` | Every connected controller |
| `getControllerByUid(uid)` | A connected controller by its unique ID |

To react when controllers connect, disconnect or change, use the [controller events](controlify-entrypoint#state-events).

A `Controller` describes one controller:

| Method | Returns |
|---|---|
| `uid()` | A unique ID that stays the same when the controller is unplugged and plugged back in. Key any data you keep per controller by it. |
| `name()` | The controller's name to show the player: its controller type's name, or the name it reports itself if the type has none |
| `driverReportedName()` | The name the controller reports itself |
| `typeId()` | Its [controller type](../reference/builtin-controller-namespaces), like `controlify:dualsense` |
| `guideVerbosity()` | How much the player wants [button guides](contextual-api#button-guides) to show |
| `input()` | Its actions and inputs |
| `gyro()` | Its gyroscope, if it has one |
| `dualsense()` | DualSense-only features, if it's a DualSense |

## Input

`input().getAction(id)` returns the controller's action with that ID, or null if there's none. Use it for actions
you only know by ID, like another mod's. For your own, keep the [`ActionAccessor`](actions#registering-it) and call
`on(controller)`.

`input().isGamepad()` is false for controllers that aren't recognised as gamepads. They report raw buttons and axes,
and need a joystick mapping before their inputs line up with a gamepad's.

## Gyro

`gyro()` is present for controllers with a gyroscope. Both of its readings use the controller's frame: X to the
right, Y up and Z towards the player, when it's held upright in front of you.

- `getState()` is how far the controller rotated since the last frame, in radians: `x` is pitch, `y` is yaw and `z`
  is roll. It's zero when there's been no recent motion.
- `gravity()` is which way is up, as a unit vector, estimated from the accelerometer and gyro. It gives the
  controller's tilt directly, rather than adding up rotations, so use it for anything that depends on how the
  controller is held. It's empty when the controller has no accelerometer.

To turn the camera with them, use a [look source](look-api#sources), which runs every frame. The example's tilt
steering source reads `gravity()`, so tilting the controller like a steering wheel steers the boat.

## DualSense

`dualsense()` is present for DualSense controllers. `setMicLightMode` sets the light on its mute button to `OFF`,
`ON` or `BREATHING`. Voice chat mods can use it to show whether the player is muted, as Controlify does for
Simple Voice Chat.
