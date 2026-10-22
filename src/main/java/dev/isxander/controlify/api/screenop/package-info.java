/// Screen and element sidecars. Register screen factories during
/// ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS and element factories during the
/// per-screen REGISTER_ELEMENT_BEHAVIOURS event. Screen behaviours compose cursor
/// handling, focused-element dispatch and default controls. Focused-element input
/// travels from the deepest element outward before continuing through screen
/// behaviours. Navigation completes before interaction resolves its focus path.
///
/// VirtualMouseMode is a screen-level policy configured through the context's
/// virtual mouse. Live predicates select cursor or focus behaviours without
/// reconstructing their state. Frame updates run before GUI mouse processing;
/// activation notifications reach all behaviours independently of continuations.
///
/// ScreenBehaviours.chain and ElementBehaviours.chain compose an explicit ordered
/// group without assigning priorities to its individual members. Its final next()
/// continues into the enclosing chain. Priorities order independently registered
/// groups; each group controls the order of its own members.
/// Branches confine cancellation to an inner group and then resume the enclosing
/// chain on normal return. Conditional skips a group when its predicate is false;
/// it is sufficient for ordinary visibility and availability checks.
///
/// ElementBehaviours.button and slider supply shared input handling through
/// callbacks, so custom controls can behave consistently with vanilla controls.
///
/// Continuations control propagation within an invocation, not action lifetime.
/// Consume or drain action pulses explicitly when rejecting input.
@NullMarked
package dev.isxander.controlify.api.screenop;

import org.jspecify.annotations.NullMarked;
