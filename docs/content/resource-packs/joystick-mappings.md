---
title: Joystick Mappings
---

# Joystick Mappings

A joystick mapping converts a controller's raw inputs into other inputs, usually Controlify's
[gamepad inputs](../reference/builtin-inputs#gamepad-inputs). It also sets the controller's deadzones.

## When you need one

SDL recognises most gamepads and reports their inputs by name, like `controlify:button/south`. Any other
controller, like a flight stick or an arcade stick, is a **joystick**. A controller is also a joystick when its
controller type sets `force_joystick` (see [Custom Controller Identification](custom-controller-identification)).
A joystick reports [generic joystick inputs](../reference/builtin-inputs#generic-joystick-inputs) instead, numbered
by index, like `controlify:button/0` or `controlify:axis/1/negative`.

Controlify's default bindings and glyph sets only use gamepad inputs. Without a mapping, a joystick has:

- no default bindings, so the player has to bind every action themselves;
- no glyphs in the controls settings or button guides;
- no deadzones, so a stick that doesn't rest exactly at the centre keeps triggering whatever it's bound to.

A mapping fixes all three by turning the joystick's inputs into gamepad inputs. Inputs the mapping doesn't list keep
their raw IDs.

To check whether Controlify sees your controller as a gamepad, open Controlify's **Global Settings**, press
**Copy Debug Dump To Clipboard** and find your controller under **Connected Controllers**. `Definitely gamepad:` is
`false` for a joystick, and **Available inputs** lists every input ID it reports.

## Making a mapping in-game

Players can make a mapping for their own controller without a resource pack. When a joystick connects and has no
mapping of its own, Controlify asks whether to map it. Players can also start it from the controller's settings, in
**Hardware**, **Controller Mapping**, **Map to Gamepad**.

The **Mapping Creator** asks for each gamepad input in turn, and the player presses the joystick input that should
act as it. **I don't have this input** leaves that gamepad input unmapped, and **I made a mistake** goes back a step.
Pressing Escape stops without saving.

The mapping it saves:

- maps each captured button or axis direction to its gamepad input, with the default options in the
  [entry tables](#mapping-entries);
- has the left stick and right stick [deadzone groups](#deadzone-groups);
- belongs to that one controller, and is stored in `config/controlify/controlify.json` under
  `devices.<controller uid>.mapping`.

**Clear Mapping** in the same group removes it.

A resource pack mapping applies to every player with that controller, so they don't have to make one. The saved
mapping uses the same format as a mapping file, so you can copy it from `controlify.json` into a resource pack as a
starting point.

## Creating a mapping file

A mapping file goes at `assets/<namespace>/controller/mapping/<path>.json`, and its ID is `<namespace>:<path>`.
`<path>` can include folders.

This mapping, from the [example addon](https://github.com/isXander/Controlify/tree/main/src/example), is for an
arcade stick with a digital stick on axes `0` and `1`, eight action buttons, and Select and Start on buttons `8` and
`9`:

<<< @/../../src/example/resources/assets/examplemod/controller/mapping/arcade_stick.json

The stick becomes the left stick, so it moves the player. Buttons `6` and `7` become the triggers, because
Controlify's default bindings put attack and use on the triggers.

### Giving a controller the mapping

Set `mapping_id` on the controller's entry in
[`controller_identification.json5`](custom-controller-identification#creating-the-definition):

```json5
[
  {
    "friendly_name": "Example Arcade Stick",
    "hids": [
      [0x1234, 0x5678] // [VID, PID]
    ],
    "mapping_id": "examplemod:arcade_stick"
  }
]
```

This entry has no `id`, so the controller keeps the `controlify:default` type. Once its inputs are mapped to gamepad
inputs, that type's default bindings and glyphs apply to it.

### Which mapping a controller uses

Controlify picks the first of these that exists:

1. The mapping the player made for that controller in the Mapping Creator.
2. The mapping named by the controller type's `mapping_id`.
3. No mapping. A gamepad keeps its left stick and right stick deadzones, and a joystick has none.

If `mapping_id` names a mapping that doesn't exist, the controller falls back to step 3. A mapping file that fails
to load is skipped, with an error in the log. When two resource packs have a mapping with the same ID, the one from
the higher-priority pack is used.

Reload resources (F3 + T) to apply your changes. Connected controllers pick up the new mapping straight away.

A `mapping_id` works on gamepads too. The mapping then replaces the gamepad's built-in deadzones, so list the stick
deadzone groups in it.

## Format

A mapping file is an object with two fields, and both are required:

| Field | Type | Description |
|---|---|---|
| `mappings` | list of [mapping entries](#mapping-entries) | How each input is converted. Each `from` input can appear in only one entry. |
| `deadzones` | object of [deadzone groups](#deadzone-groups) | The controller's deadzones. Use `{}` for none. |

### Mapping entries

Each entry converts one input, `from`, into another, `to`. Every entry needs `input_type` and `output_type`, which
pick the entry type and its other fields:

| `input_type` | `output_type` | Converts |
|---|---|---|
| `button` | `button` | [A button into a button](#button-to-button) |
| `button` | `axis` | [A button into an axis](#button-to-axis) |
| `axis` | `button` | [An axis into a button](#axis-to-button) |
| `axis` | `axis` | [An axis into an axis](#axis-to-axis) |

Hat directions, like `controlify:hat/0/up`, are buttons. Axes are the split half-axes, like
`controlify:axis/0/positive`, and go from `0` to `1`. If SDL knows an axis's resting position, like a throttle that
rests at one end, Controlify measures the axis from that position, so it reads `0` at rest and only moves in one
direction.

Every field in these tables is required. Inputs other than `from` aren't changed by the entry.

Several entries can have the same `to`. The output then follows whichever of their inputs changed last.

#### Button to button

| Field | Type | Description |
|---|---|---|
| `from` | input ID | The joystick button. |
| `to` | input ID | The button it acts as. |
| `invert` | boolean | If `true`, the output is pressed while the input is released. Use it for an input that reports pressed at rest. The Mapping Creator writes `false`. |

#### Button to axis

Use it for a controller that has buttons where a gamepad has triggers, like buttons `6` and `7` in the example.

| Field | Type | Description |
|---|---|---|
| `from` | input ID | The joystick button. |
| `to` | input ID | The axis it acts as. |
| `off_state` | number | The axis value while the button is released. The Mapping Creator writes `0`. |
| `on_state` | number | The axis value while the button is pressed. The Mapping Creator writes `1`. |

#### Axis to button

Use it for an axis that should act as a button, like a joystick that reports its d-pad as axes.

| Field | Type | Description |
|---|---|---|
| `from` | input ID | The joystick half-axis. |
| `to` | input ID | The button it acts as. |
| `threshold` | number | The button is pressed while the axis is at or above this value. The Mapping Creator writes `0.5`. |

#### Axis to axis

The entry maps `min_in` to `min_out` and `max_in` to `max_out`, and scales the values in between. The input is first
clamped to the range between `min_in` and `max_in`, and the output is clamped to `0`–`1`. If `min_in` equals
`max_in`, the output is always `min_out`.

`0, 0, 1, 1` passes the value through unchanged, which is what the Mapping Creator writes. To make an axis that only
reaches `0.8` count as fully pushed, set `max_in` to `0.8`.

| Field | Type | Description |
|---|---|---|
| `from` | input ID | The joystick half-axis. |
| `to` | input ID | The axis it acts as. |
| `min_in` | number | The input value that maps to `min_out`. |
| `min_out` | number | The output value at `min_in`. |
| `max_in` | number | The input value that maps to `max_out`. |
| `max_out` | number | The output value at `max_in`. |

### Deadzone groups

A deadzone group is a set of axes that share one radial deadzone, like the four directions of a stick. Controlify
takes the distance from the centre across all the group's axes. Inside the deadzone, every axis in the group reads
`0`. Outside it, the values are scaled so they still reach `1` at the edge.

Deadzones are applied after the mapping, so a group lists the mapped IDs, like `controlify:axis/left_stick_up`. An
axis that isn't in any group has no deadzone.

`deadzones` is an object from group IDs to groups. Use the group's `name` as its key, like the Mapping Creator does.

| Field | Type | Description |
|---|---|---|
| `name` | ID | The group's ID. |
| `dimensions` | list of dimensions | The group's axes, as one dimension for each direction the input moves in. A stick has two. |

Each dimension is one signed axis, split into its two halves:

| Field | Type | Description |
|---|---|---|
| `positive` | input ID | One half of the axis. |
| `negative` | input ID, optional | The opposite half. Leave it out for an axis that only moves one way, like a trigger. |

An axis can be in only one group, and only once in that group.

The player sets each group's deadzone in the controller's settings, under **Hardware**, **Deadzones**. The setting
is stored by group ID, so a group named `controlify:left_stick` or `controlify:right_stick` shares the setting and
the name of the gamepad's own stick. For any other group, add a language key for its name in the deadzone settings:
`controlify.deadzone_group.<namespace>.<path>`.
