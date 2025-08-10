package dev.isxander.controlify

import net.fabricmc.loom.task.prod.ClientProductionRunTask

plugins {
    id("dev.isxander.modstitch.base")
    `maven-publish`
    `java-library`
}

modstitch.apply {
    minecraftVersion = mcVersion
    javaVersion = 21

    parchment {
        propMap("parchment.version") { mappingsVersion = it }
        propMap("parchment.minecraft") { minecraftVersion = it }
    }

    metadata {
        fun prop(property: String, block: (String) -> Unit) {
            propMap(property, ifNull = {""}) { block(it) }
        }

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
                ideConfigGenerated(true)
                vmArg("-Dsodium.checks.issue2561=false")
            }

            mixin.useLegacyMixinAp = false
        }
    }

    moddevgradle {
        propMap("deps.neoForge") { neoForgeVersion = it }
        propMap("deps.forge") { forgeVersion = it }

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
}

/*
Setup stonecutter for the project.
 */
stonecutter.apply {
    constants {
        put("fabric", modstitch.isLoom)
        put("neoforge", modstitch.isModDevGradleRegular)
        put("immediately-fast", isPropDefined("deps.immediatelyFast"))
        put("iris", isPropDefined("deps.iris"))
        put("mod-menu", isPropDefined("deps.modMenu"))
        put("sodium", isPropDefined("deps.sodium"))
        put("simple-voice-chat", isPropDefined("deps.simpleVoiceChat"))
        put("reeses-sodium-options", isPropDefined("deps.reesesSodiumOptions"))
        put("fancy-menu", isPropDefined("deps.fancyMenu"))
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
