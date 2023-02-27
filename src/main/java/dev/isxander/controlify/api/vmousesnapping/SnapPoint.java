package dev.isxander.controlify.api.vmousesnapping;

import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * Defines a point on the screen that the virtual mouse can snap to.
 *
 * @param position the position on the screen where the cursor will snap to
 * @param range how far away from the snap point the cursor can be and still snap to it
 */
public record SnapPoint(Vector2ic position, int range) {
    public SnapPoint(int x, int y, int range) {
        this(new Vector2i(x, y), range);
    }
}
