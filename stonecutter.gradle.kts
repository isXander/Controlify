import de.undercouch.gradle.tasks.download.Download

plugins {
    base
    id("me.modmuss50.mod-publish-plugin")
    id("dev.kikugie.stonecutter")
    id("de.undercouch.download") version "5.6.0"
    id("org.moddedmc.wiki.toolkit") version "0.2.5"

    id("dev.isxander.modstitch.base") apply false
}

stonecutter active file("versions/current")

stonecutter.tree.nodes.forEach { it.project.plugins.apply("dev.kikugie.stonecutter") }

val releaseMod by tasks.registering {
    group = "controlify"
    dependsOn("buildAndCollect")
    dependsOn("releaseModVersion")
    dependsOn("publishMods")
}

// download the most up to date controller database for SDL2
val downloadHidDb by tasks.registering(Download::class) {
    finalizedBy("convertHidDBToSDL3")

    group = "controlify"

    src("https://raw.githubusercontent.com/gabomdq/SDL_GameControllerDB/master/gamecontrollerdb.txt")
    dest("src/main/resources/assets/controlify/controllers/gamecontrollerdb-sdl2.txt")
}

// SDL3 renamed `Mac OS X` -> `macOS` and this change carried over to mappings
val convertHidDBToSDL3 by tasks.registering(Copy::class) {
    mustRunAfter(downloadHidDb)
    dependsOn(downloadHidDb)

    group = "controlify/internal"

    val file = downloadHidDb.get().outputs.files.singleFile
    from(file)
    into(file.parent)

    rename { "gamecontrollerdb-sdl3.txt" }
    filter { it.replace("Mac OS X", "macOS") }
}

tasks.clean {
    delete(layout.buildDirectory.dir("finalJars"))
}

val modVersion: String by project
version = modVersion

publishMods {
    val modChangelog = provider {
        rootProject.file("changelog.md")
            .takeIf { it.exists() }
            ?.readText()
            ?.replace("{version}", modVersion)
            ?.replace(
                "{targets}", stonecutter.versions
                .map { it.project + (if ("exp" in it.project) " (donator only)" else "") }
                .joinToString(separator = "\n") { "- $it" })
            ?: "No changelog provided."
    }
    changelog.set(modChangelog)

    type.set(
        when {
            "alpha" in modVersion -> ALPHA
            "beta" in modVersion -> BETA
            else -> STABLE
        }
    )
}

wiki {
    docs {
        register("controlify") {
            root = file("docs/")
        }
    }
}
