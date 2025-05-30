/**
 * Achieves a splitscreen effect by creating a parent window and making the
 * vanilla window of each client a child of that parent window.
 * Win32 has special window styling properties which makes these child windows appear like they are
 * not windows at all, and they're positioned relative to the parent.
 * It's impossible to tell they're multiple windows when done well.
 */
package dev.isxander.splitscreen.client.engine.impl.reparenting;
