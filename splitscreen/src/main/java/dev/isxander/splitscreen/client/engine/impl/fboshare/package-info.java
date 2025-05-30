/**
 * A method of splitscreen that involves making pawn clients write their
 * rendered texture to a section of shared memory, instead of to a window.
 * The controller client then syncs all of these frames and renders to its vanilla window.
 * This allows greater flexibility than {@link dev.isxander.splitscreen.client.engine.impl.reparenting} because elements
 * can be rendered on top of all players at once, e.g. menu blur across the splitscreen.
 * <p>
 * The ability to create and use shared memory in this way is not possible in OpenGL. This <strong>incomplete implementation</strong>
 * is written for Vulkan, since there's a chance that Mojang may switch to Vulkan with Vibrant Visuals.
 */
package dev.isxander.splitscreen.client.engine.impl.fboshare;
