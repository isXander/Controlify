package dev.isxander.controlify.api.vmousesnapping;

import java.util.Set;

/**
 * An interface to implement by gui components to define snap points for virtual mouse snapping.
 */
public interface ISnapBehaviour {
    Set<SnapPoint> getSnapPoints();
}
