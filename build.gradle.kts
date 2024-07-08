import net.fabricmc.loom.task.RemapJarTask
import org.gradle.configurationcache.extensions.capitalized

plugins {
    `java-library`

    id("dev.architectury.loom") version "1.6.+"
    id("dev.kikugie.j52j") version "1.0"

    id("me.modmuss50.mod-publish-plugin") version "0.5.+"
    `maven-publish`

    id("org.ajoberstar.grgit") version "5.0.+"
}

// version stuff
val mcVersion = property("mcVersion")!!.toString()
val mcSemverVersion = stonecutter.current.version

// loader stuff
val loader = loom.platform.get().name.lowercase()
val isFabric = loader == "fabric"
val isNeoforge = loader == "neoforge"
val isForge = loader == "forge"
val isForgeLike = isNeoforge || isForge

// project stuff
group = "dev.isxander"
val versionWithoutMC = property("modVersion")!!.toString()
version = "$versionWithoutMC+${stonecutter.current.project}"
val isAlpha = "alpha" in version.toString()
val isBeta = "beta" in version.toString()
base.archivesName.set(property("modName").toString())

// mixin stuff
val mixins = mapOf(
    "controlify" to true,
    "controlify-compat.iris" to isPropDefined("deps.iris"),
    "controlify-compat.sodium" to isPropDefined("deps.sodium"),
    "controlify-compat.reeses-sodium-options" to isPropDefined("deps.reesesSodiumOptions"),
    "controlify-compat.yacl" to true,
    "controlify-compat.simple-voice-chat" to isPropDefined("deps.simpleVoiceChat"),
    "controlify-platform.fabric" to isFabric,
    "controlify-platform.neoforge" to isNeoforge,
)
    .map { (k, v) -> if (v) k else null }
    .filterNotNull()
    .map { "$it.mixins.json" }

val accessWidenerName = "controlify.accesswidener"
loom {
    accessWidenerPath.set(project.file("src/main/resources/$accessWidenerName"))

    if (stonecutter.current.isActive) {
        runConfigs.all {
            ideConfigGenerated(true)
            runDir("../../run")
        }
    }

    mixin {
        useLegacyMixinAp.set(false)
    }

    if (isForge) {
        forge {
            convertAccessWideners.set(true)
            mixins.forEach { mixinConfig(it) }
        }
    }
}

