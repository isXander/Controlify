import dev.isxander.controlify.*

plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.controlify.project")

    id("me.modmuss50.mod-publish-plugin")
    `maven-publish`

    id("dev.kikugie.postprocess.j52j") version "2.1-beta.3"
}

val loader = when {
    modstitch.isLoom -> "fabric"
    modstitch.isModDevGradle -> "neoforge"
    else -> error("Unknown loader")
}

modstitch {
    metadata {
        modVersion = "${project.modVersion}+${stonecutter.current.project}"
        modId = "controlify"
        modName = "Controlify"
    }

    mixin {
        addMixinsToModManifest = true

        configs.register("controlify")
        if (isPropDefined("deps.iris")) configs.register("controlify-compat.iris")
        if (isPropDefined("deps.sodium")) configs.register("controlify-compat.sodium")
        if (isPropDefined("deps.reesesSodiumOptions")) configs.register("controlify-compat.reeses-sodium-options")
        configs.register("controlify-compat.yacl")
        if (isPropDefined("deps.simpleVoiceChat")) configs.register("controlify-compat.simple-voice-chat")
        if (modstitch.isLoom) configs.register("controlify-platform.fabric")
        if (modstitch.isModDevGradleRegular) configs.register("controlify-platform.neoforge")
    }

    loom {
        configureLoom {
            accessWidenerPath = rootProject.file("src/main/resources/controlify.accesswidener")
        }

        tasks.getByName("validateAccessWidener").enabled = false
    }

    moddevgradle {
        configureNeoforge {
            validateAccessTransformers = false
        }
    }
}

dependencies {
    fun Dependency?.jij() = this?.also(::modstitchJiJ)
    fun Dependency?.productionMod() = this?.also { "productionMods"(it) }

    propMap("deps.mixinExtras") {
        when {
            modstitch.isLoom -> modstitchApi("io.github.llamalad7:mixinextras-fabric:$it").jij()
            modstitch.isModDevGradleRegular -> api("io.github.llamalad7:mixinextras-neoforge:$it").jij()
            else -> error("Unknown loader")
        }
    }

    fun modDependency(
        id: String,
        artifactGetter: (String) -> String,
        api: Boolean = false,
        supportsRuntime: Boolean = true,
        extra: (Boolean) -> Unit = {}
    ) {
        propMap("deps.$id") { modVersion ->
            val noRuntime = propMap("deps.$id.noRuntime") { it.toBoolean() } == true
            require(noRuntime || supportsRuntime) { "No runtime is not supported for $id" }

            val configuration = if (api) {
                if (noRuntime) "modstitchModCompileOnlyApi" else "modstitchModApi"
            } else {
                if (noRuntime) "modstitchModCompileOnly" else "modstitchModImplementation"
            }

            artifactGetter(modVersion).let {
                configuration(it)
                if (!noRuntime) "productionMods"(it)
            }

            extra(!noRuntime)
        }
    }

    if (modstitch.isLoom) {
        modDependency("fabricApi", { "net.fabricmc.fabric-api:fabric-api:$it" }, api = true)

        // mod menu compat
        modDependency("modMenu", { "com.terraformersmc:modmenu:$it" })
    }

    modstitchModApi("dev.isxander:yet-another-config-lib:${property("deps.yacl")}") {
        // was including old fapi version that broke things at runtime
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
        exclude(group = "thedarkcolour")
    }.productionMod()

    // bindings for SDL3
    modstitchApi("dev.isxander:libsdl4j:${property("deps.sdl3Target")}-${property("deps.sdl34jBuild")}")
        .jij()

    // steam deck bindings
    modstitchApi("dev.isxander:steamdeck4j:${property("deps.steamdeck4j")}")
        .jij()

    // used to identify controller PID/VID when SDL is not available
    modstitchApi("org.hid4java:hid4java:${property("deps.hid4java")}")
        .jij()

    // Already included by YetAnotherConfigLib, but we need it too, so let's define explicit dep
    api("org.quiltmc.parsers:json:${property("deps.quiltparsers")}")

    // sodium compat
    modDependency("sodium", { "maven.modrinth:sodium:$it" })
    // RSO compat
    modDependency("reesesSodiumOptions", { "maven.modrinth:reeses-sodium-options:$it" })
    // iris compat
    modDependency("iris", { "maven.modrinth:iris:$it" }) { runtime ->
        if (runtime) {
            modstitchModLocalRuntime("org.anarres:jcpp:1.4.14")
            modstitchModLocalRuntime("io.github.douira:glsl-transformer:2.0.0-pre13")
        }
    }
    // immediately-fast compat
    modDependency("immediatelyFast", { "maven.modrinth:immediatelyfast:$it" }) { runtime ->
        if (runtime) {
            modstitchModLocalRuntime("net.lenni0451:Reflect:1.1.0")
        }
    }
    // simple-voice-chat compat
    modDependency("simpleVoiceChat", { "maven.modrinth:simple-voice-chat:$it" })
    // fancy menu compat
    modDependency("fancyMenu", { "maven.modrinth:fancymenu:$it" }, supportsRuntime = false)
}

