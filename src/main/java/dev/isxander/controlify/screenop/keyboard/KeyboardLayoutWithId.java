package dev.isxander.controlify.screenop.keyboard;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a keyboard layout with an associated ID.
 * Layouts themselves do not have IDs, but this record
 * couples a layout with its resource pack ID.
 * @param layout the keyboard layout
 * @param id the resource location ID of the layout
 */
public record KeyboardLayoutWithId(KeyboardLayout layout, ResourceLocation id) {
}
