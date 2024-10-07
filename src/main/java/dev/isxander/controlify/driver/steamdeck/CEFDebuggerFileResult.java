package dev.isxander.controlify.driver.steamdeck;

public enum CEFDebuggerFileResult {
    /** The file has just been created, but requires steam to restart to take effect **/
    REQUIRES_RESTART,
    /** An exception occurred when attempting to create the file **/
    FAILED_TO_CREATE,
    /** The file exists, but the deck is currently on desktop mode, meaning it can't be used. **/
    PRESENT_BUT_DESKTOP,
    /** The deck is in gaming mode, Controlify has an established connection to the CEF debugger **/
    WORKING,
    /** Controlify can't check whether the file exists because it is sandboxed and can't access the file system, but even in Gaming mode, Controlify can't establish a connection to CEF **/
    SANDBOXED_ERROR,
    /** The device is not a steam deck **/
    NOT_STEAM_DECK
}
