import dev.isxander.stonecutterconfigurator.isExperimental

plugins {
    id("dev.isxander.modstitch.base")
    id("me.modmuss50.mod-publish-plugin")
    `maven-publish`

    id("dev.kikugie.postprocess.j52j") version "2.1-beta.3"
}

// version stuff
val mcVersion = property("mcVersion")!!.toString()
val mcSemverVersion = stonecutter.current.version

// loader stuff
val isFabric = modstitch.isLoom
val isNeoforge = modstitch.isModDevGradleRegular
val isForge = modstitch.isModDevGradleLegacy
val isForgeLike = modstitch.isModDevGradle
val loader = when {
    isFabric -> "fabric"
    isNeoforge -> "neoforge"
    isForge -> "forge"
    else -> error("Unknown loader")
}

val versionWithoutMC = property("modVersion")!!.toString()

modstitch {
    minecraftVersion = mcVersion

    // ideally, we use 17 for everything to tell IDE about the language features that are available
    // on the lowest common denominator: 17. However, Forge versions that use a java 21 MC version
    // won't compile on Java 17, so we need to use 21 for those.
    val mcIsJava21 = stonecutter.eval(mcSemverVersion, ">1.20.4")
    javaTarget = if (mcIsJava21 && isForgeLike) 21 else 17

    parchment {
        prop("parchment.version") { mappingsVersion = it }
        prop("parchment.minecraft") { minecraftVersion = it }
    }

    metadata {
        fun prop(property: String, block: (String) -> Unit) {
            prop(property, ifNull = {""}) { block(it) }
        }

        modId = "controlify"
        modName = "Controlify"
        modVersion = "$versionWithoutMC+${stonecutter.current.project}"
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
        prop("deps.fabricLoader", required = true) { fabricLoaderVersion = it }

        configureLoom {
            runConfigs.all {
                ideConfigGenerated(false)
                vmArg("-Dsodium.checks.issue2561=false")
            }
        }
    }

    moddevgradle {
        enable {
            prop("deps.neoForge") { neoForgeVersion = it }
            prop("deps.forge") { forgeVersion = it }
        }

        defaultRuns()
        configureNeoforge {
            runs.all {
                disableIdeRun()
            }
        }
    }

    /*
    Dynamically add mixins based on this target's supported mods.
     */
    mixin {
        addMixinsToModManifest = true

        configs.register("controlify")
        if (isPropDefined("deps.iris")) configs.register("controlify-compat.iris")
        if (isPropDefined("deps.sodium")) configs.register("controlify-compat.sodium")
        if (isPropDefined("deps.reesesSodiumOptions")) configs.register("controlify-compat.reeses-sodium-options")
        configs.register("controlify-compat.yacl")
        if (isPropDefined("deps.simpleVoiceChat")) configs.register("controlify-compat.simple-voice-chat")
        if (isFabric) configs.register("controlify-platform.fabric")
        if (isNeoforge) configs.register("controlify-platform.neoforge")
    }
}

/*
Prod run environment from loom!
MDG targets don't need this, they run with named namespace.
 */
