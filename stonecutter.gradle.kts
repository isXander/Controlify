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

    val modstitchVersion = "0.5.12"
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

val versionProjects = stonecutter.versions.map { findProject(it.project)!! }
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

    if (hasProperty("discord.publish-webhook")) {
        discord {
            webhookUrl = findProperty("discord.publish-webhook")!!.toString()
            dryRunWebhookUrl = findProperty("discord.publish-webhook-dry-run")?.toString()

            username = "Controlify Updates"
            avatarUrl = "https://raw.githubusercontent.com/isXander/Controlify/multiversion/dev/src/main/resources/icon.png"

            content = changelog.map { changelog ->
                var newChangelog = changelog

                // Remove all markdown images since Discord doesn't support them.
                newChangelog = newChangelog.replace(Regex("^.*!\\[.+]\\(.+\\)\\n$"), "")

                val controlifyPing = "\n\n<@&1146064258652712960>" // <@Controlify Ping>
                if ((newChangelog.length + controlifyPing.length) > 2000) {
                    println("Changelog is too long for Discord, trimming.")
                    newChangelog = newChangelog.substring(0, 2000 - controlifyPing.length - 3) + "..."
                }
                newChangelog + controlifyPing
            }

//            publishResults.from(
//                *versionProjects
//                    .map { it to it.extensions.findByType<ModPublishExtension>()!!.platforms }
//                    .flatMap { (project, platforms) ->
//                        platforms.map { platform ->
//                            project.tasks.named<PublishModTask>(platform.taskName)
//                                .map { it.result }
//                        }
//                    }
//                    .toTypedArray()
//            )
        }
    }

    val githubProject: String by project
    if (githubProject.isNotBlank() && hasProperty("github.token")) {
        github {
            displayName.set("Controlify $modVersion")

            repository.set(githubProject)
            accessToken.set(findProperty("github.token")?.toString())
            commitish.set(grgit.branch.current().name)
            tagName = modVersion

            allowEmptyFiles = true

            announcementTitle = "Download from GitHub"
        }
    }
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
