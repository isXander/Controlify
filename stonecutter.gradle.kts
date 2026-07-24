import de.undercouch.gradle.tasks.download.Download
import org.gradle.kotlin.dsl.register

plugins {
    id("dev.kikugie.stonecutter")
    alias(libs.plugins.undercouch.download)
    alias(libs.plugins.wiki.toolkit)
    alias(libs.plugins.mod.publish.plugin)
	alias(libs.plugins.spotless)
}

stonecutter active file("versions/current")

repositories {
	mavenCentral()
}

// download the most up to date controller database for SDL2
val downloadHidDb = tasks.register<Download>("downloadHidDb") {
    finalizedBy("convertHidDBToSDL3")

    group = "controlify"

    src("https://raw.githubusercontent.com/gabomdq/SDL_GameControllerDB/master/gamecontrollerdb.txt")
    dest("src/main/resources/assets/controlify/controllers/gamecontrollerdb-sdl2.txt")
}

// SDL3 renamed `Mac OS X` -> `macOS` and this change carried over to mappings
val convertHidDBToSDL3 = tasks.register<Copy>("convertHidDBToSDL3") {
    mustRunAfter(downloadHidDb)
    dependsOn(downloadHidDb)

    group = "controlify/internal"

    val file = downloadHidDb.get().outputs.files.singleFile
    from(file)
    into(file.parent)

    rename { "gamecontrollerdb-sdl3.txt" }
    filter { it.replace("Mac OS X", "macOS") }
}

val modVersion = providers.gradleProperty("mod.version")

publishMods {
    dryRun = false

    version = modVersion

    changelog = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md"))
        .asText
        .zip(modVersion) { changelog, version ->
            changelog.replace("{version}", version)
        }
        .map { changelog ->
            changelog.replace(
                "{targets}",
                stonecutter.versions.joinToString(separator = "\n") { "- ${it.project}" }
            )
        }

    type = modVersion.map { version ->
        when {
            "alpha" in version -> ALPHA
            "beta" in version -> BETA
            else -> STABLE
        }
    }

    discord {
        webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK_URL")
        setPlatformsAllFrom(*stonecutter.versions.map { project(it.project) }.toTypedArray())
        avatarUrl = providers.gradleProperty("discord.image-url")

        style {
            look = "MODERN"
            thumbnailUrl = avatarUrl
            color = providers.gradleProperty("discord.color")
            link = "BUTTON"
        }
    }
}

spotless {
	java {
		target("src/**/*.java")
		licenseHeaderFile(rootProject.layout.projectDirectory.file("HEADER"))

		removeUnusedImports()
		trimTrailingWhitespace()
		endWithNewline()
		formatAnnotations()
		leadingSpacesToTabs(4)
	}
}

wiki {
    docs {
        register("controlify") {
            root = file("docs/")
        }
    }
}
