---
title: Feedback Effects
---

# Feedback Effects

Feedback is everything the player feels through the controller: rumble, trigger rumble and HD haptics. A resource pack
can change what any feedback feels like, add feedback for new situations, and decide which strength slider controls it.

There are three kinds of file:

| File | What it decides |
|------|-----------------|
| `assets/<namespace>/controller/feedback/<path>.json` | What an effect feels like |
| `assets/<namespace>/controller/contextual/feedback/in_game.json` | Which effect plays, and when |
| `assets/controlify/controller/feedback_sources.json` | Which strength slider controls each event |

## Events and effects

An **event** is something that happens: the player takes damage, an explosion goes off, a bow is being drawn.
An **effect** is what that feels like.

By default an event plays the effect with the same id. The event `controlify:explosion` plays the effect at
`assets/controlify/controller/feedback/explosion.json`. To change how an explosion feels, a pack only needs to provide
that file.

Events either **fire** once (damage, an explosion) or are **held** for as long as something is true (drawing a bow,
walking through soul sand).

## Effect files

An effect is a list of tracks. Each track drives one output over time.

```json
{
  "tracks": [
    { "band": "low",  "envelope": [[0, 1], [0.1, 1], [0.1, 0.8], [0.5, 0, "out_quad"]], "scale": "power" },
    { "band": "high", "level": 0.2 }
  ]
}
```

The strength of a track at any moment is:

```text
strength = envelope × level × lfo × scale
```

Every part is optional and counts as 1 when left out. Strengths are from 0 to 1.

### What a track drives

A track has exactly one of these:

| Field | Meaning |
|-------|---------|
| `"band": "low"` | Heavy, low rumble. Works on every controller. |
| `"band": "high"` | Light, high buzz. Works on every controller. |
| `"hz": 120` | A tone at that frequency. Controllers without HD haptics fold it into the nearest band. |
| `"click": { "sharpness": 0.7 }` | A single transient. `sharpness` 0 is a dull thud, 1 is a crisp tick. |

and may say where it is felt with `"site"`:

| Site | Where |
|------|-------|
| `body` (default) | The whole controller |
| `left`, `right` | One grip. Ordinary rumble motors cannot tell these apart and play both. |
| `left_trigger`, `right_trigger` | The motors inside the triggers, on controllers that have them |

### `envelope`

A list of `[seconds, strength]` points. The strength moves in a straight line from each point to the next.

```json
"envelope": [[0, 0], [0.5, 1], [2, 0]]
```

This rises to full strength over half a second, then fades out over the next second and a half.

- Two points at the same time make a sudden step.
- A third value eases the line arriving at that point: `linear`, `in_quad`, `out_quad`, `in_out_quad`, `in_sine`
  or `out_expo`.
- After the last point, the last strength is kept.

### `level`

A constant strength. `"level": 0.3` on its own is a steady rumble at 30%.

### `lfo`

A repeating wave that the strength is multiplied by. Use it for anything that pulses.

```json
"lfo": { "period": 0.35, "on": 0.1 }
```

| Field | Meaning |
|-------|---------|
| `period` | Seconds for one full cycle |
| `shape` | `square` (default), `saw`, `triangle` or `sine` |
| `on` | For `square`: seconds of each cycle spent at `max`. Defaults to half the period. |
| `min`, `max` | The range the wave swings between. Default 0 and 1. |

| Shape | What it does over one period |
|-------|------------------------------|
| `square` | At `max` for `on` seconds, then at `min` |
| `saw` | Rises steadily from `min` to `max`, then drops back |
| `triangle` | Rises steadily from `min` to `max`, then falls steadily back |
| `sine` | Swings smoothly up and back |

The anvil is three thuds, written as a square wave that is on for 0.1 s of every 0.35 s:

```json
{
  "duration": 1.05,
  "tracks": [ { "band": "low", "lfo": { "period": 0.35, "on": 0.1 } } ]
}
```