j52j {
    prettyPrint = true
}

/*
START
Set up the configuration to put native hashes in the jar
 */
val offlineJar by tasks.registering(Jar::class) {
    group = "controlify/versioned/internal"

    // include the contents of the regular jar
    val inputJar = modstitch.finalJarTask
    from(zipTree(inputJar.flatMap { it.archiveFile }))
    dependsOn(inputJar)

    // set the classifier
    archiveClassifier.set("offline")
}
tasks.assemble { dependsOn(offlineJar) }

class NativeTarget(
    val classifier: String,
    val fileExtension: String,
    val jnaPrefix: String,
    val fileName: String,
    configuration: String,
) {
    val configurationName = "offlineNative$configuration"
}
val nativeTargets = listOf(
    NativeTarget(classifier = "linux-aarch64", fileExtension = "so", jnaPrefix = "linux-aarch64/", fileName = "libSDL3", configuration = "LinuxAarch64"),
    NativeTarget(classifier = "linux-x86_64", fileExtension = "so", jnaPrefix = "linux-x86-64/", fileName = "libSDL3", configuration = "LinuxX86_64"),
    NativeTarget(classifier = "macos-universal", fileExtension = "dylib", jnaPrefix = "darwin-aarch64/", fileName = "libSDL3", configuration = "MacArm"),
    NativeTarget(classifier = "macos-universal", fileExtension = "dylib", jnaPrefix = "darwin-x86-64/", fileName = "libSDL3", configuration = "MacIntel"),
    NativeTarget(classifier = "windows-x86", fileExtension = "dll", jnaPrefix = "win32-x86/", fileName = "SDL3", configuration = "WinX86"),
    NativeTarget(classifier = "windows-x86_64", fileExtension = "dll", jnaPrefix = "win32-x86-64/", fileName = "SDL3", configuration = "WinX86_64"),
)

val nativeHashConfiguration = configurations.create("nativeHashes")

nativeTargets.forEach { target ->
    val nativesConfiguration = configurations.create(target.configurationName)

    dependencies {
        nativesConfiguration("dev.isxander:libsdl4j-natives:${property("deps.sdl3Target")}:${target.classifier}@${target.fileExtension}")
        nativeHashConfiguration("dev.isxander:libsdl4j-natives:${property("deps.sdl3Target")}:${target.classifier}@${target.fileExtension}.md5")
    }

    offlineJar {
        from(nativesConfiguration) {
            into(target.jnaPrefix)
            rename { "${target.fileName}.${target.fileExtension}" }
        }
    }
}

tasks.jar {
    from(nativeHashConfiguration) {
        into("sdl3-hashes/")
    }
}
/*
END
Set up the configuration to put native hashes in the jar
 */


val releaseModVersion by tasks.registering {
    group = "controlify/versioned"

    dependsOn("publishMods")

    if (!project.publishMods.dryRun.get()) {
        dependsOn("publish")
    }
}
createActiveTask(releaseModVersion)

val finalJarTasks = listOf(
    offlineJar,
    modstitch.finalJarTask,
)

val buildAndCollect by tasks.registering(Copy::class) {
    group = "controlify/versioned"

    finalJarTasks.forEach { jar ->
        dependsOn(jar)
        from(jar.flatMap { it.archiveFile })
    }

    into(rootProject.layout.buildDirectory.dir("finalJars"))
}

createActiveTask(buildAndCollect)

publishMods {
    from(rootProject.publishMods)
    dryRun = rootProject.publishMods.dryRun

    file = modstitch.finalJarTask.flatMap { it.archiveFile }
    additionalFiles.setFrom(offlineJar.map { it.archiveFile })

    displayName = "$modVersion for $loader $mcVersion"
    modLoaders.add(loader)

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = versionList("pub.stableMC")

    val modrinthId: String by project
    if (modrinthId.isNotBlank() && hasProperty("modrinth.token") && !isExperimentalBuild) {
        modrinth {
            projectId.set(modrinthId)
            accessToken.set(findProperty("modrinth.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.modrinthMC"))

            announcementTitle = "Download $mcVersion for ${loader.replaceFirstChar { it.uppercase() }} from Modrinth"

            requires { slug.set("yacl") }

            if (modstitch.isLoom) {
                requires { slug.set("fabric-api") }
                optional { slug.set("modmenu") }
            }
        }
    }

    val curseforgeId: String by project
    if (curseforgeId.isNotBlank() && hasProperty("curseforge.token") && !isExperimentalBuild) {
        curseforge {
            projectId = curseforgeId
            projectSlug = findProperty("curseforgeSlug")!!.toString()
            accessToken = findProperty("curseforge.token")?.toString()
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.curseMC"))

            announcementTitle = "Download $mcVersion for ${loader.replaceFirstChar { it.uppercase() }} from CurseForge"

            requires { slug.set("yacl") }

            if (modstitch.isLoom) {
                requires { slug.set("fabric-api") }
                optional { slug.set("modmenu") }
            }
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("mod") {
            from(components["java"])

            artifactId = "controlify"
            groupId = "dev.isxander"
        }
    }
}
