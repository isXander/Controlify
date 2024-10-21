import dev.kikugie.stonecutter.settings.ProjectProvider

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.quiltmc.org/repository/release")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5-beta.3"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        fun ProjectProvider.mc(mcVersion: String, name: String = mcVersion, loaders: Iterable<String>) {
            for (loader in loaders) {
                vers("$name-$loader", mcVersion)
            }
        }

        mc("1.21", loaders = listOf("fabric", "neoforge"))
        mc("1.20.6", loaders = listOf("fabric", "neoforge"))
        mc("1.20.4", loaders = listOf("fabric", "neoforge"))
        mc("1.20.1", loaders = listOf("fabric"))

        vcsVersion = "1.21-fabric"

        branch("sodium-0.6") {
            mc("1.21", loaders = listOf("fabric", "neoforge"))
        }
        branch("sodium-0.5") {
            mc("1.21", loaders = listOf("fabric"))
            mc("1.20.6", loaders = listOf("fabric"))
            mc("1.20.4", loaders = listOf("fabric"))
            mc("1.20.1", loaders = listOf("fabric"))
        }
    }
}

rootProject.name = "Controlify"

