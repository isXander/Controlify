import de.undercouch.gradle.tasks.download.Download

plugins {
    id("dev.kikugie.stonecutter")
    id("de.undercouch.download") version "5.6.0"
}
stonecutter active "1.20.6" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("releaseAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("releaseMod")
}

stonecutter.configureEach {
    consts(listOf(
        "immediately-fast" to "deps.immediatelyFast",
        "iris" to "deps.iris",
        "mod-menu" to "deps.modMenu",
        "sodium" to "deps.sodium",
        "simple-voice-chat" to "deps.simpleVoiceChat",
    ).map { (name, prop) -> name to (project.findProperty(prop)?.toString()?.isNotBlank() ?: false)})
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

// download the most up to date controller database for SDL2
val downloadHidDb by tasks.registering(Download::class) {
    finalizedBy("convertHidDBToSDL3")

    group = "mod"

    src("https://raw.githubusercontent.com/gabomdq/SDL_GameControllerDB/master/gamecontrollerdb.txt")
    dest("src/main/resources/assets/controlify/controllers/gamecontrollerdb-sdl2.txt")
}

// SDL3 renamed `Mac OS X` -> `macOS` and this change carried over to mappings
val convertHidDBToSDL3 by tasks.registering(Copy::class) {
    mustRunAfter(downloadHidDb)
    dependsOn(downloadHidDb)

    group = "mod"

    val file = downloadHidDb.get().outputs.files.singleFile
    from(file)
    into(file.parent)

    rename { "gamecontrollerdb-sdl3.txt" }
    filter { it.replace("Mac OS X", "macOS") }
}