stonecutter {
    dependencies(
        "fapi" to (findProperty("deps.fabricApi")?.toString() ?: "0.0.0")
    )
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.isxander.dev/snapshots")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.quiltmc.org/repository/release")
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") }
        filter { includeGroup("maven.modrinth") }
    }
    exclusiveContent {
        forRepository { maven("https://cursemaven.com") }
        filter { includeGroup("curse.maven") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.flashyreese.me/releases") }
        filter { includeGroup("me.flashyreese.mods") }
    }
    maven("https://jitpack.io")
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    fun Dependency?.jij() = this?.also(::include)
    fun Dependency?.forgeRuntime() = this?.also { if (isForgeLike) "forgeRuntimeLibrary"(it) }

    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        optionalProp("deps.parchment") {
            parchment("org.parchmentmc.data:parchment-$it@zip")
        }

        officialMojangMappings()
    })

    fun modDependency(id: String, artifactGetter: (String) -> String, extra: (Boolean) -> Unit = {}) {
        optionalProp("deps.$id") {
            val noRuntime = findProperty("deps.$id.noRuntime")?.toString()?.toBoolean() == true
            val configuration = if (noRuntime) "modCompileOnly" else "modImplementation"

            configuration(artifactGetter(it))

            extra(!noRuntime)
        }
    }

    if (isFabric) {
        modImplementation("net.fabricmc:fabric-loader:${property("deps.fabricLoader")}")

        val fapiVersion = property("deps.fabricApi").toString()
        listOf(
            "fabric-resource-loader-v0",
            "fabric-lifecycle-events-v1",
            "fabric-key-binding-api-v1",
            "fabric-registry-sync-v0",
            "fabric-screen-api-v1",
            "fabric-command-api-v2",
            "fabric-networking-api-v1",
            "fabric-item-group-api-v1",
            "fabric-rendering-v1",
            "fabric-transitive-access-wideners-v1",
        ).forEach {
            modImplementation(fabricApi.module(it, fapiVersion))
        }
        // so you can do `depends: fabric-api` in FMJ
        modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fapiVersion")

        // sodium compat
        modDependency("sodium", { "maven.modrinth:sodium:$it" }) { runtime ->
            if (runtime) {
                listOf(
                    "fabric-rendering-fluids-v1",
                    "fabric-rendering-data-attachment-v1",
                ).forEach { module ->
                    modRuntimeOnly(fabricApi.module(module, fapiVersion))
                }
            }
        }

        // RSO compat
        modDependency("reesesSodiumOptions", { "me.flashyreese.mods:reeses-sodium-options:$it" })

        // iris compat
        modDependency("iris", { "maven.modrinth:iris:$it" }) { runtime ->
            if (runtime) {
                modRuntimeOnly("org.anarres:jcpp:1.4.14")
                modRuntimeOnly("io.github.douira:glsl-transformer:2.0.0-pre13")
            }
        }

        // mod menu compat
        modDependency("modMenu", { "com.terraformersmc:modmenu:$it" })
    } else if (isNeoforge) {
        "neoForge"("net.neoforged:neoforge:${findProperty("deps.neoforge")}")
    } else if (isForge) {
        "forge"("net.minecraftforge:forge:${findProperty("deps.forge")}")
    }

    modApi("dev.isxander:yet-another-config-lib:${property("deps.yacl")}") {
        // was including old fapi version that broke things at runtime
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
        exclude(group = "thedarkcolour")
    }.forgeRuntime()

    // bindings for SDL3
    api("dev.isxander:libsdl4j:${property("deps.sdl3Target")}-${property("deps.sdl34jBuild")}")
        .forgeRuntime().jij()

    // used to identify controller PID/VID when SDL is not available
    api("org.hid4java:hid4java:${property("deps.hid4java")}")
        .jij().forgeRuntime()

    // A json5 reader that hooks into gson
    listOf(
        "json",
        "gson",
    ).forEach {
        api("org.quiltmc.parsers:$it:${property("deps.quiltParsers")}")
            .jij().forgeRuntime()
    }

    // immediately-fast compat
    modDependency("immediatelyFast", { "maven.modrinth:immediatelyfast:$it" }) { runtime ->
        if (runtime) {
            modRuntimeOnly("net.lenni0451:Reflect:1.1.0")
        }
    }

    // simple-voice-chat compat
    modDependency("simpleVoiceChat", { "maven.modrinth:simple-voice-chat:$it" })
}

tasks {
    processResources {
        val modId: String by project
        val modName: String by project
        val modDescription: String by project
        val githubProject: String by project
        val packFormat: String by project

        val props = buildMap {
            put("id", modId)
            put("group", project.group)
            put("name", modName)
            put("description", modDescription)
            put("version", project.version)
            put("github", githubProject)
            put("pack_format", packFormat)

            if (isFabric) {
                put("mc", findProperty("fmj.mcDep"))
                put("mixins", mixins.joinToString("\",\"", prefix = "\"", postfix = "\""))
                put("fapi", findProperty("fmj.fapiDep") ?: "*")
            }

            if (isForgeLike) {
                put("mc", findProperty("modstoml.mcDep"))
                put("loaderVersion", findProperty("modstoml.loaderVersion"))
                put("forgeId", findProperty("modstoml.forgeId"))
                put("forgeConstraint", findProperty("modstoml.forgeConstraint"))
                put("mixins", mixins.joinToString("\n\n") { """
                    [[mixins]]
                    config = "$it"
                """.trimIndent() })
            }
        }
        props.forEach(inputs::property)

        val fabricModJson = "fabric.mod.json"
        val modsToml = "META-INF/mods.toml"
        val neoforgeModsToml = "META-INF/neoforge.mods.toml"
        val metadataFiles = listOf(
            fabricModJson, modsToml, neoforgeModsToml,
        )
        val modMetadataFile = when {
            isFabric -> fabricModJson
            isNeoforge && Version(stonecutter.current.version) >= Version("1.20.5") -> neoforgeModsToml
            isForgeLike -> modsToml
            else -> error("Unknown loader")
        }

        filesMatching(listOf(modMetadataFile, "**/pack.mcmeta")) {
            expand(props)
        }

        exclude(metadataFiles - modMetadataFile)

        eachFile {
            // don't include photoshop files for the textures for development
            if (name.endsWith(".psd")) {
                exclude()
            }
        }
    }

    register("releaseMod") {
        group = "mod"

        dependsOn("publishMods")
        dependsOn("publish")
    }
}

