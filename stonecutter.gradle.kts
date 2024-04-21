import de.undercouch.gradle.tasks.download.Download

plugins {
    id("dev.kikugie.stonecutter")
    id("de.undercouch.download") version "5.6.0"
}
stonecutter active "1.20.5-rc2" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("releaseAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("releaseMod")
}

val sdl3Target = property("deps.sdl3Target")!!.toString()
data class NativesDownload(val mavenSuffix: String, val extension: String, val jnaCanonicalPrefix: String, val taskName: String)
val downloadNativesTasks = listOf(
    NativesDownload("windows64", "dll", "win32-x86-64", "Win64"),
    NativesDownload("linux64", "so", "linux-x86-64", "Linux64"),
    NativesDownload("macos-x86_64", "dylib", "darwin-x86-64", "MacIntel"),
    NativesDownload("macos-aarch64", "dylib", "darwin-aarch64", "MacApple"),
).map {
    tasks.register("download${it.taskName}", Download::class) {
        group = "natives"

        src("https://maven.isxander.dev/releases/dev/isxander/libsdl4j-natives/$sdl3Target/libsdl4j-natives-$sdl3Target-${it.mavenSuffix}.${it.extension}")
        dest("${layout.buildDirectory.get()}/sdl-natives/${sdl3Target}/${it.jnaCanonicalPrefix}/libSDL3.${it.extension}")
        overwrite(false)
    }
}.let { downloadTasks ->
    tasks.register("downloadOfflineNatives") {
        group = "natives"

        downloadTasks.forEach(::dependsOn)

        outputs.dir("${layout.buildDirectory.get()}/sdl-natives/${sdl3Target}")
    }
}
