plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.21"
}

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
    strictMaven("https://quiltmc.org/repository/release") {
        includeGroupAndSubgroups("org.quiltmc")
    }
}

dependencies {
    fun plugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"

    implementation(plugin("dev.isxander.modstitch.base", "0.7.1-unstable"))
    implementation(plugin("dev.kikugie.stonecutter", "0.7.9"))
    implementation(plugin("fabric-loom", "1.13.4"))
    implementation(plugin("net.neoforged.moddev", "2.0.119"))
}
