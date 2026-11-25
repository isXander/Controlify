---
title: Contextual API
---

# Contextual API

_Describe what's happening in the game, so data can react to it._

A **contextual domain** describes one situation, like playing in a world or having a container open, as a set of
**facts** and **slots**. Rule sets read that state to decide what to do:

| Rule set | Decides | Domains |
|---|---|---|
| [Button guides](#button-guides) | Which actions to hint at, and what to call them | `controlify:in_game`, `controlify:container` |
| [Adaptive trigger effects](#adaptive-trigger-effects) | How the DualSense's triggers resist | `controlify:in_game` |
| [Feedback rules](feedback-api#selecting-effects-with-rules) | Which feedback effects play | `controlify:in_game` |

Rules, and the facts they test, are resource files, so resource packs, including a server's resource pack, can
change them without code. Mods add their own state to a domain, so that packs can react to the mod too.

The in-game domain describes the player in a world. The container domain describes a container screen, like an
inventory or chest, including modded ones.

## State

- A **fact** is a boolean, like `controlify:on_ground` or `controlify:sneaking`.
- A **slot** holds an item, block or entity, like `controlify:main_hand` or `controlify:self`, the player.

See [built-in state](#built-in-state) for every fact and slot Controlify provides.

### Predicates

Rules and facts test state with predicates. A fact's ID tests that fact:

```json
"if": "controlify:on_ground"
```

`true` always matches. `all_of` needs every child to match, `any_of` needs at least one, and `none_of` needs none.
One object can use several of them:

```json
"if": {
  "all_of": ["controlify:in_water", "controlify:input_moving"],
  "none_of": ["controlify:sprinting", "controlify:in_vehicle"]
}
```

A slot is tested with Minecraft's item, block or entity predicate format, by giving the slot and an `item`, `block`
or `entity` predicate. A slot that's empty doesn't match.

```json
"if": {
  "slot": "controlify:main_hand",
  "item": { "items": "#minecraft:swords" }
}
```

```json
"if": {
  "slot": "controlify:hit_result",
  "entity": { "type": "#minecraft:villager" }
}
```

Predicates can reference tags and registries that only the server knows, like a server's datapack tags, so a
server's resource pack can use them. Rules update when the server's tags change.

## Adding facts

Add facts for your mod's state wherever they describe a situation. They're reusable: the same fact works in every
rule set that uses the domain, so packs can show button guides, trigger effects and feedback from it.

There are two kinds of fact. A **derived fact** is defined in data, from state the domain already has. A
**source fact** is contributed from code.

### Derived facts

Before writing code, check whether a fact can be derived from what the domain already has. A domain's derived facts
live in `assets/<domain namespace>/controller/contextual/facts/<domain path>.json`, which merges across mods and
resource packs. Each derived fact is a predicate. The player is in the `controlify:self` slot, so the example defines
`examplemod:in_boat` from the player's vehicle:

<<< @/../../src/example/resources/assets/controlify/controller/contextual/facts/in_game.json

Derived facts can use other facts in their predicates. Controlify works out the order to evaluate them in, and rejects
facts that depend on themselves.

### Source facts

For state only your mod knows, contribute a source fact from code, reading it from your mod's own logic. Only the
example knows whether a boat is boosting, since its boost can run out, so it contributes `examplemod:boosting`:

<<< @/../../src/example/java/dev/isxander/controlify/example/contextual/BoatContext.java#contributor

`BuiltinContextualDomains` has the in-game and container domains. Register contributors in
`ContextualEvents.REGISTER_DOMAINS`, subscribed from your [entrypoint](controlify-entrypoint). A contributor
receives the domain's context, such as `InGameContext` with the player, level and controller, and writes to a
`ContextualStateSink`:

| Method | Contributes |
|---|---|
| `contributeFact(id, value)` | A fact. `contributeFact(id)` contributes `true`. |
| `contributeItem(slot, stack)` | An item slot |
| `contributeBlock(slot, block)` | A block slot |
| `contributeEntity(slot, entity)` | An entity slot |

Contributors run in the order they were registered. A later contribution to the same ID replaces an earlier one.

Contribute a slot rather than a fact when packs should be able to test its properties, such as which item or
entity it is.

## Button guides

Button guides show what actions do: on the HUD in game, and on container screens. Each guide rule names an action
and what to call it, when its predicate matches. A domain's guide rules live in
`assets/<domain namespace>/controller/contextual/guide/<domain path>.json`, which merges across mods and resource
packs. The example shows its horn and boost while the player is in a boat:

<<< @/../../src/example/resources/assets/controlify/controller/contextual/guide/in_game.json

| Field | Meaning |
|---|---|
| `for` | The action to show, with its current input |
| `where` | `left` or `right` |
| `if` | A predicate |
| `then` | The text, as a string or text component |

Higher-priority resource packs' rules are checked first, then each file's rules in order. For each action and side,
the first rule that matches wins. Set `"replace": true` in a file to ignore every lower-priority pack's rules:

```json
{
  "replace": true,
  "rules": []
}
```

The player chooses how much the guide shows in the controller settings. Rules can test it with the
`controlify:verbosity_minimal`, `controlify:verbosity_reduced` and `controlify:verbosity_full` facts. Each is true
at its level and above, so a rule for `verbosity_reduced` shows at reduced and full. The example only shows its
boost at reduced and above.

## Adaptive trigger effects

Adaptive trigger effects set how a DualSense's triggers resist or vibrate, depending on the game. They only work
on DualSense controllers, and only in the in-game domain. Their rules live in
`assets/controlify/controller/contextual/trigger_effect/in_game.json`. The example makes the throttle stiffen the
further it's pulled, while the player is in a boat:

<<< @/../../src/example/resources/assets/controlify/controller/contextual/trigger_effect/in_game.json

A rule only applies while its `for` action is bound to the left or right trigger.

The trigger's motor takes time to move into place, so a rule should match when the player is *able* to do
something, not once they've started. The example stiffens the throttle as soon as the player is in a boat, rather
than once they start accelerating. For the same reason, rules for using an item test `controlify:active_item`
first, then `controlify:off_hand`. `active_item` is the item being used, or the main hand item when nothing is, so
this matches the order in which using an item tries each hand. Rules are checked in the same
order as button guide rules, and the first that matches for an action wins. Use `"type": "off"` to stop rules below
it from applying, and `"replace": true` to ignore lower-priority packs.

### Effect types

Positions are the trigger's ten zones, `0` to `9`. Strengths and amplitudes go from `0`, off, to `8`, the maximum.

| Type | Fields |
|---|---|
| `off` | None |
| `feedback` | `position`: 0–9<br>`strength`: 0–8<br>Constant resistance from that position. |
| `weapon` | `start_position`: 2–7<br>`end_position`: after the start, up to 8<br>`strength`: 0–8 |
| `vibration` | `position`: 0–9<br>`amplitude`: 0–8<br>`frequency`: in hertz, 1–127 |
| `feedback_multiple_position` | `strength`: ten values, 0–8, one per zone |
| `feedback_slope` | `start_position`: 0–8<br>`end_position`: after the start, up to 9<br>`start_strength`, `end_strength`: 1–8 |
| `vibration_multiple_position` | `frequency`: in hertz, 1–127<br>`amplitude`: ten values, 0–8, one per zone |

An effect whose strengths or amplitudes are all 0, or a vibration with no frequency, is off.

Put all of your active-item rules before your off-hand ones, as Controlify's built-in rules do, so the item that
would be used wins.

## Feedback rules

Feedback rules select which feedback effects play, from the in-game domain. They're covered with the rest of
feedback, in [Selecting effects with rules](feedback-api#selecting-effects-with-rules).

## Custom domains

Create your own domain for a situation the built-in ones don't describe, like the container domain does for
container screens, but for your mod's own UI. A custom domain can have button guides. The example adds one for its
boat settings screen, which shows what pressing does on the focused field.

A domain's context is a type of your own, with the state its contributors read. It also returns the player's guide
verbosity, from their controller:

<<< @/../../src/example/java/dev/isxander/controlify/example/contextual/BoatSettingsContext.java#context

Register the domain in `ContextualEvents.REGISTER_DOMAINS`, with a contributor that turns the context into facts and
slots:

<<< @/../../src/example/java/dev/isxander/controlify/example/contextual/BoatSettingsDomain.java#register

The domain's facts and rules use its ID in their paths, so the example's guide rules live in
`assets/examplemod/controller/contextual/guide/boat_settings.json`:

<<< @/../../src/example/resources/assets/examplemod/controller/contextual/guide/boat_settings.json

Create a `GuideInstance` from the domain, update it with a context, and render it. The example does this in a
[screen behaviour](screen-operation-api#screen-behaviours), registered alongside the screen's other behaviours:

<<< @/../../src/example/java/dev/isxander/controlify/example/screen/BoatSettingsGuideBehaviour.java#guide-behaviour

`extractRenderState` draws the guide yourself, as the example does. `renderable` returns a `Renderable` that does the
same, for adding to a screen. Use whichever suits your UI. Both take whether to align the guide to the bottom of the
screen, and whether to draw a background behind its text.

## Built-in state

Every domain has the verbosity facts: `controlify:verbosity_full`, `controlify:verbosity_reduced` and
`controlify:verbosity_minimal`.

### In game

| Slot | Holds |
|---|---|
| `controlify:self` | The player, an entity |
| `controlify:hit_result` | The entity or block the player is looking at |
| `controlify:main_hand`, `controlify:off_hand` | The items in each hand |
| `controlify:active_item` | The item being used, or the main hand item when nothing is |

Facts:

- Movement: `on_ground`, `in_vehicle`, `flying`, `creative_flying`, `elytra_flying`, `can_elytra_fly`,
  `on_climbable`, `on_slow_block`, `in_liquid`, `in_water`, `under_water`, `sneaking`, `sprinting`,
  `input_moving`, `is_toggle_sneak`, `is_toggle_sprint`
- Riding: `riding_saddled_horse`, `riding_saddled_equine`, `riding_saddled_camel`, `riding_saddled_nautilus`,
  `riding_happy_ghast`
- Game mode: `is_spectator`, `is_creative`, `is_survival`, `has_hearts`
- Hands: `has_item_in_main_hand`, `has_item_in_offhand`, `has_item_in_either_hand`,
  `has_multiple_items_in_main_hand`, `main_hand_is_tool`, `main_hand_is_block_item`, `off_hand_is_block_item`,
  `fishing_hook_cast`
- Using an item: `use_animation/<animation>` for each use animation, like `use_animation/bow`,
  `use_animation/eat` or `use_animation/spyglass`, true for the animation of the item being used
- Looking at: `looking_at_entity`, `looking_at_block`, `looking_at_air`, `looking_at_villager`,
  `looking_at_merchant`, `looking_at_rideable`, `looking_at_chest_boat`, `looking_at_container_minecart`,
  `looking_at_closed_openable`, `looking_at_open_openable`, `looking_at_container`, `looking_at_button`,
  `looking_at_lever`, `looking_at_bell`, `looking_at_bed`, `looking_at_crafting_table`, `looking_at_furnace`,
  `looking_at_smoker`, `looking_at_brewing_stand`, `looking_at_enchanting_table`, `looking_at_smithing_table`,
  `looking_at_repair_station`, `looking_at_stonecutter`, `looking_at_loom`, `looking_at_cartography_table`

All of them are in the `controlify` namespace.

### Container

| Slot | Holds |
|---|---|
| `controlify:hovering_slot` | The item in the slot under the cursor |
| `controlify:holding_item` | The item held on the cursor |
| `controlify:bundle_selected` | The item selected in a hovered bundle |

Facts: `hovering_slot`, `hovering_item`, `hovering_many_items`, `holding_item`, `holding_many_items`,
`hovering_item_is_bundle`, `selected_bundle_slot`, `can_place_held_item`, `can_pickup_slot`,
`cursor_outside_container`, all in the `controlify` namespace.
