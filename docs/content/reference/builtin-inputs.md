---
title: Built-in Inputs
---

# Built-in Inputs

Every controller input, like pressing the A button or pushing the left stick up, has an ID. You use these IDs in
[default bindings](../developers/actions#default-bindings) and [font mappings](../resource-packs/input-glyphs).

Which IDs a controller uses depends on whether SDL recognises it as a gamepad. A gamepad uses the
[gamepad inputs](#gamepad-inputs), and anything else uses the [generic joystick inputs](#generic-joystick-inputs).
A controller type can also force a controller to be a joystick with `force_joystick` (see
[Custom Controller Identification](../resource-packs/custom-controller-identification)).

To see the inputs your controller reports, open Controlify's **Global Settings**, press
**Copy Debug Dump To Clipboard** and look under **Available inputs** for your controller.

## Gamepad inputs

### Buttons

| ID | Button |
|---|---|
| `controlify:button/south` | Bottom face button (A on Xbox, Cross on PlayStation) |
| `controlify:button/east` | Right face button |
| `controlify:button/west` | Left face button |
| `controlify:button/north` | Top face button |
| `controlify:button/left_shoulder` | Left bumper |
| `controlify:button/right_shoulder` | Right bumper |
| `controlify:button/left_stick` | Left stick click |
| `controlify:button/right_stick` | Right stick click |
| `controlify:button/back` | Back or Select |
| `controlify:button/start` | Start |
| `controlify:button/guide` | Guide or Home |
| `controlify:button/dpad_up`<br>`controlify:button/dpad_down`<br>`controlify:button/dpad_left`<br>`controlify:button/dpad_right` | D-pad |

### Axes

Each stick is split into four inputs, one for each direction, and each one goes from `0` to `1`.

| ID | Axis |
|---|---|
| `controlify:axis/left_trigger` | Left trigger |
| `controlify:axis/right_trigger` | Right trigger |
| `controlify:axis/left_stick_up`<br>`controlify:axis/left_stick_down`<br>`controlify:axis/left_stick_left`<br>`controlify:axis/left_stick_right` | Left stick |
| `controlify:axis/right_stick_up`<br>`controlify:axis/right_stick_down`<br>`controlify:axis/right_stick_left`<br>`controlify:axis/right_stick_right` | Right stick |

### Extra buttons

Most gamepads don't have these.

| ID | Button |
|---|---|
| `controlify:button/misc_1`<br>…<br>`controlify:button/misc_6` | Extra buttons. What they are depends on the controller. On a DualSense, `misc_1` is the microphone button, and on a Switch controller it's the capture button. |
| `controlify:button/left_paddle_1`<br>`controlify:button/left_paddle_2`<br>`controlify:button/right_paddle_1`<br>`controlify:button/right_paddle_2` | Back paddles |
| `controlify:button/touchpad_1` | Touchpad click |
| `controlify:button/touchpad_2` | Second touchpad click. The SDL driver only reports one touchpad button, as `touchpad_1`, so nothing currently reports this input. |

### Touch sensors

Touch sensors report whether something is touching them. They're reported separately from inputs, so you can't
bind them to actions. Controlify uses them for the gyro's touch activation setting.

| ID | Sensor |
|---|---|
| `controlify:capsense/left_stick`<br>`controlify:capsense/right_stick` | A finger on a stick |
| `controlify:capsense/left_grip`<br>`controlify:capsense/right_grip` | A hand on a grip |
| `controlify:capsense/touchpad_1`<br>`controlify:capsense/touchpad_2`<br>… | A finger on a touchpad, numbered from `1` |

## Generic joystick inputs

A joystick can have any number of buttons, axes and hats, so their IDs use the index that SDL reports, starting at
`0`.

| ID | Example | Input |
|---|---|---|
| `controlify:button/<index>` | `controlify:button/0` | A button |
| `controlify:axis/<index>/positive`<br>`controlify:axis/<index>/negative` | `controlify:axis/0/positive` | One direction of an axis |
| `controlify:hat/<index>/up`<br>`controlify:hat/<index>/right`<br>`controlify:hat/<index>/down`<br>`controlify:hat/<index>/left` | `controlify:hat/0/up` | One direction of a hat switch |

Each axis is split into two inputs, one for each direction, and each one goes from `0` to `1`.