### `scale`

Events pass numbers along with them, called params. `scale` multiplies a track by one.

```json
"scale": "movement"
```

When the number is not already between 0 and 1, remap it:

```json
"scale": { "param": "destroy_time", "from": [0, 20], "to": [0.02, 0.27], "ease": "in_quad" }
```

The param is clamped to the `from` range and stretched onto the `to` range. Here a block that takes no time to break
gives 0.02, one with a destroy time of 20 or more gives 0.27, and `ease` bends the line between them. `from` and `to`
both default to `[0, 1]`.

A param the event does not have counts as zero, which silences the track.

### `delay`

Seconds before the track starts. It is silent until then, and its envelope and LFO count from that moment. For a
`click`, `delay` is when the click happens.

### `requires`

`"requires": "hd"` plays the track only on controllers with HD haptics, and `"requires": "no_hd"` only on controllers
without. This lets one effect have a detailed version and a simple fallback.

### How long an effect lasts

| How the event plays it | When it ends |
|------------------------|--------------|
| Fired | When its longest envelope (or last click) is over, or after `duration` seconds if given |
| Held | When the event stops being held. It then fades out over `release` seconds (default 0). |

An effect with only constant tracks has no length, so a fired event cannot play it unless it has a `duration`.

## Built-in events

| Event | Fired or held | Params |
|-------|---------------|--------|
| `controlify:incoming_damage` | Fired | |
| `controlify:outgoing_damage` | Fired | |
| `controlify:explosion` | Fired at the blast | `power`: the explosion's power |
| `controlify:lightning` | Fired at the bolt | |
| `controlify:item_break` | Fired | |
| `controlify:anvil` | Fired | |
| `controlify:dragon_death` | Fired | |
| `controlify:wither_spawn` | Fired | |
| `controlify:water_land` | Fired | `impact`: 0 to 1 |
| `controlify:fishing_bite` | Held | |
| `controlify:slow_block` | Held | `movement`: 0 to 1 |
| `controlify:block_break` | Held | `destroy_time`: the block's destroy time |
| `controlify:use_item` | Held | `charge_progress`: 0 to 1 while a crossbow charges |
| `controlify:ui_navigate` | Fired | |

Events fired at a position get weaker with distance on their own. Effects do not need to handle it.

## Choosing which effect plays

Rules live in `assets/<namespace>/controller/contextual/feedback/in_game.json`. They use the same predicates as
[button guides](guides.md) and [adaptive trigger effects](adaptive-trigger-effects.md).

```json
{
  "rules": [
    { "for": "controlify:use_item", "if": "controlify:use_animation/bow", "then": "controlify:use_item/bow" }
  ]
}
```

There are two kinds of rule.

### Rules for an event

A rule with `for` picks the effect an event plays. `controlify:use_item` uses this to feel different for a bow, a
shield and food.

- The first rule for the event whose `if` is true wins.
- If none match, the event plays the effect with its own id.
- `"then": "controlify:none"` plays nothing.

### Rules with no event

A rule without `for` needs no event at all. Its effect is held for as long as `if` is true. This is how a pack adds
feedback for a situation Controlify has no event for.

```json
{ "if": "controlify:on_climbable", "then": "mypack:climbing" }
```

What that feels like depends on the effect:

- An effect with a constant track is felt the whole time the predicate is true.
- An effect whose envelopes end at 0 is felt once, when the predicate becomes true.

These rules are checked once per game tick.

## Strength sliders

`assets/controlify/controller/feedback_sources.json` groups events under the strength sliders in Controlify's settings.

```json
{
  "controlify:world": ["controlify:explosion", "controlify:lightning"],
  "mypack:mounts": ["mypack:climbing"]
}
```

- Every pack's file is merged. Listing an event under a different source moves it.
- A source that is new gets its own slider.
- For a rule with no event, list the effect id.
- Anything not listed is controlled by the master strength only.
