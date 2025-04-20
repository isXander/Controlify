package dev.isxander.controlify

import net.fabricmc.loom.task.prod.ClientProductionRunTask

plugins {
    id("dev.isxander.modstitch.base")
    `maven-publish`
}

modstitch.apply {
    minecraftVersion = mcVersion
    javaTarget = 21

    parchment {
        propMap("parchment.version") { mappingsVersion = it }
        propMap("parchment.minecraft") { minecraftVersion = it }
    }

    metadata {
        fun prop(property: String, block: (String) -> Unit) {
            propMap(property, ifNull = {""}) { block(it) }
        }

        modVersion = "$modVersion+${stonecutter.current.project}"
        modGroup = "dev.isxander"
        modLicense = "LGPL-3.0-or-later"
        modAuthor = "isXander"
        prop("modDescription") { modDescription = it }

        prop("packFormat") { replacementProperties.put("pack_format", it) }
        prop("githubProject") { replacementProperties.put("github", it) }
        prop("meta.mcDep") { replacementProperties.put("mc", it) }
        prop("meta.loaderDep") { replacementProperties.put("loaderVersion", it) }
        prop("deps.fabricApi") { replacementProperties.put("fapi", it) }
    }

    loom {
        propMap("deps.fabricLoader", required = true) { fabricLoaderVersion = it }

        configureLoom {
            runConfigs.all {
                ideConfigGenerated(false)
                vmArg("-Dsodium.checks.issue2561=false")
            }
        }
    }

    moddevgradle {
        enable {
            propMap("deps.neoForge") { neoForgeVersion = it }
            propMap("deps.forge") { forgeVersion = it }
        }

        defaultRuns()
        configureNeoforge {
            runs.all {
                disableIdeRun()
            }
        }
    }
}

repositories {
    maven("https://maven.terraformersmc.com")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.isxander.dev/snapshots")
    maven("https://maven.quiltmc.org/repository/release")
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


/*
Setup stonecutter for the project.
 */
stonecutter.apply {
    consts(
        "fabric" to modstitch.isLoom,
        "neoforge" to modstitch.isModDevGradleRegular,
        "immediately-fast" to isPropDefined("deps.immediatelyFast"),
        "iris" to isPropDefined("deps.iris"),
        "mod-menu" to isPropDefined("deps.modMenu"),
        "sodium" to isPropDefined("deps.sodium"),
        "simple-voice-chat" to isPropDefined("deps.simpleVoiceChat"),
        "reeses-sodium-options" to isPropDefined("deps.reesesSodiumOptions"),
        "fancy-menu" to isPropDefined("deps.fancyMenu"),
    )

    dependencies(
        "fapi" to (findProperty("deps.fabricApi")?.toString() ?: "0.0.0"),
    )
}

tasks.named<ProcessResources>("generateModMetadata") {
    eachFile {
        // don't include photoshop files for the textures for development
        if (name.endsWith(".psd")) {
            exclude()
        }
    }
}

publishing {
    repositories {
        val username = prop("XANDER_MAVEN_USER")
        val password = prop("XANDER_MAVEN_PASS")
        if (username != null && password != null) {
            maven(url = "https://maven.isxander.dev/releases") {
                name = "XanderReleases"
                credentials {
                    this.username = username
                    this.password = password
                }
            }
        } else {
            logger.warn("Xander Maven credentials not satisfied.")
        }
    }
}
