---
title: Screen Operation API
---

# Screen Operation API

_Make your mod's screens work with a controller._

Controlify already works with most screens. On any screen it moves focus between widgets, presses buttons, adjusts
sliders, goes back, switches tabs, and opens the on-screen keyboard for text boxes. Players can also turn on a
virtual mouse cursor for screens that don't work with focus.

You add to this with **behaviours**: classes that Controlify attaches to screens or to the elements on them, matched
by class. Because they're matched by class, you can add behaviours to any screen or element, including vanilla ones
and other mods' ones, without a mixin.

Behaviours only run while Controlify is in use. That makes them the place for controller-only parts of your UI too:
shortcuts using the GUI abstract actions, [button guides](#button-guides), or rendering a
[contextual guide](contextual-api#custom-domains).

This page adds controller support to the example addon's boat settings screen, which has a custom text field, a
custom slider and a reset button. None of the screen's own classes reference Controlify; the
[entrypoint](controlify-entrypoint) registers the behaviours.

## Element behaviours

An element behaviour handles one element, such as a widget. Most mods need these more than screen behaviours: any
custom element that Controlify doesn't know how to use, like a slider that isn't a vanilla slider, needs one.

When no behaviour handles a press, Controlify sends Enter to the screen. So a custom widget that already activates
on Enter works without one.

Register element behaviours in `ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS`. It fires for each screen, with that
screen's context, so you can give the same element class different behaviours on different screens:

<<< @/../../src/example/java/dev/isxander/controlify/example/screen/BoatSettingsBehaviours.java#register-elements

A registration matches its class and every subclass. The factory runs the first time Controlify needs the element's
behaviour, and that behaviour instance is kept for as long as the element is on the screen.

### Using a built-in behaviour

`ElementBehaviours` has factories for the common kinds of control. Give them a callback into your widget.

Build your elements' and screens' support out of these wherever you can, instead of one behaviour that handles
everything itself. Then Controlify implements the standard controls, and your slider works the same as every other
slider, in every mod.

| Factory | Controller handling |
|---|---|
| `button(press)` | Press activates it, with a click sound. |
| `slider(adjust)` | Press starts editing. While editing, left and right call `adjust` with `-1` or `+1`, and back stops editing. Secondary left and right adjust it without editing. |
| `slider(adjust, isEditing, setEditing)` | The same, using editing state that your widget already has, so it can render it. |
| `secondarySlider(adjust)` | Secondary left and right adjust it. Press still activates it normally. |
| `cycle(cycle)` | Press, or secondary left and right, step through options. |

These don't check whether the widget is active or visible. Wrap a factory in `enabledWidget` to skip the widget
while it's inactive or hidden, as the example does.

### Text input

Every element that takes text input needs a keyboard behaviour, so that players can type into it with the on-screen
keyboard. Controlify provides one for `EditBox` and chat. Any other text field, like the example's name field, needs
its own:

<<< @/../../src/example/java/dev/isxander/controlify/example/screen/NameFieldBehaviour.java#keyboard

When the player presses on the focused element, Controlify opens the on-screen keyboard with the
`ElementKeyboardBehaviour` you return, if the player has the on-screen keyboard turned on.

- `Handled(layout, inputTarget, positioner)` opens it. The layout is one of `KeyboardLayouts`: `FULL`, `SIMPLE` or
  `SERVER_IP`, or [your own](../resource-packs/keyboard-layouts). The positioner places it on the screen.
- `DoNothing()` stops the keyboard from opening, for an element that already provides its own way to type.
- `Optional.empty()` leaves the decision to the element's other behaviours.

The `InputTarget` receives what the player types. Implement the parts your element supports:

<<< @/../../src/example/java/dev/isxander/controlify/example/screen/NameFieldBehaviour.java#input-target

| Capability | Methods |
|---|---|
| Characters | `supportsCharInput`, `acceptChar` |
| Keys, like backspace and enter | `supportsKeyCodeInput`, `acceptKeyCode` |
| Copying | `supportsCopying`, `copy` |
| Moving the cursor | `supportsCursorMovement`, `moveCursor` |

### Writing your own

Implement `ElementBehaviour` for what no built-in behaviour covers. Keep it to that one job and register it
alongside the built-in ones, rather than replacing them. Every method has a default, so override only what you need:

| Method | Called |
|---|---|
| `handleNavigation(screen, controller, next)` | With directional input, before interaction. Focused elements receive it from the deepest one outwards, before the screen does. |
| `handleInteraction(screen, controller, next)` | With every other input, such as pressing the element. |
| `onFocusChanged(screen, focused)` | When the element gains or loses focus. |
| `keyboardBehaviour(screen)` | When Controlify needs to know whether the element takes text input. |
| `collectSnapPoints(screen, output, next)` | When the [virtual mouse](#virtual-mouse) gathers points to snap to. |

## Screen behaviours

A screen behaviour handles things that belong to the whole screen, like shortcuts and button guides. Register it
in `ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS`:

<<< @/../../src/example/java/dev/isxander/controlify/example/screen/BoatSettingsBehaviours.java#register-screen

Java can't infer the screen type when you pass a constructor reference, so give it explicitly, like
`registry.<BoatSettingsScreen>register`.

The factory runs once for each screen instance, and receives its `ScreenContext`. Keep the context: it gives you
the screen, its virtual mouse, its focus, sounds and button guides. The behaviour is kept while the screen is open,
including when its widgets are rebuilt and when the player returns to it from another screen.

The example resets the settings with the first GUI abstract action, and shows that on the reset button:

<<< @/../../src/example/java/dev/isxander/controlify/example/screen/BoatSettingsScreenBehaviour.java#screen-behaviour

`BuiltinActions.GUI_ABSTRACT_ACTION_1` to `3` are general-purpose screen actions, for shortcuts like this one. Each
screen decides what they do.

| Method | Called |
|---|---|
| `handleNavigation(controller, next)` | With directional input, before interaction. |
| `handleInteraction(controller, next)` | With every other input, after navigation. |
| `onWidgetRebuild(next)` | After the screen creates its widgets, including when it's resized. |
| `extractRenderState(graphics, tickDelta, controller, next)` | When the screen renders, to draw controller-only content. |
| `update(delta, next)` | Every frame, for smooth movement or animation. |
| `collectSnapPoints(output, next)` | When the [virtual mouse](#virtual-mouse) gathers points to snap to. |
| `onScreenActiveChanged(active)` | When the screen becomes, or stops being, the current screen. |

Navigation and interaction run on each tick. Navigation runs first, and interaction then uses the focus that
navigation left behind.

### Button guides

A button guide shows an action's button next to a widget. Add guides in `onWidgetRebuild`, because the screen
creates new widgets each time it rebuilds them.

The last argument, a `ButtonGuidePredicate`, decides when the guide shows. `ButtonGuidePredicate.always()` and
`focusOnly()` cover the common cases. It's a functional interface, so you can also pass a lambda that takes the
widget, like the example's, which only shows the guide while there are settings to reset.

## Chains

Every behaviour that matches a screen or element runs, in a chain. Each method that takes `next` passes the call on
to the next behaviour when you call `next.run()`. Leave it out to stop the rest of the chain, for that call only:

- Stopping navigation doesn't stop interaction, and stopping interaction doesn't undo navigation.
- Stopping the chain doesn't consume any action's pulses. Consume them yourself when you handle an action.
- Call `next` at most once, during the method. Code after `next.run()` runs after the rest of the chain.

Behaviours run from the highest priority to the lowest. Within a priority, more specific classes run first, then
newer registrations. Leave the priority at its default of `0` unless you need to run before or after a particular
behaviour. Controlify registers its own as follows:

| Priority | Screen behaviours | Element behaviours |
|---|---|---|
| `100` | The virtual mouse, focus, and passing input to the focused elements | Skipping snap points of hidden or inactive widgets |
| `0` | Behaviours for specific screens | Behaviours for specific elements |
| `-100` | Pressing the focused element, switching tabs and going back | Passing input to a container's children |
| `-200` | | The centre snap point of every widget |

To combine several behaviours yourself, `ScreenBehaviours` and `ElementBehaviours` provide:

- `chain(...)` runs behaviours in a fixed order, without giving each its own priority.
- `conditional(condition, behaviour)` skips a behaviour while the condition is false.
- `branch(behaviour)` limits leaving out `next` to the behaviour inside it. Use it for a group of behaviours that
  are meant to stop each other: without `branch`, one of them leaving out `next` also stops every behaviour after
  the group.

## Virtual mouse

Each screen has a virtual mouse, with a mode that decides what it does:

| Mode | Cursor |
|---|---|
| `DEFAULT` | Off, unless the player turns it on for this screen. |
| `ENABLED` | On. It moves, snaps, scrolls and clicks. |
| `DISABLED` | Off. |
| `CURSOR_ONLY` | On, but it only moves and snaps. Your behaviours handle clicks. |
| `CURSOR_SCROLL` | Like `CURSOR_ONLY`, but it also scrolls. |

Leave screens on `DEFAULT` when they work with focus. The player's toggle exists for modded screens that don't.
Choosing any other mode removes that toggle for the screen. Container screens use `CURSOR_SCROLL`: the cursor moves
between slots, and their behaviours decide what pressing does.

Set the mode in your screen behaviour factory, with `context.virtualMouse().setMode(...)`. When the cursor doesn't
click for you, `virtualMouse().emulateClicks(controller)` performs mouse clicks from the cursor's actions.

The cursor snaps to snap points. Every widget already has one at its centre. Add more in `collectSnapPoints`, for
targets that aren't widgets, such as inventory slots.
