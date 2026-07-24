plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    fun plugin(id: String, version: Provider<String>) =
        version.map { "$id:$id.gradle.plugin:$it" }

    implementation(plugin("dev.isxander.mtk.modrepos", libs.versions.modstitch.modrepos))
}
