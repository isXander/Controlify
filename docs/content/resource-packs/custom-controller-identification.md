---
title: Custom Controller Identification
---

# Custom Controller Identification

Controlify identifies a controller by its USB vendor ID (VID) and product ID (PID). Each VID and PID pair belongs to
a **controller type**, and the type's ID, also called its namespace, decides:

- the name shown for the controller;
- its icon in Controlify's settings, a GUI sprite at `assets/<namespace>/textures/gui/sprites/controllers/<path>.png`;
- its [default bindings](../developers/actions#default-bindings);
- its [input glyph set](input-glyphs).

A type can also give the controller a [joystick mapping](joystick-mappings), or change how Controlify loads it.

A controller that matches no entry gets the type `controlify:default`.

## Creating the definition

Create the file `assets/controlify/controller/controller_identification.json5` in your resource pack. It must be in
the `controlify` namespace, whatever your pack's own namespace is.

This file is **additive**. Controlify reads the file from every resource pack and combines the entries, so your file
doesn't replace the built-in definitions. When two entries list the same VID and PID, the one from the
higher-priority resource pack is used.

::: info
The file extension must be `.json5`, not `.json`. JSON5 is a superset of JSON, so plain JSON works too. JSON5 is
what lets you write VIDs and PIDs as hex numbers like `0x57e`.
:::

This file adds a type for Switch Pro controllers:

```json5
[
  {
    "friendly_name": "Switch Controller",
    "id": "my_resource_pack:switch",
    "hids": [
      [0x57e, 0x2009] // [VID, PID]
    ]
  }
]
```

The file is a list of entries. Each entry takes these fields:

| Field | Default | Description |
|---|---|---|
| `hids` | required | A list of `[VID, PID]` pairs. Each pair has exactly two numbers, and the list needs at least one. |
| `id` | `controlify:default` | The controller type ID. Use your pack's own namespace for new types, so they don't clash with other packs. |
| `friendly_name` | none | The name shown for the controller. Without it, Controlify shows the name the controller's driver reports. |
| `mapping_id` | none | The ID of a [joystick mapping](joystick-mappings), loaded from `assets/<namespace>/controller/mapping/<path>.json`. A mapping the player has made for that controller takes priority. |
| `force_joystick` | `false` | Treat the controller as a generic joystick, even if SDL recognises it as a gamepad. Its inputs then use the [generic joystick IDs](../reference/builtin-inputs#generic-joystick-inputs). |
| `prevent_loading` | `false` | Don't load the controller at all. |

You don't need a new type to give a controller its own name. Controlify's entry for the Thrustmaster T16000M flight
stick only sets `friendly_name`, so it keeps the `controlify:default` type, glyphs and bindings.

An entry can also reuse an existing type. Controlify maps the 8BitDo SN30 Pro in DInput mode to `controlify:switch`,
so it gets the Switch glyphs and default bindings.

Reload resources (F3 + T) to apply your changes. Connected controllers whose type changed are reloaded.

### Finding your controller's VID and PID

Controlify loads a controller even when no entry matches it. To find its VID and PID:

1. Connect the controller.
2. Open Controlify's **Global Settings** and press **Copy Debug Dump To Clipboard**.
3. Paste the dump somewhere. Under **Connected Controllers**, the `HID:` line for your controller looks like
   `HID[VID=0x057e, PID=0x2009]`.

## Adding to built-in types

To identify more controllers as a built-in type, add an entry with that type's `id`:

```json5
[
  {
    "friendly_name": "Xbox Controller",
    "id": "controlify:xbox",
    "hids": [
      [0x01, 0x10] // an extra controller identified as controlify:xbox
    ]
  }
]
```

Each entry defines the whole type for the HIDs it lists, so set `friendly_name` here too. Without it, these
controllers show the name their driver reports, not the built-in name.

For the built-in types, see [Built-in Controller Namespaces](../reference/builtin-controller-namespaces).
