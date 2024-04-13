import de.undercouch.gradle.tasks.download.Download

plugins {
    `java-library`

    id("fabric-loom") version "1.6.+"

    id("me.modmuss50.mod-publish-plugin") version "0.5.+"
    `maven-publish`

    id("io.github.p03w.machete") version "2.+"
    id("org.ajoberstar.grgit") version "5.0.+"
    id("de.undercouch.download") version "5.5.0"
}

val mcVersion = stonecutter.current.version // get this builds minecraft version
val mcDep = property("fmj.mcDep").toString()

group = "dev.isxander"
val versionWithoutMC = "2.0.0-beta.2"
version = "$versionWithoutMC+${stonecutter.current.project}"

val isAlpha = "alpha" in version.toString()
val isBeta = "beta" in version.toString()
if (isAlpha) println("Controlify alpha version detected.")
if (isBeta) println("Controlify beta version detected.")

base {
    archivesName.set(property("modName").toString())
}

// add custom expressions to stonecutter to allow optional dependencies at build-time
stonecutter.expression {
    when (it) {
        "immediately-fast" -> isPropDefined("deps.immediatelyFast")
        "iris" -> isPropDefined("deps.iris")
        "mod-menu" -> isPropDefined("deps.modMenu")
        "sodium" -> isPropDefined("deps.sodium")
        "simple-voice-chat" -> isPropDefined("deps.simpleVoiceChat")
        else -> null
    }
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
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://jitpack.io")
    maven("https://maven.flashyreese.me/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.project}")
    mappings(loom.layered {
        // quilt does not support pre-releases so it is necessary to only layer if they exist
        optionalProp("deps.quiltMappings") {
            mappings("org.quiltmc:quilt-mappings:$mcVersion+build.$it:intermediary-v2")
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
    ).forEach {
        modImplementation(fabricApi.module(it, fapiVersion))
    }
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fapiVersion") // so you can do `depends: fabric-api` in FMJ

    modApi("dev.isxander.yacl:yet-another-config-lib-fabric:${property("deps.yacl")}") {
        // was including old fapi version that broke things at runtime
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
    }

    // bindings for SDL3
    api(include("dev.isxander:libsdl4j:${property("deps.sdl34j")}")!!)

    // used to identify controller PID/VID when SDL is not available
    api(include("org.hid4java:hid4java:${property("deps.hid4java")}")!!)

    // used to parse hiddb.json5
    api(include("org.quiltmc:quilt-json5:${property("deps.quiltJson5")}")!!)

    // mod menu compat
    optionalProp("deps.modMenu") {
        modImplementation("com.terraformersmc:modmenu:$it")
    }

    // sodium compat
    optionalProp("deps.sodium") {
        modImplementation("maven.modrinth:sodium:$it")

        // sodium needs more runtime fapi modules
        // modrinth maven is obvi not transitive
        listOf(
            "fabric-rendering-fluids-v1",
            "fabric-rendering-data-attachment-v1",
        ).forEach { module ->
            modRuntimeOnly(fabricApi.module(module, fapiVersion))
        }
    }
    // iris compat
    optionalProp("deps.iris") {
        modCompileOnly("maven.modrinth:iris:$it")

        // only necessary if above ^^ is in runtime
        // modRuntimeOnly("org.anarres:jcpp:1.4.14")
        // modRuntimeOnly("io.github.douira:glsl-transformer:2.0.0-pre13")
    }
    // immediately-fast compat
    optionalProp("deps.immediatelyFast") {
        modImplementation("maven.modrinth:immediatelyfast:$it")
        modRuntimeOnly("net.lenni0451:Reflect:1.1.0")
    }

    // simple-voice-chat compat
    optionalProp("deps.simpleVoiceChat") {
        modCompileOnly("maven.modrinth:simple-voice-chat:$it")
    }
}

// download the most up to date controller database for SDL2
val downloadHidDb by tasks.registering(Download::class) {
    finalizedBy("convertHidDBToSDL3")

    group = "mod"

    src("https://raw.githubusercontent.com/gabomdq/SDL_GameControllerDB/master/gamecontrollerdb.txt")
    dest("src/main/resources/assets/controlify/controllers/gamecontrollerdb-sdl2.txt")
}

// SDL3 renamed `Mac OS X` -> `macOS` and this change carried over to mappings
val convertHidDBToSDL3 by tasks.registering(Copy::class) {
    mustRunAfter(downloadHidDb)
    dependsOn(downloadHidDb)

    group = "mod"

    val file = downloadHidDb.get().outputs.files.singleFile
    from(file)
    into(file.parent)

    rename { "gamecontrollerdb-sdl3.txt" }
    filter { it.replace("Mac OS X", "macOS") }
}

tasks {
    processResources {
        val modId: String by project
        val modName: String by project
        val modDescription: String by project
        val githubProject: String by project

        val props = mapOf(
            "id" to modId,
            "group" to project.group,
            "name" to modName,
            "description" to modDescription,
            "version" to project.version,
            "github" to githubProject,
            "mc" to mcDep
        )

        props.forEach(inputs::property)

        filesMatching("fabric.mod.json") {
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

machete {
    // don't minify fabric.mod.json and mixin file
    json.enabled.set(false)
}

val javaMajorVersion = property("java.version").toString().toInt()
java {
    withSourcesJar()
    withJavadocJar()

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

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = listOf(stonecutter.current.project)

    val modrinthId: String by project
    if (modrinthId.isNotBlank() && hasProperty("modrinth.token")) {
        modrinth {
            projectId.set(modrinthId)
            accessToken.set(findProperty("modrinth.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)

            requires { slug.set("fabric-api") }
            requires { slug.set("yacl") }
            optional { slug.set("modmenu") }
        }

        tasks.getByName("publishModrinth") {
            dependsOn("optimizeOutputsOfRemapJar")
        }
    }

    val curseforgeId: String by project
    if (curseforgeId.isNotBlank() && hasProperty("curseforge.token")) {
        curseforge {
            projectId.set(curseforgeId)
            accessToken.set(findProperty("curseforge.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)

            requires { slug.set("fabric-api") }
            requires { slug.set("yacl") }
            optional { slug.set("modmenu") }
        }

        tasks.getByName("publishCurseforge") {
            dependsOn("optimizeOutputsOfRemapJar")
        }
    }

    val githubProject: String by project
    if (githubProject.isNotBlank() && hasProperty("github.token")) {
        github {
            repository.set(githubProject)
            accessToken.set(findProperty("github.token")?.toString())
            commitish.set(grgit.branch.current().name)
        }

        tasks.getByName("publishGithub") {
            dependsOn("optimizeOutputsOfRemapJar")
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
            tasks.getByName("publishModPublicationToXanderReleasesRepository") {
                dependsOn("optimizeOutputsOfRemapJar")
            }
        } else {
            println("Xander Maven credentials not satisfied.")
        }
    }
}

tasks.getByName("generateMetadataFileForModPublication") {
    dependsOn("optimizeOutputsOfRemapJar")
}

fun <T> optionalProp(property: String, block: (String) -> T?) {
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)
}

fun isPropDefined(property: String): Boolean {
    return property(property)?.toString()?.isNotBlank() ?: false
}
