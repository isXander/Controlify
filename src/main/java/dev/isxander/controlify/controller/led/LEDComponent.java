package dev.isxander.controlify.controller.led;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

public class LEDComponent implements ECSComponent {
    public static final Identifier ID = CUtil.rl("led");

    private final int[] ledColors;
    private boolean dirty = false;

    public LEDComponent(int ledCount) {
        this.ledColors = new int[ledCount];
    }

    /**
     * Sets the color of all LEDs to a single color.
     * This method updates the internal array of LED colors and marks the component as dirty.
     * @param color the color to set for all LEDs, represented as an RGB format integer (0xRRGGBB).
     */
    public void setAll(int color) {
        Arrays.fill(ledColors, color);
        dirty = true;
    }

    /**
     * Gets the current color of all LEDs.
     * This method returns a copy of the internal array of LED colors.
     * @return an array of colors for each LED, represented as RGB format integers (0xRRGGBB).
     */
    public int[] getAll() {
        return Arrays.copyOf(ledColors, ledColors.length);
    }

    /**
     * Sets the color of a specific LED.
     * @param index the index of the LED to set, must be within bounds of the array.
     * @param color the color to set, represented as an RGB format integer (0xRRGGBB).
     */
    public void set(int index, int color) {
        if (index < 0 || index >= ledColors.length) {
            throw new IndexOutOfBoundsException("LED index out of bounds: " + index);
        }
        if (ledColors[index] != color) {
            ledColors[index] = color;
            dirty = true;
        }
    }

    /**
     * Gets the color of a specific LED.
     * @param index the index of the LED to get, must be within bounds of the array.
     * @return the color of the LED at the specified index, represented as an RGB format integer (0xRRGGBB).
     */
    public int get(int index) {
        if (index < 0 || index >= ledColors.length) {
            throw new IndexOutOfBoundsException("LED index out of bounds: " + index);
        }
        return ledColors[index];
    }

    /**
     * Gets the number of LEDs in this component.
     * @return the count of LEDs.
     */
    public int getCount() {
        return ledColors.length;
    }

    @ApiStatus.Internal
    public boolean consumeDirty() {
        boolean old = this.dirty;
        this.dirty = false;
        return old;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
