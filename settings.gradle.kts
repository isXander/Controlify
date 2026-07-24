pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        exclusiveContent {
            forRepository { maven("https://maven.fabricmc.net") }
            filter { includeGroupAndSubgroups("net.fabricmc") }
        }
        exclusiveContent {
            forRepository { maven("https://maven.neoforged.net/releases") }
            filter {
                includeGroupAndSubgroups("net.neoforged")
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
        exclusiveContent {
            forRepository { maven("https://maven.kikugie.dev/releases") }
            filter { includeGroupAndSubgroups("dev.kikugie") }
        }
        exclusiveContent {
            forRepository { maven("https://maven.quiltmc.org/repository/release") }
            filter { includeGroupAndSubgroups("org.quiltmc") }
        }
    }

    includeBuild("build-logic")
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "controlify"

stonecutter {
    val ciSingleBuild: String? = System.getenv("CI_SINGLE_BUILD")

    if (ciSingleBuild != null) {
        val split = ciSingleBuild.split(":")
        create(rootProject) {
            version(split[0], split.getOrNull(1) ?: split[0])
        }
    } else {
        create(rootProject, file("versions/versions.json"))
    }
}

enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")

