package dev.isxander.controlify.gui.layout;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLayoutComponent<T extends RenderComponent> implements RenderComponent {
    private final List<T> components = new ArrayList<>();

    public List<T> getChildComponents() {
        return components;
    }

    public <U extends T> U insertTop(U area) {
        components.add(area);
        return area;
    }

    public <U extends T> U insertBottom(U area) {
        components.add(0, area);
        return area;
    }

    public <U extends T> U insertAbove(U area, T above) {
        int index = components.indexOf(above);
        if (index == -1)
            throw new IllegalArgumentException("InteractionArea " + above + " is not registered!");

        components.add(index + 1, area);
        return area;
    }

    public <U extends T> U insertBelow(U area, T below) {
        int index = components.indexOf(below);
        if (index == -1)
            throw new IllegalArgumentException("InteractionArea " + below + " is not registered!");

        components.add(index, area);
        return area;
    }
}
