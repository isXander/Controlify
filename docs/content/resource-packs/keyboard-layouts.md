---
title: Keyboard Layouts
---

# Keyboard Layouts

A keyboard layout is a data file that sets the rows and keys of Controlify's on-screen keyboard. A resource pack
can change the built-in layouts, translate them for other languages, or add new layouts that other layouts switch
to.

## Where layouts go

A layout lives at `assets/<namespace>/controller/keyboard_layout/<layout>/<language>.json`, and its ID is
`<namespace>:<layout>`. `<layout>` is a single folder name, and `<language>` is a Minecraft language code like
`en_us` or `fr_fr`.

Controlify picks the file for the player's current language. If there isn't one, it uses `en_us.json`. If the
layout doesn't exist at all, or its file failed to load, the keyboard shows a built-in QWERTY layout instead. A file
that fails to load is skipped, with an error in the log.

Controlify has three built-in layouts:

| Layout | Used for |
|---|---|
| `controlify:full` | The chat screen |
| `controlify:simple` | Other text boxes, and sign editing |
| `controlify:server_ip` | The server address box |

To replace a built-in layout, add a file at the same path, like
`assets/controlify/controller/keyboard_layout/simple/en_us.json`. To translate one, add a file for your language
next to it, like `assets/controlify/controller/keyboard_layout/simple/fr_fr.json`.

## File format

```json
{
  "width": 10,
  "keys": [
    ["q", "w", "e", "r", "t", "y", "u", "i", "o", "p"],
    ["a", "s", "d", "f", "g", "h", "j", "k", "l", [".", ","]],
    ["z", "x", "c", "v", "b", "n", "m", ["-", "_"], { "regular": { "action": "enter" }, "width": 2 }]
  ]
}
```

