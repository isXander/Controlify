---
title: Actions
---

# Actions

_Let players trigger your mod's features from a controller._

An action is something the player can do with a controller, like jumping or dropping an item. Your mod declares
an action in code and reads its state from your own code. Everything about *how* it is triggered is data:

- its **bindings** say which inputs trigger it, and players can rebind them in the controls settings;
- its **activator** says what those inputs must do (a tap, a hold, a long press) and what the action outputs.

Because these are resource files, resource packs, including a server's resource pack, can change how your action
is triggered without any code.

An action reports its state through three channels:

| Channel | Read with | Use it for |
|---|---|---|
| **Pulse** | `pulse().consumePulse()` | Something that either happened or didn't. Dropping an item is a pulse: you have dropped it or you haven't. |
| **Latch** | `latch().isLatchActive()` | Something the player is *doing* for a while. Jumping is a latch, because holding jump keeps jumping every time you land. |
| **Continuous** | `continuous().getContinuous()` | An amount from `0` to `1`, like how far a trigger is pulled. |

This page builds a boating mod with three actions: a horn (pulse), a boost (latch) and a throttle (continuous).
The full source is the [example addon](https://github.com/isXander/Controlify/tree/main/src/example).

## Declaring an action

### Registering it

Register actions in `ActionEvents.REGISTER_ACTIONS`, subscribed from your
[Controlify entrypoint](controlify-entrypoint). Keep the `ActionAccessor` that `register` returns:

<<< @/../../src/example/java/dev/isxander/controlify/example/actions/BoatActions.java#accessors

<<< @/../../src/example/java/dev/isxander/controlify/example/actions/BoatActions.java#register-actions

An action needs an ID, a category and at least one [layer](#layers). The category groups it in the controls
settings. The name and description default to these language keys:

- `controlify.action.<namespace>.<path>`
- `controlify.action.<namespace>.<path>.desc` (optional)

Call `.name(Component)` or `.description(Component)` to set them yourself.

`shouldExpose(Predicate<Controller>)` hides the action from the controls settings on controllers it doesn't
apply to. The action still exists on those controllers.

### Activators

An action has no activation behaviour until it has an activator. Without one, it appears in the controls settings
and can be bound, but it never outputs anything.

Activators live in `assets/controlify/controller/activators.json`. Every mod and resource pack contributes to the
same file, and a higher-priority pack replaces the entry for any action it lists:

<<< @/../../src/example/resources/assets/controlify/controller/activators.json

A type with no options can be written as just its name, like `"span"`.

| Type | Outputs | Options |
|---|---|---|
| `pulse` | One pulse per gesture | `gesture`: an instant gesture, default `press`<br>`editable`: default `false` |
| `span` | Holds the latch while the gesture lasts, and pulses once when it starts | `gesture`: a span gesture, default `hold`<br>`editable`: default `false`<br>`pulse_on_start`: default `true` |
| `continuous` | The bound axis value | |
| `latch_toggle` | Toggles the latch on each pulse | `source_pulse`: the activator whose pulses toggle it |
| `pulse_repeat_latch` | Pulses when the latch turns on, then repeats while it stays on | `source_latch`: the activator to read<br>`initial_delay_ms`: default `250`<br>`repeat_delay_ms`: default `50` |

`pulse`, `span` and `continuous` are leaves: they read the action's bindings directly. `latch_toggle` and
`pulse_repeat_latch` transform another activator's output instead, so they have no gesture of their own. Only a
leaf's gesture can be `editable`, which lets players change it per binding in the controls settings. To make
a `latch_toggle` editable, make its `source_pulse` leaf editable.

#### Gestures


| Gesture | Kind | Fires |
|---|---|---|
| `press` | instant | as soon as the input goes down |
| `release` | instant | when the input comes up |
| `tap` | instant | on a short press |
| `long_press` | instant | once the input has been held for the long press duration |
| `gui_press` | instant | on release, if there was no GUI navigation since the press |
| `hold` | span | from press to release |
| `long_hold` | span | from the long press duration until release |

`pulse` takes an instant gesture and `span` takes a span gesture. An editable gesture can only be changed to
another gesture of the same kind.

::: tip
Prefer `tap` for pulse actions. A tap and a long press on the same input don't conflict, so players can put both
on one button. `press` and `release` conflict with almost everything else on that input.
:::

#### Combining activators

An entry can also be a list of activators, when an action needs more than one channel. All of them read the same
bindings. Controlify's GUI press does this:

```json
"controlify:gui_press": [
  {
    "type": "span",
    "pulse_on_start": false
  },
  {
    "type": "pulse",
    "gesture": "gui_press"
  }
]
```

Buttons activate on the pulse, which only fires if the player didn't navigate away before releasing. The on-screen
keyboard reads the latch instead, to repeat a key while it is held. Similarly, the look actions are
`["continuous", "pulse"]`: stick look reads the continuous value, and flick stick flicks on the pulse.

A list can contain at most one editable leaf.

### Default bindings

Default bindings are also data. Defaults for every controller go in
`assets/controlify/controller/default_bind/default.json`:

<<< @/../../src/example/resources/assets/controlify/controller/default_bind/default.json

Each action takes a list of bindings. An action can have several bindings, but they all share its one activator.
Pulses from every binding add together, the latch is active while any binding holds it, and the continuous value
is the largest of them.

A binding is an input ID (see [Built-in Inputs](../reference/builtin-inputs)), a chord, or either of those with a
gesture.

A **chord**, like the example's zoom binding, fires when the trigger is pressed while every modifier is already
held. It takes one or two modifiers, and can't be bound to an action with a `continuous` activator.

A **gesture** on a binding replaces the gesture of the activator's editable leaf, for that binding only:

```json
"controlify:drop": ["controlify:button/dpad_down"],
"controlify:drop_stack": [{"input": "controlify:button/dpad_down", "gesture": "long_press"}]
```

This is the same setting players change in the controls settings, and it lets two actions share an input. Drop
and drop stack both use the d-pad down: drop stack's activator is an editable `tap`, and its default binding
changes it to `long_press`. The gesture is ignored, with an error in the log, if the activator has no editable
leaf of the same kind.

An empty list, like `"controlify:gyro_button": []`, leaves the action unbound by default.

To change defaults for one type of controller, put the file at
`assets/<namespace>/controller/default_bind/<path>.json`, where `<namespace>:<path>` is the controller type, like
`assets/controlify/controller/default_bind/dualsense.json` for `controlify:dualsense`. See
[Built-in controller namespaces](../reference/builtin-controller-namespaces) for the types.

When several files define defaults for a controller, Controlify takes each action's defaults from the first file
that lists it, checking in this order:

1. the controller type's files, from the highest-priority resource pack down;
2. the `default.json` files, in the same order.

So any file only needs to list the actions it changes. Add `"clear_below": true` to a file to leave every action
it doesn't list unbound instead of falling through to the files below it.

### Radial menu icon

An action can be picked in the radial menu once it has an icon. Icons go in
`assets/controlify/controller/radial_icons.json`, which merges across resource packs in the same way as
activators:

<<< @/../../src/example/resources/assets/controlify/controller/radial_icons.json

An icon is exactly one of:

- `model`: an item model ID, like `minecraft:goat_horn`. It doesn't have to belong to a registered item: a model
  definition at `assets/<namespace>/items/<path>.json` works on its own. Use `{ "id": "...", "enchant_glint": true }`
  to add an enchantment glint.
- `texture`: a sprite in the GUI atlas, drawn at 18×18. `example:radial/horn` loads
  `assets/example/textures/gui/sprites/radial/horn.png`.

An optional `overlay` text component is drawn on top of the icon.

## Using an action

### Reading its channels

Resolve the action on the current controller and read the channels its activator drives. The example reads them
in a client tick:

<<< @/../../src/example/java/dev/isxander/controlify/example/actions/BoatActions.java#read

The `BoatFeedback` calls play [feedback](feedback-api) for each action.

There are two ways to consume pulses:

- `while (pulse.consumePulse())` runs once per pulse. Dropping an item uses this, so three quick taps drop three
  items.
- `if (pulse.consumeAllPulses())` runs once however many pulses arrived. Pausing uses this.

Controlify processes controller input once per frame, before Minecraft runs that frame's ticks. Reading actions in
a client tick suits most actions. Code that is sensitive to input latency can read them every frame instead:
Controlify turns the camera per frame for this reason.

A pulse stays available until it is consumed, or until the end of the first frame that runs a tick. Above 20 FPS
most frames run no tick, so a pulse waits for the next frame that does. Below 20 FPS a frame can run several
ticks, and the pulse is available to all of them until one consumes it.

Keep the `ActionAccessor`, not the `Action` that `on(controller)` returns. Switching profile can replace an
action's instance, and the accessor always resolves the current one.

### Showing its input

`ActionGlyphs.action` creates a text component that renders as the action's current input, such as the
controller's south button:

<<< @/../../src/example/java/dev/isxander/controlify/example/actions/BoatActions.java#glyph

The glyph is resolved by the client that displays the component, using that player's bindings and controller.
Creating one doesn't need a controller, so servers can send these components too. On clients without Controlify,
`actionWithFallback` shows a fallback component instead. `actionWithActiveFallback` also shows the fallback to
players who are using keyboard and mouse.

## Layers

A layer decides when an action can receive input. Most in-game actions belong in `BuiltinActionLayers.IN_GAME`,
and screen actions in one of the screen layers. Register your own layer when your mod adds a mode with actions
that only exist in that mode.

Controlify has no layer for boats or other mounts: steering a boat reuses the movement actions, so nothing is
exclusive to it. The example mod adds a horn, a boost and a throttle that only make sense in a boat, so it gives
them a layer:

<<< @/../../src/example/java/dev/isxander/controlify/example/actions/BoatActions.java#register-layer

Register layers in `ActionEvents.REGISTER_LAYERS`, which fires before `REGISTER_ACTIONS`.

Layers form a stack, and input is delivered from the top down. Each active layer's mode decides what reaches the
layers below it:

| Mode | Layers below receive |
|---|---|
| `TRANSPARENT` | Every input |
| `OVERLAY` (default) | Every input this layer doesn't bind |
| `BLOCKING` | Nothing |

The boating layer is an overlay above `IN_GAME`. While the player is in a boat, the horn's south button no longer
reaches jump, but every other in-game input still works.

Place a layer with `above`, `below`, `top` or `bottom`, relative to other layers.

### Built-in layers

`BuiltinActionLayers` has Controlify's layers, from top to bottom:

| Layer | Mode | Active while |
|---|---|---|
| `RADIAL_MENU` | Overlay | The radial menu is open |
| `V_MOUSE_CURSOR` | Transparent | The virtual mouse is on |
| `CONTAINER` | Transparent | A container screen is open |
| `REGULAR_SCREEN` | Transparent | A screen is open and the virtual mouse is off |
| `ANY_SCREEN` | Blocking | Any screen is open |
| `IN_GAME` | Overlay | A world is loaded |

Every layer above `ANY_SCREEN` requires it, so none of them competes with `IN_GAME` for an input.

`requires` declares that a layer is only ever active while another one is, whatever its own predicate says.
Declare it whenever it's true, because the binding conflict checks in the controls settings rely on it.

## Key mappings

Controlify creates an action for every modded `KeyMapping`, so mods with no Controlify integration still work
with a controller. The automatic action holds the key mapping down while its binding is held, and is always in the
radial menu, with a book icon unless it has a [radial icon](#radial-menu-icon). Its ID is
`controlify_modded:` followed by the key mapping's name, lowercased, with every character other than `a-z`, `0-9`,
`/`, `.`, `_` and `-` replaced by `_`.

If your mod has a key mapping for something you've made an action for, the player would see both in the controls
settings. Tell Controlify how they relate in `ActionEvents.REGISTER_PROXIES`, which fires right after
`REGISTER_ACTIONS`:

<<< @/../../src/example/java/dev/isxander/controlify/example/actions/KeyMappingProxies.java#proxy

- `dontAutoProxyKeyMapping` stops the automatic action. Use it for every key mapping your actions replace, and read
  the action's channels in your own code. The example's horn key mapping is one.
- `proxyKeyMapping` stops the automatic action and drives the key mapping from your action instead. Use it only
  when code you don't control reads the key mapping directly, such as other mods calling `isDown()` on it.
  Controlify proxies attack and use for this reason, because many mods read `options.keyAttack` and
  `options.keyUse`.

::: warning
Don't proxy a key mapping just to avoid reading the action. The key mapping is held down for as long as the
action's latch is on, so a proxied action needs a `hold`, and that is its activator unless you give it one. A
hold takes every press of its button, so no other action can share that button, and players can only change it
to `long_hold`. An action you read yourself can use a `tap`, and share its button with a long press.
:::

A proxy consumes the action's pulses as key clicks, so don't read them yourself.
`proxyKeyMapping(action, keyMapping, controller -> ...)` toggles the key mapping on each pulse when the predicate
is true, instead of holding it.
