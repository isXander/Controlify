import de.undercouch.gradle.tasks.download.Download
import dev.isxander.stonecutterconfigurator.*
import dev.kikugie.stonecutter.controller.ChiseledTask
import dev.kikugie.stonecutter.ide.RunConfigType

plugins {
    base
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    id("org.ajoberstar.grgit") version "5.0.+"
    id("dev.kikugie.stonecutter")
    id("de.undercouch.download") version "5.6.0"
    id("org.moddedmc.wiki.toolkit") version "0.2.5"

    val modstitchVersion = "0.5.15-unstable"
    id("dev.isxander.modstitch.base") version modstitchVersion apply false
}

val ciSingleBuild: String? = System.getenv("CI_SINGLE_BUILD")
if (ciSingleBuild != null) {
    stonecutter active ciSingleBuild
} else {
stonecutter active file("versions/current")
}

val registeredBuilds = getRegisteredBuilds()

val chiseledBuildAndCollect = registerChiseled("buildAndCollect")
val chiseledReleaseModVersion = registerChiseled("releaseModVersion")

val releaseMod by tasks.registering {
    group = "controlify"
    dependsOn(chiseledBuildAndCollect)
    dependsOn(chiseledReleaseModVersion)
    dependsOn("publishMods")
}

allprojects {
    repositories {
        maven("https://maven.terraformersmc.com")
        maven("https://maven.isxander.dev/releases")
        maven("https://maven.isxander.dev/snapshots")
        maven("https://maven.quiltmc.org/repository/release")
    }
}

stonecutter {
    generateRunConfigs = listOf(RunConfigType.SWITCH)

    parameters {
        fun String.propDefined() = project(node!!.metadata.project).findProperty(this)?.toString()?.isNotBlank() ?: false
        consts(
            listOf(
                "immediately-fast" to "deps.immediatelyFast".propDefined(),
                "iris" to "deps.iris".propDefined(),
                "mod-menu" to "deps.modMenu".propDefined(),
                "sodium" to "deps.sodium".propDefined(),
                "simple-voice-chat" to "deps.simpleVoiceChat".propDefined(),
                "reeses-sodium-options" to "deps.reesesSodiumOptions".propDefined(),
                "fancy-menu" to "deps.fancyMenu".propDefined(),
            )
        )
    }
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
                "{targets}", registeredBuilds.builds
                .map { it.identifier + (if (it.experimental) " (donator only)" else "") }
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

fun registerChiseled(task: String, includeExperimental: Boolean = true, action: ChiseledTask.() -> Unit = {}): TaskProvider<ChiseledTask> {
    return tasks.register(
        "chiseled" + task.replaceFirstChar { it.uppercase() },
        stonecutter.chiseled.kotlin
    ) {
        group = "controlify"
        ofTask(task)
        versions { _, version ->
            includeExperimental || !registeredBuilds.builds.find { it.identifier == version.project }!!.experimental
        }
        action(this)

    }.also { stonecutter registerChiseled it }
}
