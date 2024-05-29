import net.fabricmc.loom.task.RemapJarTask

plugins {
    `java-library`

    id("fabric-loom") version "1.6.+"

    id("me.modmuss50.mod-publish-plugin") version "0.5.+"
    `maven-publish`

    id("org.ajoberstar.grgit") version "5.0.+"
}

val mcVersion = property("mcVersion")!!.toString()
val mcSemverVersion = stonecutter.current.version
val mcDep = property("fmj.mcDep").toString()

group = "dev.isxander"
val versionWithoutMC = "2.0.0-beta.10"
version = "$versionWithoutMC+${stonecutter.current.project}"

val isAlpha = "alpha" in version.toString()
val isBeta = "beta" in version.toString()
if (isAlpha) println("Controlify alpha version detected.")
if (isBeta) println("Controlify beta version detected.")

base {
    archivesName.set(property("modName").toString())
}

loom {
    accessWidenerPath.set(project.file("src/main/resources/controlify.accesswidener"))

    if (stonecutter.current.isActive) {
        runConfigs.all {
            ideConfigGenerated(true)
            runDir("../../run")
        }
    }

    mixin {
        useLegacyMixinAp.set(false)
    }
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
    maven("https://jitpack.io")
    maven("https://maven.flashyreese.me/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        optionalProp("deps.parchment") {
            parchment("org.parchmentmc.data:parchment-$it@zip")
        }

        officialMojangMappings()
    })
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
    ).forEach {
        modImplementation(fabricApi.module(it, fapiVersion))
    }
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fapiVersion") // so you can do `depends: fabric-api` in FMJ

    modApi("dev.isxander:yet-another-config-lib:${property("deps.yacl")}") {
        // was including old fapi version that broke things at runtime
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
    }

    // bindings for SDL3
    api(include("dev.isxander:libsdl4j:${property("deps.sdl3Target")}-${property("deps.sdl34jBuild")}")!!)

    // used to identify controller PID/VID when SDL is not available
    api(include("org.hid4java:hid4java:${property("deps.hid4java")}")!!)

    // A json5 reader that hooks into gson
    listOf(
        "json",
        "gson",
    ).forEach {
        api(include("org.quiltmc.parsers:$it:${property("deps.quiltParsers")}")!!)
    }

    fun modDependency(id: String, artifactGetter: (String) -> String, extra: (Boolean) -> Unit = {}) {
        optionalProp("deps.$id") {
            val noRuntime = findProperty("deps.$id.noRuntime")?.toString()?.toBoolean() == true
            println("Adding $id dependency: $it (noRuntime: $noRuntime)")
            val configuration = if (noRuntime) "modCompileOnly" else "modImplementation"

            configuration(artifactGetter(it))

            extra(!noRuntime)
        }
    }

    // mod menu compat
    modDependency("modMenu", { "com.terraformersmc:modmenu:$it" })

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

    // iris compat
    modDependency("iris", { "maven.modrinth:iris:$it" }) { runtime ->
        if (runtime) {
            modRuntimeOnly("org.anarres:jcpp:1.4.14")
            modRuntimeOnly("io.github.douira:glsl-transformer:2.0.0-pre13")
        }
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

        val conditionalMixins = mapOf(
            "compat.iris" to isPropDefined("deps.iris"),
            "compat.sodium" to isPropDefined("deps.sodium"),
            "compat.yacl" to true,
            "compat.simple-voice-chat" to isPropDefined("deps.simpleVoiceChat"),
            "platform.fabric" to true,
        )
            .map { (k, v) -> if (v) k else null }
            .filterNotNull()
            .joinToString("\",", prefix = "\",") { "\"controlify-$it.mixins.json" }

        val props = mapOf(
            "id" to modId,
            "group" to project.group,
            "name" to modName,
            "description" to modDescription,
            "version" to project.version,
            "github" to githubProject,
            "mc" to mcDep,
            "pack_format" to packFormat,
            "mixins" to conditionalMixins
        )

        props.forEach(inputs::property)

        filesMatching(listOf("fabric.mod.json", "**/pack.mcmeta")) {
            expand(props)
        }

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

val javaMajorVersion = property("java.version").toString().toInt()
java {
    withSourcesJar()

    javaMajorVersion
        .let { JavaVersion.values()[it - 1] }
        .let {
            sourceCompatibility = it
            targetCompatibility = it
        }
}

tasks.withType<JavaCompile> {
    options.release = javaMajorVersion
}

publishMods {
    displayName.set("Controlify $versionWithoutMC for MC $mcVersion")
    file.set(tasks.remapJar.get().archiveFile)
    additionalFiles.setFrom(offlineRemapJar.get().archiveFile)
    changelog.set(
        rootProject.file("changelogs/${versionWithoutMC}.md")
            .takeIf { it.exists() }
            ?.readText()
            ?: "No changelog provided."
    )
    type.set(when {
        isAlpha -> ALPHA
        isBeta -> BETA
        else -> STABLE
    })
    modLoaders.add("fabric")

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

            requires { slug.set("fabric-api") }
            requires { slug.set("yacl") }
            optional { slug.set("modmenu") }
        }
    }

    val curseforgeId: String by project
    if (curseforgeId.isNotBlank() && hasProperty("curseforge.token")) {
        curseforge {
            projectId.set(curseforgeId)
            accessToken.set(findProperty("curseforge.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.curseMC"))

            requires { slug.set("fabric-api") }
            requires { slug.set("yacl") }
            optional { slug.set("modmenu") }
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
