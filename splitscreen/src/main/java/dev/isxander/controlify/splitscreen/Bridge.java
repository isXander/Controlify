package dev.isxander.controlify.splitscreen;

/**
 * Represents a bridge object between the host and the remote.
 * This is used to determine if this object sits controlling a local object, or a remote object.
 */
public interface Bridge {
    /**
     * @return true if this is a remote bridge, false if this is a local bridge.
     */
    boolean isRemote();
}
