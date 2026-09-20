---
title: Button Guides
---

# Button Guides

Button guides show contextual binding hints on the HUD and container screens. The contextual system separates
reusable facts from ordered guide rules.

## Guide rule files

A domain's guide rules are loaded from:

```text
assets/<domain namespace>/contextual/guide/<domain path>.json
```

For Controlify's built-in domains, these are:

- `assets/controlify/contextual/guide/in_game.json`
- `assets/controlify/contextual/guide/container.json`

A rule set contains an optional `replace` flag and an ordered `rules` array:

```json
{
  "replace": false,
  "rules": [
    {
      "for": "controlify:jump",
      "where": "left",
      "if": "controlify:on_ground",
      "then": { "translate": "key.jump" }
    }
  ]
}
```

Each guide rule has:

- `for`: the Controlify binding to display;
- `where`: `left` or `right`;
- `if`: a contextual predicate;
- `then`: a literal string or Minecraft text component.

Use `true` for an unconditional rule.

## Contextual predicates

A fact identifier directly tests a boolean fact:

```json
"if": "controlify:on_ground"
```

Predicates can be combined. `all_of` requires every child, `none_of` rejects any matching child, and `any_of`
requires at least one matching child:

```json
"if": {
  "all_of": [
    "controlify:in_water",
    "controlify:input_moving"
  ],
  "none_of": [
    "controlify:sprinting",
    "controlify:in_vehicle"
  ]
}
```

Rules can also test named item, block, or entity slots with Minecraft's predicate formats:

```json
"if": {
  "slot": "controlify:main_hand",
  "item": {
    "items": "#minecraft:swords"
  }
}
```

```json
"if": {
  "slot": "controlify:hit_result",
  "entity": {
    "type": "#minecraft:villager"
  }
}
```

Replace `item` with `block` or `entity` to select the corresponding predicate type. A missing slot does not
match.

## Rule precedence

Higher-priority resource packs are evaluated before lower-priority packs. Within a file, rules are evaluated in
array order. For each binding and guide location, the first matching rule wins.

Set `replace` to `true` to discard all guide-rule layers below that file:

```json
{
  "replace": true,
  "rules": []
}
```

An invalid file layer is logged and skipped without aborting the resource reload.

## Defining facts

Data-defined facts for a domain use:

```text
assets/<domain namespace>/contextual/facts/<domain path>.json
```

The file is a map from fact identifiers to contextual predicates:

```json
{
  "example:holding_sword": {
    "slot": "controlify:main_hand",
    "item": {
      "items": "#minecraft:swords"
    }
  },
  "example:ready_to_attack": {
    "all_of": [
      "example:holding_sword",
      "controlify:looking_at_entity"
    ]
  }
}
```

Facts may reference other data-defined facts. Controlify orders their evaluation automatically and rejects cycles.
Mods can expose additional source facts and slots through the [Contextual API](../developers/contextual-api).

## Built-in domain state

Every domain exposes these source facts:

- `controlify:verbosity_full`
- `controlify:verbosity_reduced`
- `controlify:verbosity_minimal`

The `controlify:in_game` domain exposes these slots:

- entity `controlify:self`;
- entity or block `controlify:hit_result`;
- items `controlify:main_hand`, `controlify:off_hand`, and `controlify:active_item`.

It also provides source or data-defined facts including:

- `controlify:on_ground`, `controlify:in_vehicle`, `controlify:flying`, `controlify:creative_flying`,
  `controlify:elytra_flying`;
- `controlify:riding_saddled_horse`, `controlify:riding_saddled_equine`,
  `controlify:riding_saddled_camel`, `controlify:riding_saddled_nautilus`, and
  `controlify:riding_happy_ghast`;
- `controlify:can_elytra_fly`, `controlify:in_liquid`, `controlify:in_water`, `controlify:under_water`;
- `controlify:sneaking`, `controlify:sprinting`, `controlify:input_moving`, `controlify:on_climbable`;
- `controlify:is_toggle_sneak`, `controlify:is_toggle_sprint`;
- `controlify:is_spectator`, `controlify:is_creative`, `controlify:is_survival`;
- `controlify:looking_at_entity`, `controlify:looking_at_block`, `controlify:looking_at_air`;
- `controlify:looking_at_villager`, `controlify:looking_at_merchant`,
  `controlify:looking_at_rideable`, `controlify:looking_at_chest_boat`, and
  `controlify:looking_at_container_minecart`;
- `controlify:looking_at_closed_openable`, `controlify:looking_at_open_openable`,
  `controlify:looking_at_container`, `controlify:looking_at_button`, `controlify:looking_at_lever`,
  `controlify:looking_at_bell`, and `controlify:looking_at_bed`;
- workstation target facts named `controlify:looking_at_crafting_table`, `controlify:looking_at_furnace`,
  `controlify:looking_at_smoker`, `controlify:looking_at_brewing_stand`,
  `controlify:looking_at_enchanting_table`, `controlify:looking_at_smithing_table`,
  `controlify:looking_at_repair_station`, `controlify:looking_at_stonecutter`,
  `controlify:looking_at_loom`, and `controlify:looking_at_cartography_table`;
- `controlify:has_item_in_main_hand`, `controlify:has_item_in_offhand`;
- `controlify:has_item_in_either_hand`, `controlify:has_multiple_items_in_main_hand`,
  `controlify:main_hand_is_tool`;
- `controlify:main_hand_is_block_item`, `controlify:off_hand_is_block_item`, and
  `controlify:fishing_hook_cast`.

The built-in in-game guide uses the verbosity facts as tiers. Minimal includes movement and contextual attack/use
actions, reduced adds sprint, inventory, drop, and swap-hands hints, and full additionally includes radial-menu,
drop-stack, and pick-block hints.

The `controlify:container` domain exposes item slots `controlify:hovering_slot`, `controlify:holding_item`, and
`controlify:bundle_selected`. Its built-in facts include:

- `controlify:hovering_slot`, `controlify:hovering_item`, `controlify:hovering_many_items`;
- `controlify:holding_item`, `controlify:holding_many_items`;
- `controlify:hovering_item_is_bundle`, `controlify:selected_bundle_slot`;
- `controlify:can_place_held_item`, `controlify:can_pickup_slot`, `controlify:cursor_outside_container`.
