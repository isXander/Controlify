import de.undercouch.gradle.tasks.download.Download

plugins {
    id("me.modmuss50.mod-publish-plugin")
    id("dev.kikugie.stonecutter")
    id("de.undercouch.download") version "5.6.0"
    id("org.moddedmc.wiki.toolkit") version "0.2.5"
    id("com.gradleup.nmcp.aggregation") version "1.4.3"
    id("dev.isxander.secrets")

    id("dev.isxander.modstitch.base") apply false
}

stonecutter active file("versions/current")

repositories {
    mavenCentral()
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

tasks.register("clean") {
    group = "build"
    delete(layout.buildDirectory.dir("finalJars"))
}

val modVersion: String by project
version = modVersion

publishMods {
    dryRun = false

    val modChangelog = provider {
        rootProject.file("changelog.md")
            .takeIf { it.exists() }
            ?.readText()
            ?.replace("{version}", modVersion)
            ?.replace("{targets}", stonecutter.versions.joinToString(separator = "\n") { "- $it" })
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

// subprojects depend themselves on this task
val releaseModVersions by tasks.registering {
    group = "controlify"

    if (!publishMods.dryRun.get()) {
        dependsOn("publishAggregationToCentralPortal")
    }
}

nmcpAggregation {
    allowDuplicateProjectNames = true

    centralPortal {
        username = secrets.gradleProperty("mcentral.username")
        password = secrets.gradleProperty("mcentral.password")

        publicationName = "controlify:$version"
    }
}
dependencies {
    allprojects {
        nmcpAggregation(project(path))
    }
}

wiki {
    docs {
        register("controlify") {
            root = file("docs/")
        }
    }
}
