package dev.isxander.controlify.api.vmousesnapping;

import java.util.Set;

/**
 * An interface to implement by gui components to define snap points for virtual mouse snapping.
 * Can also be implemented in a mixin to improve compatibility.
 */
public interface ISnapBehaviour {
    Set<SnapPoint> getSnapPoints();
}