val offlineRemapJar by tasks.registering(RemapJarTask::class) {
    group = "offline"

    val downloadTask = rootProject.tasks["downloadOfflineNatives"]

    dependsOn(tasks.jar)
    dependsOn(downloadTask)
    inputFile.set(tasks.jar.get().archiveFile.get().asFile)

    from(downloadTask.outputs.files)

    archiveClassifier.set("offline")
}

tasks.build { dependsOn(offlineRemapJar) }

tasks.remapJar {
    if (isNeoforge) {
        atAccessWideners.add(accessWidenerName)
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.release = project.property("java.version").toString().toInt()
}

publishMods {
    displayName.set("Controlify $versionWithoutMC for ${loader.capitalized()} $mcVersion")

    file.set(tasks.remapJar.get().archiveFile)
    additionalFiles.setFrom(offlineRemapJar.get().archiveFile)

    changelog.set(
        rootProject.file("changelog.md")
            .takeIf { it.exists() }
            ?.readText()
            ?: "No changelog provided."
    )
    type.set(when {
        isAlpha -> ALPHA
        isBeta -> BETA
        else -> STABLE
    })
    modLoaders.add(loader)

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = versionList("pub.stableMC")

    val modrinthId: String by project
    if (modrinthId.isNotBlank() && hasProperty("modrinth.token")) {
        modrinth {
            projectId.set(modrinthId)
            accessToken.set(findProperty("modrinth.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.modrinthMC"))

            requires { slug.set("yacl") }

            if (isFabric) {
                requires { slug.set("fabric-api") }
                optional { slug.set("modmenu") }
            }
        }
    }

    val curseforgeId: String by project
    if (curseforgeId.isNotBlank() && hasProperty("curseforge.token")) {
        curseforge {
            projectId.set(curseforgeId)
            accessToken.set(findProperty("curseforge.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.curseMC"))

            requires { slug.set("yacl") }

            if (isFabric) {
                requires { slug.set("fabric-api") }
                optional { slug.set("modmenu") }
            }
        }
    }

    val githubProject: String by project
    if (githubProject.isNotBlank() && hasProperty("github.token")) {
        github {
            repository.set(githubProject)
            accessToken.set(findProperty("github.token")?.toString())
            commitish.set(grgit.branch.current().name)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mod") {
            groupId = "dev.isxander"
            artifactId = "controlify"

            from(components["java"])
        }
    }

    repositories {
        val username = "XANDER_MAVEN_USER".let { System.getenv(it) ?: findProperty(it) }?.toString()
        val password = "XANDER_MAVEN_PASS".let { System.getenv(it) ?: findProperty(it) }?.toString()
        if (username != null && password != null) {
            maven(url = "https://maven.isxander.dev/releases") {
                name = "XanderReleases"
                credentials {
                    this.username = username
                    this.password = password
                }
            }
        } else {
            println("Xander Maven credentials not satisfied.")
        }
    }
}

fun <T> optionalProp(property: String, block: (String) -> T?) {
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)
}

fun isPropDefined(property: String): Boolean {
    return findProperty(property)?.toString()?.isNotBlank() ?: false
}

data class Version(val string: String) {
    operator fun compareTo(other: Version): Int = stonecutter.compare(string, other.string)
}
