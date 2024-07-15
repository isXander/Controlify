import de.undercouch.gradle.tasks.download.Download

plugins {
    id("dev.architectury.loom") version "1.7.+" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.6.1+"
    id("org.ajoberstar.grgit") version "5.0.+"
    id("dev.kikugie.stonecutter")
    id("de.undercouch.download") version "5.6.0"
}
stonecutter active "1.21-fabric" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("releaseAllVersions", stonecutter.chiseled) {
    group = "mod"
    ofTask("releaseModVersion")
}

val releaseMod by tasks.registering {
    group = "mod"
    dependsOn("releaseAllVersions")
    dependsOn("publishMods")
}

stonecutter.configureEach {
    val platform = project.property("loom.platform")

    fun String.propDefined() = project.findProperty(this)?.toString()?.isNotBlank() ?: false
    consts(
        listOf(
            "fabric" to (platform == "fabric"),
            "forge" to (platform == "forge"),
            "neoforge" to (platform == "neoforge"),
            "forgelike" to (platform == "forge" || platform == "neoforge"),
            "immediately-fast" to "deps.immediatelyFast".propDefined(),
            "iris" to "deps.iris".propDefined(),
            "mod-menu" to "deps.modMenu".propDefined(),
            "sodium" to "deps.sodium".propDefined(),
            "simple-voice-chat" to "deps.simpleVoiceChat".propDefined(),
            "reeses-sodium-options" to "deps.reesesSodiumOptions".propDefined(),
        )
    )
}

val sdl3Target = property("deps.sdl3Target")!!.toString()

data class NativesDownload(
    val mavenSuffix: String,
    val extension: String,
    val jnaCanonicalPrefix: String,
    val taskName: String
)

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

val modVersion: String by project
version = modVersion

val versionProjects = stonecutter.versions.map { findProject(it.project)!! }
publishMods {
    val modChangelog =
        rootProject.file("changelog.md")
            .takeIf { it.exists() }
            ?.readText()
            ?.replace("{version}", modVersion)
            ?.replace("{targets}", stonecutter.versions
                .map { it.project }
                .joinToString(separator = "\n") { "- $it" })
            ?: "No changelog provided."
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
            dryRunWebhookUrl.set(webhookUrl)

            username = "Controlify Updates"
            avatarUrl = "https://raw.githubusercontent.com/isXander/Controlify/1.20.x/dev/src/main/resources/icon.png"

            content = changelog.get() + "\n\n<@&1146064258652712960>" // <@Controlify Ping>

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
tasks.named("publishMods") {
    dependsOn("releaseAllVersions")
}
