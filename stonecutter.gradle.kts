plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.5-pre1" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("releaseAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("releaseMod")
}
