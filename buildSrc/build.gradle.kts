plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net")
    maven("https://maven.neoforged.net/releases")
    exclusiveContent {
        forRepository {
            maven("https://maven.kikugie.dev/releases")
            maven("https://maven.kikugie.dev/snapshots")
        }
        filter {
            includeGroupAndSubgroups("dev.kikugie")
        }
    }
}

dependencies {
    fun plugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"

    implementation(plugin("dev.isxander.modstitch.base", "0.5.16-unstable"))
    implementation(plugin("dev.kikugie.stonecutter", "0.7-alpha.8"))
    implementation(plugin("fabric-loom", "1.10.5"))
    implementation(plugin("net.neoforged.moddev", "2.0.80"))
}
