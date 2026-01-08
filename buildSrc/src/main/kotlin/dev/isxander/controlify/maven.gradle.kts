package dev.isxander.controlify

plugins {
    `maven-publish`
}

val publishUsername = providers
    .gradleProperty("XANDER_MAVEN_USER")
    .orElse(providers.environmentVariable("XANDER_MAVEN_USER"))

val publishPassword = providers
    .gradleProperty("XANDER_MAVEN_PASS")
    .orElse(providers.environmentVariable("XANDER_MAVEN_PASS"))


publishing {
    repositories {
        maven(url = "https://maven.isxander.dev/releases") {
            name = "XanderReleases"
            credentials(PasswordCredentials::class) {
                username = publishUsername.orNull
                password = publishPassword.orNull
            }
        }
    }
}
