import dev.isxander.controlify.*

plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.controlify.project")

    id("me.modmuss50.mod-publish-plugin")
    `maven-publish`
    id("com.gradleup.nmcp")

    id("dev.kikugie.postprocess.j52j") version "2.1-beta.3"
}

val loader = when {
    modstitch.isLoom -> "fabric"
    modstitch.isModDevGradle -> "neoforge"
    else -> error("Unknown loader")
}

modstitch {
    metadata {
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
}

dependencies {
    fun Dependency?.jij() = this?.also(::modstitchJiJ)
    fun Dependency?.productionMod() = this?.also { "productionMods"(it) }

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
        exclude(group = "thedarkcolour")
    }.productionMod().jij()

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

    if (stonecutter.current.parsed < "1.21.11") {
        compileOnly("org.jspecify:jspecify:1.0.0")
    }

    // sodium compat
    when {
        modstitch.isLoom -> {
            modDependency("sodium", { "net.caffeinemc:sodium-fabric:$it" })
        }
        modstitch.isModDevGradle -> {
            modDependency("sodium", { "net.caffeinemc:sodium-neoforge:$it" })
            modDependency("sodium", { "net.caffeinemc:sodium-neoforge-mod:$it" })
        }
    }

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
Include native libraries for SDL3 in the jar.
 */
data class NativeTarget(
    val classifier: String,
    val fileExtension: String,
    val jnaPrefix: String,
    val fileName: String,
    val configurationName: String,
)

val nativeTargets = listOf(
    NativeTarget(classifier = "linux-aarch64", fileExtension = "so", jnaPrefix = "linux-aarch64/", fileName = "libSDL3", configurationName = "offlineNativeLinuxAarch64"),
    NativeTarget(classifier = "linux-x86_64", fileExtension = "so", jnaPrefix = "linux-x86-64/", fileName = "libSDL3", configurationName = "offlineNativeLinuxX86_64"),
    NativeTarget(classifier = "macos-aarch64", fileExtension = "dylib", jnaPrefix = "darwin-aarch64/", fileName = "libSDL3", configurationName = "offlineNativeMacArm"),
    NativeTarget(classifier = "macos-x86_64", fileExtension = "dylib", jnaPrefix = "darwin-x86-64/", fileName = "libSDL3", configurationName = "offlineNativeMacIntel"),
    NativeTarget(classifier = "windows-x86_64", fileExtension = "dll", jnaPrefix = "win32-x86-64/", fileName = "SDL3", configurationName = "offlineNativeWinX86_64"),
)

// Create configurations
val nativeConfigurations = nativeTargets.associate { target ->
    target.configurationName to configurations.create(target.configurationName)
}
val nativeHashConfiguration: Configuration = configurations.create("nativeHashes")

nativeTargets.forEach { target ->
    dependencies {
        nativeConfigurations[target.configurationName]!!("dev.isxander:libsdl4j-natives:${property("deps.sdl3Target")}:${target.classifier}@${target.fileExtension}")
        nativeHashConfiguration("dev.isxander:libsdl4j-natives:${property("deps.sdl3Target")}:${target.classifier}@${target.fileExtension}.md5")
    }
}

val prepareNatives = tasks.register<Sync>("prepareNativeResources") {
    group = "controlify/internal"

    into(layout.buildDirectory.dir("generated-resources/sdl-natives"))

    nativeTargets.forEach { target ->
        from(configurations.named(target.configurationName)) {
            into(target.jnaPrefix)
            rename { "${target.fileName}.${target.fileExtension}" }
        }
    }
    from(configurations.named("nativeHashes")) {
        into("sdl3-hashes/")
    }
}

sourceSets {
    main {
        resources.srcDir(prepareNatives.map { it.destinationDir })
    }
}

tasks.processResources {
    dependsOn(prepareNatives)
}
/*
END
Include native libraries for SDL3 in the jar.
 */


val releaseModVersion by tasks.registering {
    group = "controlify/versioned"
    dependsOn("publishMods")
}
createActiveTask(releaseModVersion)

if (project.isPublishingEnabled) {
    rootProject.tasks.named("releaseModVersions") {
        dependsOn(releaseModVersion)
    }
}

val finalJarTasks = listOf(
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

    displayName = "$modVersion for $loader $mcVersion"
    modLoaders.add(loader)

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = versionList("pub.stableMC")

    modrinth {
        accessToken = secrets.gradleProperty("modrinth.accessToken")

        projectId = providers.gradleProperty("pub.modrinthId")

        minecraftVersions.addAll(stableMCVersions)
        minecraftVersions.addAll(versionList("pub.modrinthMC"))

        announcementTitle = "Download $mcVersion for ${loader.replaceFirstChar { it.uppercase() }} from Modrinth"

        requires { slug.set("yacl") }

        if (modstitch.isLoom) {
            requires { slug.set("fabric-api") }
            optional { slug.set("modmenu") }
        }
    }

    curseforge {
        accessToken = secrets.gradleProperty("curseforge.accessToken")

        projectId = providers.gradleProperty("pub.curseforgeId")
        projectSlug = providers.gradleProperty("pub.curseforgeSlug")

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
publishing {
    publications {
        create<MavenPublication>("mod") {
            from(components["java"])

            artifactId = "controlify"
            groupId = "dev.isxander"

            pom {
                name = modstitch.metadata.modName
                description = modstitch.metadata.modDescription
                url = "https://www.isxander.dev/projects/controlify"
                licenses {
                    license {
                        name = "LGPL-3.0-or-later"
                        url = "https://www.gnu.org/licenses/lgpl-3.0.en.html"
                    }
                }
                developers {
                    developer {
                        id = "isXander"
                        name = "Xander"
                        email = "business@isxander.dev"
                    }
                }
                scm {
                    url = "https://github.com/isXander/Controlify"
                    connection = "scm:git:git//github.com/isXander/Controlify.git"
                    developerConnection = "scm:git:ssh://git@github.com/isXander/Controlify.git"
                }
            }
        }
    }
}
signing {
    sign(publishing.publications["mod"])
}
