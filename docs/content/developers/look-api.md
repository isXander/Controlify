---
title: Look API
---

# Look API

_Change how the camera turns with a controller._

Every frame, Controlify turns the player's camera from **look sources**, multiplied by **look scalers**:

- A source adds to the turn. Controlify's sources are stick look, flick stick and gyro.
- A scaler multiplies it, like slowing the camera down while zoomed in.

The Look API only handles controller look. Mouse look is untouched, so a mod that changes how the camera turns
still handles the mouse itself, since it has to work without Controlify.

## Scalers

Most addons only need a scaler. The example zooms the camera in while its zoom key is held, and slows controller
look to match, so aiming feels the same zoomed in:

<<< @/../../src/example/java/dev/isxander/controlify/example/look/ZoomLook.java#scaler

Register scalers in `LookEvents.REGISTER`, subscribed from your [entrypoint](controlify-entrypoint). The factory
creates a scaler for each controller. Every frame, the scaler multiplies two kinds of contribution:

| Method | Multiplies |
|---|---|
| `rate(yaw, pitch)` | Rate contributions, like stick look |
| `direct(yaw, pitch)` | Direct contributions, like gyro |

Usually scale both. All scales multiply together, so the order scalers run in doesn't matter. A scaler can't tell
sources apart: it scales every source's rate or direct contribution alike.

The total turn each frame is:

```text
turn = rate × frame time × rate scales + direct × direct scales + scripted
```

Scripted contributions can't be scaled. Flick stick is scripted, so a flick always turns the camera to where the
stick points, zoomed in or not.

## Sources

A source adds its own contribution to the turn. The example lets the player steer a boat by tilting the controller
like a steering wheel: the further it's tilted, the faster its source turns the camera, and the boat steers towards
where its driver looks. Holding a tilt keeps turning, so it's a rate.

<<< @/../../src/example/java/dev/isxander/controlify/example/look/TiltSteeringLook.java#source

Register a factory with `registerSource(id, factory)` in the same event, which creates one `LookSource` for each
controller. Here the constructor is the factory.

`contribute(ctx, out)` is called every frame while controller look is being processed. Add to the turn with one
of three kinds of contribution. Positive yaw turns right, and positive pitch looks down.

| Method | Use for | Units |
|---|---|---|
| `rate(yaw, pitch)` | A velocity, like a stick held in one direction | Degrees per second. Controlify multiplies by the frame time. |
| `delta(yaw, pitch)` | A physical displacement, like turning a gyro | Degrees |
| `scripted(yaw, pitch)` | A generated rotation that scalers mustn't change, like a flick | Degrees |

Contributions from every source add together, so the order sources run in doesn't matter either.

`reset()` is called when look processing is interrupted, like when a screen opens or the window loses focus. Drop
any rotation still in progress there.

`LookContext` gives each frame's state:

| Field | Meaning |
|---|---|
| `controller` | The controller whose look is being processed |
| `player` | The player being turned |
| `dt` | The frame time, in seconds |
| `yaw`, `pitch` | The camera's rotation at the start of the frame, in degrees |

Keep a source self-contained, because a scaler can't tell which source a contribution came from. Each kind of
input has its own sensitivity: Controlify has a stick look sensitivity and a gyro sensitivity, and each is applied
inside its own source. There's no general look sensitivity, so a source for a new kind of input has a sensitivity
setting of its own. Other behaviour that belongs to one source, like Controlify's gyro and stick look slowing down
while aiming, goes inside that source too.

Physically turning the player's camera only happens with a controller. Use a source for how the player controls the
camera, not for effects like camera shake, which should work the same with a mouse.