| Field | Description |
|---|---|
| `width` | The width of the keyboard, in key units. At least `1`. |
| `keys` | A list of rows, drawn from top to bottom. Each row is a list of [keys](#keys), drawn from left to right. |

::: warning
The widths of the keys in every row must add up to `width` exactly. If any row doesn't, the file doesn't load.
:::

## Keys

A key has a regular function and a shifted function, used while Shift is on. It can be written in three ways:

| Form | Example | Shifted function |
|---|---|---|
| A [key function](#key-functions) | `"a"` | Made from the regular one |
| A `[regular, shifted]` pair | `["1", "?"]` | The second function |
| An object | `{ "regular": "b", "width": 2 }` | `shifted`, or made from the regular one |

The object form takes these fields:

| Field | Default | Description |
|---|---|---|
| `regular` | required | The [key function](#key-functions) used while Shift is off. |
| `shifted` | made from `regular` | The key function used while Shift is on. |
| `width` | `1` | The key's width, in key units. At least `0.1`. |
| `shortcut` | none | The ID of an [action](../reference/builtin-actions) that also presses this key. Its glyph is shown on the key, and holding the action repeats the key. |
| `identifier` | none | A name used to keep focus on this key when the layout changes. See [Changing layouts](#changing-layouts). |

Only a string key function can make its own shifted version: it uses the upper case of its characters. Every other
function stays the same when shifted, so give it a `shifted` function if it needs one.

To use `shortcut`, `width` or `identifier`, you need the object form, even for a single function. A field next to a
key function, like `{ "action": "enter", "width": 2 }`, is ignored.

## Key functions

A key function says what a key does and what it shows. There are four kinds, and a row can mix them.

### Insert characters

Types its characters into the text box.

| Form | Example |
|---|---|
| A string | `"a"` |
| An object | `{ "chars": " ", "display_name": "Space" }` |

`display_name` is optional. Without it, the key shows its characters. It's a [text component](#display-names).

### Key codes

Sends one or more key presses, using [GLFW key codes](https://www.glfw.org/docs/latest/group__keys.html). A key
code stands for a physical key, not a character: `257` is Enter, and `259` is Backspace.

```json
{
  "codes": [
    257,
    { "keycode": 259, "scancode": 0, "modifier": 0 }
  ],
  "display_name": "Enter, then Backspace"
}
```

| Field | Description |
|---|---|
| `codes` | A list of key codes, sent in order. Each one is a number, or an object with `keycode` and optional `scancode` and `modifier`, both default `0`. `modifier` takes [GLFW modifier flags](https://www.glfw.org/docs/latest/group__mods.html), like `1` for Shift. |
| `display_name` | Required. The key's label, as a [text component](#display-names). |

Prefer a [special action](#special-actions) for keys it covers, like Enter or Backspace. Its label is already
translated.

### Special actions

Does a built-in job.

```json
{ "action": "shift" }
```

| Action | What it does |
|---|---|
| `shift` | Turns Shift on or off. Shift turns off by itself after the next key that has a different shifted function. Pressing `shift` while Shift lock is on turns both off. |
| `shift_lock` | Turns Shift on, and keeps it on until `shift_lock` is pressed again. |
| `enter` | Presses Enter. |
| `backspace` | Presses Backspace. |
| `tab` | Presses Tab. |
| `left_arrow`<br>`right_arrow`<br>`up_arrow`<br>`down_arrow` | Presses an arrow key. |
| `copy_all` | Copies all the text in the text box. |
| `paste` | Types the clipboard's contents. |
| `previous_layout` | Changes back to the layout that was open before this one. |

The label is the translation key `controlify.keyboard.special.<action>`, like `controlify.keyboard.special.enter`.

A key is disabled when the text box doesn't support what it does. For example, `copy_all` only works in a text
box that supports copying.

### Change layout

Changes the keyboard to another layout.

```json
{ "layout": "my_pack:numbers", "display_name": "123" }
```

| Field | Description |
|---|---|
| `layout` | The ID of the layout to change to. |
| `display_name` | Required. The key's label, as a [text component](#display-names). |

## Changing layouts

When the layout changes, Controlify chooses the key to focus in this order:

1. the key whose `identifier` matches the key that was focused;
2. a key that changes back to the old layout, or a `previous_layout` key;
3. the first key.

When the keyboard first opens, it focuses the key with the `identifier` `initial_focus`, if there is one.

This pair of layouts uses identifiers so focus stays on the same corner key when the player switches:

```json
{
  "width": 2,
  "keys": [
    [
      { "regular": { "layout": "my_pack:numbers", "display_name": "123" }, "identifier": "switch" },
      { "regular": { "action": "backspace" }, "shortcut": "controlify:gui_abstract_action_1" }
    ]
  ]
}
```

```json
{
  "width": 2,
  "keys": [
    [
      { "regular": { "action": "previous_layout" }, "identifier": "switch" },
      "1"
    ]
  ]
}
```

## Display names

A display name is a [text component](https://minecraft.wiki/w/Text_component_format#Java_Edition). A plain string
works, as do objects:

```json
"Enter"
{ "text": "Enter" }
{ "translate": "my_pack.keyboard.enter" }
```

You don't need to translate display names. Each language can have its own layout file.

## Conventions

The built-in layouts use these shortcuts. Follow them so the keyboard behaves the same everywhere:

| Key | Shortcut |
|---|---|
| Backspace | `controlify:gui_abstract_action_1` |
| Space | `controlify:gui_abstract_action_2` |
| Shift | `controlify:gui_abstract_action_3` |
| Enter | `controlify:pause` |

Don't use `controlify:gui_next_tab` or `controlify:gui_prev_tab` as shortcuts. In a text box that has a cursor,
the keyboard uses them to move the cursor.

When you translate a layout, keep the special actions in the same places where you can, and use the most common
layout for the language, like AZERTY for French.

## JSON schema

To get validation in your editor, add this to the top of your layout file:

```json
{
  "$schema": "https://controlify.isxander.dev/schemas/keyboard_layout.json"
}
```
