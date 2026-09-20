---
title: Built-in Inputs
---

# Built-in Inputs

For each controller input, like pressing the A button, or pushing up on the left stick, Controlify has a unique identifier
that uses Minecraft's resource location system.

These inputs are provided arbitrarily by controller drivers, but these are some of the common ones.

## Gamepad Inputs

### Buttons
- `controlify:button/south`
- `controlify:button/east`
- `controlify:button/west`
- `controlify:button/north`
- `controlify:button/left_shoulder`
- `controlify:button/right_shoulder`
- `controlify:button/left_stick`
- `controlify:button/right_stick`
- `controlify:button/back`
- `controlify:button/start`
- `controlify:button/guide`
- `controlify:button/dpad_up`
- `controlify:button/dpad_down`
- `controlify:button/dpad_left`
- `controlify:button/dpad_right`

### Axes
- `controlify:axis/left_trigger`
- `controlify:axis/right_trigger`
- `controlify:axis/left_stick_up`
- `controlify:axis/left_stick_down`
- `controlify:axis/left_stick_left`
- `controlify:axis/left_stick_right`
- `controlify:axis/right_stick_up`
- `controlify:axis/right_stick_down`
- `controlify:axis/right_stick_left`
- `controlify:axis/right_stick_right`

### Additional Inputs
For example, on DualSense, `misc_1` is the mute button.
Most of these remain unused by most controller drivers.

- `controlify:button/misc_1`
- `controlify:button/misc_2`
- `controlify:button/misc_3`
- `controlify:button/misc_4`
- `controlify:button/misc_5`
- `controlify:button/misc_6`

### Paddles
::: info
Some controllers with paddles, like the Xbox Elite controllers, do not work with this.
Their paddles are mapped to other buttons in the firmware of the controller. No data of
paddles ever reaches the connected computer.
:::

- `controlify:button/right_paddle_1`
- `controlify:button/right_paddle_2`
- `controlify:button/left_paddle_1`
- `controlify:button/left_paddle_2`

### Touchpad
The actual actuation click of the touchpad itself, hence, a button.

- `controlify:button/touchpad_1`
- `controlify:button/touchpad_2`

## Generic Joystick Inputs

For generic joysticks with an arbitrary amount of buttons and axes, Controlify creates resource
locations by using the *index* of the button or axis.

### Buttons

`controlify:button/<buttonindex>`

So for index 0, it would be `controlify:button/0`.

### Axes

`controlify:axis/<axisindex>/(positive|negative)`

So for index 0, pushing in the positive direction, it would be `controlify:axis/0/positive`.

::: info
Controlify axes operate within a range of [0, 1], not [-1, 1]. Hence, each axis is split into two inputs:
one for the positive direction, and one for the negative direction.
:::
