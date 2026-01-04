pluginManagement {
    repositories {
        fun strictMaven(url: String, action: Action<in InclusiveRepositoryContentDescriptor>) =
            exclusiveContent {
                forRepository { maven(url) }
                filter(action)
            }

        gradlePluginPortal()
        mavenCentral()
        strictMaven("https://maven.fabricmc.net") {
            includeGroupAndSubgroups("net.fabricmc")
            includeGroup("fabric-loom")
        }
        strictMaven("https://maven.neoforged.net/releases/") {
            includeGroupAndSubgroups("net.neoforged")
        }
        strictMaven("https://maven.kikugie.dev/releases") {
            includeGroupAndSubgroups("dev.kikugie")
        }
        strictMaven("https://maven.quiltmc.org/repository/release") {
            includeGroupAndSubgroups("org.quiltmc")
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

stonecutter {
    val ciSingleBuild: String? = System.getenv("CI_SINGLE_BUILD")

    if (ciSingleBuild != null) {
        val split = ciSingleBuild.split(":")
        create(rootProject) {
            version(split[0], split[1])
        }
    } else {
        create(rootProject, file("versions/versions.json"))
    }
}

rootProject.name = "Controlify"

include("api")

