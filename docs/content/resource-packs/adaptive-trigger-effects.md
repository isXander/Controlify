---
title: Adaptive Trigger Effects
---

# Adaptive Trigger Effects

Adaptive trigger effects are contextual rules. Each rule associates a Controlify binding with a contextual predicate
and a DualSense trigger effect.

## Trigger-effect files

A domain's trigger rules are loaded from:

```text
assets/<domain namespace>/contextual/trigger_effect/<domain path>.json
```

The built-in in-game domain uses:

```text
assets/controlify/contextual/trigger_effect/in_game.json
```

A rule set contains an optional `replace` flag and an ordered `rules` array:

```json
{
  "replace": false,
  "rules": [
    {
      "for": "controlify:use",
      "if": {
        "slot": "controlify:active_item",
        "item": {
          "items": "minecraft:bow"
        }
      },
      "then": {
        "type": "feedback_slope",
        "start_position": 3,
        "end_position": 9,
        "start_strength": 2,
        "end_strength": 8
      }
    }
  ]
}
```

Each rule has:

- `for`: the Controlify binding whose physical trigger receives the effect;
- `if`: a [contextual predicate](./guides#contextual-predicates);
- `then`: the trigger effect.

The binding is not restricted to Use Item or Attack. A rule is considered when its binding is currently bound to the
left or right trigger.

## Matching in-game items

The in-game domain provides these item slots:

- `controlify:active_item`: the item currently being used;
- `controlify:main_hand`;
- `controlify:off_hand`.

For an Attack effect, test the main hand:

```json
{
  "for": "controlify:attack",
  "if": {
    "slot": "controlify:main_hand",
    "item": {
      "predicates": {
        "minecraft:weapon": {}
      }
    }
  },
  "then": {
    "type": "weapon",
    "start_position": 3,
    "end_position": 5,
    "strength": 1
  }
}
```

To retain Controlify's Use Item fallback behavior, put all active-item rules first and then repeat them for the
off-hand slot. This makes an active-item match win before any off-hand match.

Item predicates use Minecraft's standard item predicate format and can match item IDs, tags, exact component values,
component predicates, and stack counts:

```json
{
  "for": "controlify:use",
  "if": {
    "slot": "controlify:off_hand",
    "item": {
      "items": "#example:heavy_tools",
      "predicates": {
        "minecraft:custom_data": {
          "example": {
            "trigger_effect": "heavy"
          }
        }
      }
    }
  },
  "then": {
    "type": "feedback_multiple_position",
    "strength": [0, 0, 2, 3, 4, 5, 6, 7, 8, 8]
  }
}
```

Contextual predicates can also combine facts, items, blocks, and entities with `all_of`, `none_of`, and `any_of`.

## Rule precedence and replacement

Higher-priority resource packs are evaluated before lower-priority packs. Rules within a file are evaluated in array
order. The first matching rule for a binding wins.

Set `replace` to `true` to discard every lower-priority layer:

```json
{
  "replace": true,
  "rules": []
}
```

Use an `off` effect when a condition should explicitly disable a lower rule for the same binding:

```json
{
  "for": "controlify:use",
  "if": {
    "slot": "controlify:active_item",
    "item": {
      "items": "minecraft:bow"
    }
  },
  "then": {
    "type": "off"
  }
}
```

An invalid file layer is logged and skipped without aborting the resource reload.

## Server-provided registries and tags

Trigger and fact resources are decoded against the client's static registries, then remapped against the current
world's registry access. This allows a server resource pack to reference tags supplied by the server's datapack:

```json
{
  "rules": [
    {
      "for": "controlify:attack",
      "if": {
        "slot": "controlify:main_hand",
        "item": {
          "items": "#example:heavy_weapons"
        }
      },
      "then": {
        "type": "weapon",
        "start_position": 2,
        "end_position": 8,
        "strength": 6
      }
    }
  ]
}
```

Resolved rules are invalidated whenever client tags update, so datapack `/reload` changes are reflected without a
resource-pack reload.

## Effect formats

Trigger positions refer to the DualSense's ten trigger zones, numbered `0` through `9`. Strength and amplitude
values use `0` for off and `8` for maximum.

| Type                          | Fields                                                                                                                 |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `off`                         | No additional fields.                                                                                                  |
| `feedback`                    | `position`: 0–9; `strength`: 0–8. Applies constant resistance from that position.                                      |
| `weapon`                      | `start_position`: 2–7; `end_position`: greater than the start and at most 8; `strength`: 0–8.                          |
| `vibration`                   | `position`: 0–9; `amplitude`: 0–8; `frequency`: positive signed-byte frequency in hertz.                               |
| `feedback_multiple_position`  | `strength`: exactly 10 values, each 0–8, corresponding to zones 0–9.                                                   |
| `feedback_slope`              | `start_position`: 0–8; `end_position`: greater than the start and at most 9; `start_strength` and `end_strength`: 1–8. |
| `vibration_multiple_position` | `frequency`: positive signed-byte frequency in hertz; `amplitude`: exactly 10 values, each 0–8.                        |

For example:

```json
{
  "type": "vibration_multiple_position",
  "frequency": 20,
  "amplitude": [0, 0, 2, 2, 3, 4, 5, 6, 7, 8]
}
```

Effects whose entire amplitude or strength is zero, and vibration effects whose frequency is not positive, behave as
`off`.