val productionMods: Configuration by configurations.creating {
    isTransitive = false
}
if (isFabric) {
    @Suppress("UnstableApiUsage")
    val runProdClient by tasks.registering(net.fabricmc.loom.task.prod.ClientProductionRunTask::class) {
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
createActiveTask(taskName = "runClient")
createActiveTask(taskName = "runProdClient")

/*
Setup test harness for the project.
 */
val testharness by sourceSets.registering {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}
modstitch.createProxyConfigurations(testharness.get())

/*
Setup stonecutter for the project.
 */
stonecutter {
    consts(
        "fabric" to modstitch.isLoom,
        "neoforge" to modstitch.isModDevGradleRegular,
        "forge" to modstitch.isModDevGradleLegacy,
        "forgelike" to modstitch.isModDevGradle,
    )

    val sodiumSemver = findProperty("deps.sodiumSemver")?.toString() ?: "0.0.0"
    dependencies(
        "fapi" to (findProperty("deps.fabricApi")?.toString() ?: "0.0.0"),
        "sodium" to sodiumSemver
    )

    // sodium repackaged in 0.6, this allows quick swapping imports
    swaps["sodium-package"] = if (eval(sodiumSemver, ">=0.6"))
        "net.caffeinemc.mods.sodium" else "me.jellysquid.mods.sodium"
}

dependencies {
    fun Dependency?.jij() = this?.also(::modstitchJiJ)
    fun Dependency?.productionMod() = this?.also { productionMods(it) }

    prop("deps.mixinExtras") {
        when {
            isFabric -> modstitchImplementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:$it")!!).jij()
            isNeoforge -> implementation("io.github.llamalad7:mixinextras-neoforge:$it").jij()
            isForge -> implementation("io.github.llamalad7:mixinextras-forge:$it").jij()
            else -> error("Unknown loader")
        }
    }

    fun modDependency(
        id: String,
        artifactGetter: (String) -> String,
        requiredByDependants: Boolean = false,
        supportsRuntime: Boolean = true,
        extra: (Boolean) -> Unit = {}
    ) {
        prop("deps.$id") { modVersion ->
            val noRuntime = prop("deps.$id.noRuntime") { it.toBoolean() } == true
            require(noRuntime || supportsRuntime) { "No runtime is not supported for $id" }

            val configuration = if (requiredByDependants) {
                if (noRuntime) "modstitchModCompileOnlyApi" else "modstitchModApi"
            } else {
                if (noRuntime) "modstitchModCompileOnly" else "modstitchModImplementation"
            }

            artifactGetter(modVersion).let {
                configuration(it)
                if (!noRuntime) productionMods(it)
            }

            extra(!noRuntime)
        }
    }

    if (isFabric) {
        modDependency("fabricApi", { "net.fabricmc.fabric-api:fabric-api:$it" }, requiredByDependants = true)

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

    "testharnessImplementation"(sourceSets.main.get().output)
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

tasks.generateModMetadata {
    eachFile {
        // don't include photoshop files for the textures for development
        if (name.endsWith(".psd")) {
            exclude()
        }
    }
}

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

    displayName = "$versionWithoutMC for $loader $mcVersion"
    modLoaders.add(loader)

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    // modrinth and curseforge use different formats for snapshots. this can be expressed globally
    val stableMCVersions = versionList("pub.stableMC")

    val modrinthId: String by project
    if (modrinthId.isNotBlank() && hasProperty("modrinth.token") && !isExperimental) {
        modrinth {
            projectId.set(modrinthId)
            accessToken.set(findProperty("modrinth.token")?.toString())
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.modrinthMC"))

            announcementTitle = "Download $mcVersion for ${loader.replaceFirstChar { it.uppercase() }} from Modrinth"

            requires { slug.set("yacl") }

            if (isFabric) {
                requires { slug.set("fabric-api") }
                optional { slug.set("modmenu") }
            }
        }
    }

    val curseforgeId: String by project
    if (curseforgeId.isNotBlank() && hasProperty("curseforge.token") && !isExperimental) {
        curseforge {
            projectId = curseforgeId
            projectSlug = findProperty("curseforgeSlug")!!.toString()
            accessToken = findProperty("curseforge.token")?.toString()
            minecraftVersions.addAll(stableMCVersions)
            minecraftVersions.addAll(versionList("pub.curseMC"))

            announcementTitle = "Download $mcVersion for ${loader.replaceFirstChar { it.uppercase() }} from CurseForge"

            requires { slug.set("yacl") }

            if (isFabric) {
                requires { slug.set("fabric-api") }
                optional { slug.set("modmenu") }
            }
        }
    }

    val githubProject: String by project
    if (githubProject.isNotBlank() && hasProperty("github.token") && !isExperimental) {
        github {
            accessToken = findProperty("github.token")?.toString()

            // will upload files to this parent task
            parent(rootProject.tasks.named("publishGithub"))
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
    repositories {
        val username = prop("XANDER_MAVEN_USER") { it }
        val password = prop("XANDER_MAVEN_PASS") { it }
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

fun <T> prop(property: String, required: Boolean = false, ifNull: () -> String? = { null }, block: (String) -> T?): T? {
    return ((System.getenv(property) ?: findProperty(property)?.toString())
        ?.takeUnless { it.isBlank() }
        ?: ifNull())
        .let { if (required && it == null) error("Property $property is required") else it }
        ?.let(block)
}

fun isPropDefined(property: String): Boolean {
    return prop(property) { true } == true
}

fun createActiveTask(
    taskProvider: TaskProvider<*>? = null,
    taskName: String? = null,
    internal: Boolean = false
): String {
    val taskExists = taskProvider != null || taskName!! in tasks.names
    val task = taskProvider ?: taskName?.takeIf { taskExists }?.let { tasks.named(it) }
    val taskName = when {
        taskProvider != null -> taskProvider.name
        taskName != null -> taskName
        else -> error("Either taskProvider or taskName must be provided")
    }
    val activeTaskName = "${taskName}Active"

    if (stonecutter.current.isActive) {
        rootProject.tasks.register(activeTaskName) {
            group = "controlify${if (internal) "/versioned" else ""}"

            task?.let { dependsOn(it) }
        }
    }

    return activeTaskName
}
