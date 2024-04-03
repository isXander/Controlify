plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "24w14potato" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "mod"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledReleaseMod", stonecutter.chiseled) {
    group = "mod"
    ofTask("releaseMod")
}
