---
title: Contextual API
---

# Contextual API

Controlify's contextual system powers button guides and adaptive trigger effects. A contextual domain combines:

- a context object supplied at runtime;
- contributors which expose source facts, items, blocks, and entities;
- data-defined facts derived from those values;
- guide and trigger-effect rule sets evaluated against the resulting state.

For the resource formats, see [Button Guides](../resource-packs/guides) and
[Adaptive Trigger Effects](../resource-packs/adaptive-trigger-effects).

## Accessing built-in domains

Domains are available during Controlify pre-initialization:

```java
private ContextualDomain<InGameContext> inGameDomain;

@Override
public void onControlifyPreInit(PreInitContext context) {
    this.inGameDomain = context.contextualDomains().inGame();
}
```

Controlify provides `inGame()` and `container()` domains.

## Contributing state

A contributor writes values into named slots. Resource-pack facts and rules can then test those slots with
Minecraft's standard predicates.

```java
@Override
public void onControlifyPreInit(PreInitContext context) {
    context.contextualDomains().inGame().registerContributor((inGame, sink) -> {
        sink.contributeFact(
            Identifier.fromNamespaceAndPath("example", "holding_wand"),
            inGame.player().getMainHandItem().is(EXAMPLE_WAND)
        );

        sink.contributeItem(
            Identifier.fromNamespaceAndPath("example", "focus_item"),
            inGame.player().getOffhandItem()
        );
    });
}
```

`ContextualStateSink` can contribute booleans with `contributeFact`, or values with `contributeItem`,
`contributeBlock`, and `contributeEntity`. Contributors run in registration order; a later contribution to the
same identifier replaces the earlier value.

## Creating a domain

Custom domains use a context type implementing `Context`:

```java
public record SpellContext(
    ControllerEntity controller,
    GuideVerbosity verbosity,
    ItemStack selectedSpell
) implements Context {}
```

Register the domain and its contributor during pre-initialization:

```java
private ContextualDomain<SpellContext> spellDomain;

@Override
public void onControlifyPreInit(PreInitContext context) {
    Identifier id = Identifier.fromNamespaceAndPath("example", "spells");
    this.spellDomain = context.contextualDomains().register(id, (spell, sink) -> {
        sink.contributeItem(
            Identifier.fromNamespaceAndPath("example", "selected_spell"),
            spell.selectedSpell()
        );
    });
}
```

Controlify automatically registers the domain's fact resource loader. Rules for the domain use the same domain
identifier in their asset path.

## Using a guide instance

Create the instance once, update it when contextual state may have changed, and render it independently:

```java
GuideInstance<SpellContext> guide = spellDomain.createGuideInstance(minecraft.font);

void tick(SpellContext context) {
    guide.update(context);
}
```

Use `extractRenderState` for HUD-style rendering, or `renderable` when adding the guide to a screen.

## Using a trigger-effect instance

Trigger-effect instances are evaluated in the same way:

```java
TriggerEffectInstance<SpellContext> effects = spellDomain.createTriggerEffectInstance();

void tick(SpellContext context) {
    effects.update(context);
    DualsenseTriggerEffect left = effects.getLeftTriggerEffect();
    DualsenseTriggerEffect right = effects.getRightTriggerEffect();
}
```

The trigger-effect API is experimental. A matching rule only applies when its `for` binding is currently bound to
the corresponding controller trigger.
