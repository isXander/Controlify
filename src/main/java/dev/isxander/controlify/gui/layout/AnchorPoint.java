package dev.isxander.controlify.gui.layout;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public enum AnchorPoint {
    TOP_LEFT(0, 0),
    TOP_CENTER(0.5f, 0),
    TOP_RIGHT(1, 0),
    CENTER_LEFT(0, 0.5f),
    CENTER(0.5f, 0.5f),
    CENTER_RIGHT(0.5f, 1),
    BOTTOM_LEFT(0f, 1f),
    BOTTOM_CENTER(0.5f, 1f),
    BOTTOM_RIGHT(1f, 1f);

    public final float anchorX, anchorY;

    AnchorPoint(float anchorX, float anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    public Vector2i getAnchorPosition(Vector2ic windowSize) {
        return new Vector2i((int) (windowSize.x() * anchorX), (int) (windowSize.y() * anchorY));
    }
}
