/// Composable controller support attached to screens and GUI elements as stateful sidecars.
/// Screen implementations can remain independent of controller-specific classes, while
/// registered factories construct the support that Controlify invokes.
///
/// ## Registration and lifetime
///
/// Subscribe to [ScreenopEvents#REGISTER_SCREEN_BEHAVIOURS] to register explicit screen
/// factories. Each screen context retains its matching behaviours across widget rebuilds
/// and reactivation. After those factories run,
/// [ScreenopEvents#REGISTER_ELEMENT_BEHAVIOURS] supplies the context and its element
/// registry. Element factories run lazily and results are cached by element identity
/// within that context. Widgets may not exist when registration occurs.
///
/// Registrations compose rather than replace one another. Higher priorities run first,
/// then more specific assignable types, then newer registrations. Use
/// [ScreenBehaviours#chain] and [ElementBehaviours#chain] for a group with a fixed internal
/// order instead of giving every member its own priority.
///
/// ## Input stages and continuations
///
/// Every controller fixed update completes navigation before starting interaction.
/// The built-in focused-element dispatcher visits the deepest focused element, then its
/// ancestors, then continues with remaining screen controls. Focus is synchronized
/// between stages, and interaction resolves the updated focus path.
///
/// Each `next` is synchronous and may be called at most once. Omitting it stops only the
/// remaining invocation of that method. Cancelling navigation does not cancel interaction,
/// and neither cancellation nor [ScreenBehaviours#branch] consumes or restores action
/// pulses. Consume or drain conflicting actions explicitly. Changing screens stops further
/// input for the previous screen.
///
/// Branches resume the enclosing chain after the inner callback returns normally, even
/// if it omitted `next`. Conditional wrappers skip their member while a live predicate
/// is false. State survives both forms of composition.
///
/// ## Notifications, keyboard policies, and cursor state
///
/// [ElementBehaviour#onFocusChanged] and [ScreenBehaviour#onScreenActiveChanged] are
/// broadcast notifications, independent of input propagation. The composition helpers
/// forward them to every member, including conditionally disabled members. Keyboard
/// policies instead select the first non-empty result; an explicit
/// [ElementKeyboardBehaviour.DoNothing] suppresses popup activation.
///
/// [ScreenContext] provides the current focus path, feedback, keyboard opening, and the
/// shared [VirtualMouse]. Its [VirtualMouseMode] is a screen policy rather than another
/// middleware instance. Frame updates run before GUI mouse processing and are separate
/// from fixed input updates. [SnapPoint] coordinates are scaled GUI pixels; virtual mouse
/// position getters return unscaled window coordinates.
@NullMarked
package dev.isxander.controlify.api.screenop;

import org.jspecify.annotations.NullMarked;
