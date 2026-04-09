package dev.isxander.controlify

import dev.kikugie.stonecutter.build.config.ReplacementContainer
import net.fabricmc.loom.task.prod.ClientProductionRunTask

plugins {
    `java-library`
    id("dev.isxander.modstitch.base")
    `maven-publish`
    signing
    id("dev.isxander.secrets")
}

modstitch.apply {
    minecraftVersion = mcVersion
    javaVersion = 25

    parchment {
        propMap("parchment.version") { mappingsVersion = it }
        propMap("parchment.minecraft") { minecraftVersion = it }
    }

    metadata {
        fun prop(property: String, block: (String) -> Unit) {
            propMap(property, ifNull = {""}) { block(it) }
        }

        modVersion = "${project.modVersion}+${stonecutter.current.project}"
        modGroup = "dev.isxander"
        modLicense = "LGPL-3.0-or-later"
        modAuthor = "isXander"
        prop("modDescription") { modDescription = it }

        prop("githubProject") { replacementProperties.put("github", it) }
        prop("meta.mcDep") { replacementProperties.put("mc", it) }
        prop("meta.loaderDep") { replacementProperties.put("loaderVersion", it) }
        prop("deps.fabricApi") { replacementProperties.put("fapi", it) }
    }

    loom {
        propMap("deps.fabricLoader", required = true) { fabricLoaderVersion = it }

        configureLoom {
            runConfigs.all {
                ideConfigGenerated(true)
                vmArg("-Dsodium.checks.issue2561=false")
            }

            mixin.useLegacyMixinAp = false
        }
    }

    moddevgradle {
        propMap("deps.neoForge") { neoForgeVersion = it }

        defaultRuns()
    }
}

repositories {
    strictMaven("https://maven.terraformersmc.com") {
        includeGroupAndSubgroups("com.terraformersmc")
    }
    strictMaven("https://maven.quiltmc.org/repository/release") {
        includeGroupAndSubgroups("org.quiltmc")
    }
    strictMaven("https://maven.nucleoid.xyz/releases") {
        includeGroupAndSubgroups("eu.pb4")
    }
    strictMaven("https://maven.caffeinemc.net/releases") {
        includeGroupAndSubgroups("net.caffeinemc")
    }
    maven("https://maven.isxander.dev/releases")
}

/*
Prod run environment from loom!
MDG targets don't need this, they run with named namespace.
 */
val productionMods: Configuration by configurations.creating {
    isTransitive = false
}
if (modstitch.isLoom) {
    @Suppress("UnstableApiUsage")
    val runProdClient by tasks.registering(ClientProductionRunTask::class) {
        group = "fabric"

        mods.from(productionMods)

        jvmArgs = listOf("-Dsodium.checks.issue2561=false")

        outputs.upToDateWhen { false }
    }
} else {
    val runProdClient by tasks.registering {
        group = "controlify/versioned"
        dependsOn("runClient") // neoforge is prod always
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}
tasks.javadoc {
    isFailOnError = false
}

/*
Setup stonecutter for the project.
 */
stonecutter.apply {
    constants {
        put("fabric", modstitch.isLoom)
        put("neoforge", modstitch.isModDevGradleRegular)
        put("immediately_fast", isPropDefined("deps.immediatelyFast"))
        put("iris", isPropDefined("deps.iris"))
        put("mod_menu", isPropDefined("deps.modMenu"))
        put("sodium", isPropDefined("deps.sodium"))
        put("simple_voice_chat", isPropDefined("deps.simpleVoiceChat"))
        put("reeses_sodium_options", isPropDefined("deps.reesesSodiumOptions"))
        put("fancy_menu", isPropDefined("deps.fancyMenu"))
    }

    dependencies {
        put("fapi", prop("deps.fabricApi") ?: "0.0.0")
    }
}

tasks.named<ProcessResources>("generateModMetadata") {
    eachFile {
        // don't include photoshop files for the textures for development
        if (name.endsWith(".psd")) {
            exclude()
        }
    }
}

val signingKeyProvider = secrets.gradleProperty("signing.secretKey")
val signingPasswordProvider = secrets.gradleProperty("signing.password")
// not configuration cache friendly, but neither is the whole of signing plugin
// this plugin does not support lazy configuration of signing keys
gradle.taskGraph.whenReady {
    val willSign = allTasks.any { it.name.startsWith("sign") }
    if (willSign) {
        signing {
            val signingKey = signingKeyProvider.orNull
            val signingPassword = signingPasswordProvider.orNull

            isRequired = signingKey != null && signingPassword != null
            if (isRequired) {
                useInMemoryPgpKeys(signingKey, signingPassword)
            } else {
                logger.error("Signing keys not found; skipping signing!")
            }
        }
    }
}

// fix stonecutterGenerate task dependencies
tasks.named<ProcessResources>("generateModMetadata") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn("stonecutterGenerate")
}
modstitch.moddevgradle {
    modstitch.onEnable {
        tasks.named("createMinecraftArtifacts") {
            dependsOn("stonecutterGenerate")
        }
    }
}
