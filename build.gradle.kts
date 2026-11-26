@file:Suppress("UnstableApiUsage")

plugins {
    id("controlify-common")

    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.neoforged.gradle.userdev) apply false
    alias(libs.plugins.modstitch.multiloader)
    alias(libs.plugins.modstitch.manifests)
    alias(libs.plugins.mod.publish.plugin)
}

val modVersion = providers.gradleProperty("mod.version").get()
val minecraftVersion = property("dep.minecraft")!!.toString()
version = "$modVersion+mc$minecraftVersion"

base.archivesName = "controlify"

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

val fabricModDependency = configurations.dependencyScope("fabricModDependency")
configurations.fabricCompileOnly { extendsFrom(fabricModDependency) }
configurations.fabricLocalRuntime { extendsFrom(fabricModDependency) }

val neoforgeModDependency = configurations.dependencyScope("neoforgeModDependency")
configurations.neoforgeCompileOnly { extendsFrom(neoforgeModDependency) }
configurations.neoforgeLocalRuntime { extendsFrom(neoforgeModDependency) }

val fabricApiBom = dependencies.platform("net.fabricmc.fabric-api:fabric-api-bom:${property("dep.fapi")}")

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    fabricLoader(libs.fabric.loader)

	ifPresent("dep.neoforge") {
		neoforgeImplementation("net.neoforged:neoforge:${property("dep.neoforge")}")
	}

    implementation(fabricApiBom)
	fabricImplementation(fabricApiBom)
	fabricImplementation("net.fabricmc.fabric-api:fabric-resource-loader-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-networking-api-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-command-api-v2")
	fabricImplementation("net.fabricmc.fabric-api:fabric-lifecycle-events-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-screen-api-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-rendering-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-creative-tab-api-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-key-mapping-api-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-tag-api-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-convention-tags-v2")
	// this needs to be on main because fabric is shared with main jar
	implementation("net.fabricmc.fabric-api:fabric-transitive-access-wideners-v1")
    fabricLocalRuntime("net.fabricmc.fabric-api:fabric-api")

    commonApi(libs.sdl.java.api)
    commonInclude(libs.sdl.java.api)
    commonApi(libs.sdl.java.backend.ffm)
    commonInclude(libs.sdl.java.backend.ffm)

	commonApi(libs.steamworks)
	commonInclude(libs.steamworks)

    commonApi(libs.quilt.parsers.json)

    compileOnlyApi("dev.isxander:yet-another-config-lib:${property("dep.yacl")}") {
		exclude(group = "net.fabricmc.fabric-api")
	}
    fabricApi("dev.isxander:yet-another-config-lib:${property("dep.yacl")}")
    neoforgeApi("dev.isxander:yet-another-config-lib:${property("dep.yacl-neoforge")}")

	ifPresent("dep.mod-menu") {
		fabricCompileOnly("maven.modrinth:modmenu:$it")
	}

    ifPresent("dep.sodium") {
        compileOnly("net.caffeinemc:sodium-fabric:$it") {
			exclude(group = "net.fabricmc.fabric-api")
		}
        fabricModDependency("net.caffeinemc:sodium-fabric:$it")
        neoforgeModDependency("net.caffeinemc:sodium-neoforge:$it")
		neoforgeModDependency("net.caffeinemc:sodium-neoforge-mod:$it")
    }

    ifPresent("dep.iris") {
        compileOnly("maven.modrinth:iris:$it")
		fabricCompileOnly("maven.modrinth:iris:$it")
    }
    ifPresent("dep.iris-neoforge") {
        neoforgeCompileOnly("maven.modrinth:iris:$it")
    }

    ifPresent("dep.svc") {
        compileOnly("maven.modrinth:simple-voice-chat:$it")
        fabricCompileOnly("maven.modrinth:simple-voice-chat:$it")
    }
    ifPresent("dep.svc-neoforge") {
        neoforgeCompileOnly("maven.modrinth:simple-voice-chat:$it")
    }

    ifPresent("dep.fancy-menu") {
        compileOnly("maven.modrinth:fancymenu:$it")
        fabricCompileOnly("maven.modrinth:fancymenu:$it")
    }
    ifPresent("dep.fancy-menu-neoforge") {
        neoforgeCompileOnly("maven.modrinth:fancymenu:$it")
    }
}

/// Stonecutter

stonecutter {
    constants {
        put("iris", hasProperty("dep.iris"))
        put("mod_menu", hasProperty("dep.mod-menu"))
        put("sodium", hasProperty("dep.sodium"))
        put("simple_voice_chat", hasProperty("dep.svc"))
        put("fancy_menu", hasProperty("dep.fancy-menu"))
    }
}

/// Metadata file generation

val minecraftRange = property("meta.minecraft-range")!!.toString()
val supportedMinecraftVersions = manifests.minecraftReleasesMatching(minecraftRange)

val commonManifest = manifests.manifest {
    modId = providers.gradleProperty("mod.id")
    version = project.version.toString()
    displayName = providers.gradleProperty("mod.name")
    description = providers.gradleProperty("mod.description")
    authors.add("isXander")
    iconPath = "icon.png"
    licenses.add("LGPL-3.0-or-later")
    issueTrackerUrl = providers.gradleProperty("mod.issuesUrl")
    sourcesUrl = providers.gradleProperty("mod.sourcesUrl")
    homepage = sourcesUrl

    mixin("controlify.mixins.json")
    mixin("controlify-compat.yacl.mixins.json")
    ifPresent("dep.iris") { mixin("controlify-compat.iris.mixins.json") }
    ifPresent("dep.sodium") { mixin("controlify-compat.sodium.mixins.json") }
    ifPresent("dep.svc") { mixin("controlify-compat.simple-voice-chat.mixins.json") }
    mixin("controlify-compat.rrls.mixins.json")

    dependency("minecraft", REQUIRED, minecraftRange)
    dependency("yet_another_config_lib_v3", REQUIRED, "*")
}
manifests {
    val rrlsData = mapOf<String, Any>("rrls" to mapOf(
        // must match the getReloadId() of every ControlifyReloadListener
        "force_load" to listOf(
            "controlify:default_config",
            "controlify:controller_type",
            "controlify:default_binds",
            "controlify:contextual/domain/in_game",
            "controlify:contextual/domain/container",
            "controlify:contextual/ruleset/guide",
            "controlify:contextual/ruleset/trigger_effect",
            "controlify:contextual/ruleset/feedback",
            "controlify:activators",
            "controlify:controller_mapping",
            "controlify:feedback_groups",
            "controlify:feedback_effects",
            "controlify:radial_icons",
            "controlify:font_mappings",
            "controlify:keyboard_layout",
        )
    ))

    fabricModJson(sourceSets.fabric.get()) {
        from(commonManifest)

        entrypoint("modmenu", "dev.isxander.controlify.fabric.compat.ModMenuIntegration")
        entrypoint("main", "dev.isxander.controlify.fabric.impl.ControlifyBootstrap")
        entrypoint("client", "dev.isxander.controlify.fabric.impl.ControlifyBootstrap")
        entrypoint("server", "dev.isxander.controlify.fabric.impl.ControlifyBootstrap")
        dependency("fabricloader", REQUIRED, "[0.19,)")

        mixin("controlify-platform.fabric.mixins.json")

        customData.putAll(rrlsData)
    }

    neoForgeModsToml(sourceSets.neoforge.get()) {
        from(commonManifest)

        mixin("controlify-platform.neoforge.mixins.json")

        modProperties.putAll(rrlsData)
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        into("META-INF")
    }
}

// the neoforge main compile check reveals javac inconsistencies with
// incremental compilation and anonymous class constructor parameter LVT
tasks.withType<JavaCompile>().configureEach {
	options.compilerArgs.add("-parameters")
}

/// Natives in the jar

//val includeNatives = sc.current.parsed < "26.3"
val includeNatives = true

stonecutter.constants.put("natives_in_jar", includeNatives)

if (includeNatives) {
    data class NativeTarget(
        val classifier: String,
        val ext: String,
        val jarDir: String,
        val fileName: String,
        val configurationName: String,
    )

    val nativeTargets = listOf(
        NativeTarget(classifier = "linux-aarch64", ext = "so", jarDir = "linux-aarch64/", fileName = "libSDL3", configurationName = "offlineNativeLinuxAarch64"),
        NativeTarget(classifier = "linux-x86_64", ext = "so", jarDir = "linux-x86-64/", fileName = "libSDL3", configurationName = "offlineNativeLinuxX86_64"),
        NativeTarget(classifier = "macos-aarch64", ext = "dylib", jarDir = "darwin-aarch64/", fileName = "libSDL3", configurationName = "offlineNativeMacAarch64"),
        NativeTarget(classifier = "macos-x86_64", ext = "dylib", jarDir = "darwin-x86-64/", fileName = "libSDL3", configurationName = "offlineNativeMacX86_64"),
        NativeTarget(classifier = "windows-x86_64", ext = "dll", jarDir = "win32-x86-64/", fileName = "SDL3", configurationName = "offlineNativeWinX86_64"),
        NativeTarget(classifier = "windows-aarch64", ext = "dll", jarDir = "win32-aarch64/", fileName = "SDL3", configurationName = "offlineNativeWinAarch64"),
    )

    val nativeConfigurations = nativeTargets.associate { target ->
        target.configurationName to configurations.create(target.configurationName)
    }

    nativeTargets.forEach { target ->
        dependencies {
            val sdlVersion = libs.versions.sdl.natives.get()
            val configuration = nativeConfigurations[target.configurationName]!!
            configuration("dev.isxander.sdl:sdl-natives:${sdlVersion}:${target.classifier}@${target.ext}")
            configuration("dev.isxander.sdl:sdl-natives:${sdlVersion}:${target.classifier}@${target.ext}.md5")
        }
    }

    val prepareNatives = tasks.register<Sync>("prepareNativeResources") {
        group = "controlify/internal"

        into(layout.buildDirectory.dir("generated-resources/sdl-natives"))

        nativeTargets.forEach { target ->
            from(configurations.named(target.configurationName)) {
                into(target.jarDir)
                rename { fileName ->
                    "${target.fileName}.${target.ext}${if (fileName.endsWith(".md5")) ".md5" else ""}"
                }
            }
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
}

/// Testing

fabricApi {
	@Suppress("UnstableApiUsage")
	configureTests {
		createSourceSet = true
		modId = "controlify_test"
		enableGameTests = false
		enableClientGameTests = true
		eula = true
	}
}

sourceSets.named { it in listOf("gametest", "test") }.configureEach {
	compileClasspath += sourceSets.fabric.get().output
	runtimeClasspath += sourceSets.fabric.get().output
}
configurations.named("testCompileClasspath") {
	extendsFrom(configurations.named("fabricCompileClasspath"))
}
configurations.named("gametestCompileClasspath") {
	extendsFrom(configurations.named("testCompileClasspath"))
}
configurations.named("testRuntimeClasspath") {
	extendsFrom(configurations.named("fabricRuntimeClasspath"))
}
configurations.named("gametestRuntimeClasspath") {
	extendsFrom(configurations.named("testRuntimeClasspath"))
}

val gametestImplementation = configurations.named("gametestImplementation")

dependencies {
	testImplementation(libs.fabric.loader.junit)

	gametestImplementation(fabricApiBom)
	gametestImplementation("net.fabricmc.fabric-api:fabric-gametest-api-v1")
	gametestImplementation("net.fabricmc.fabric-api:fabric-client-gametest-api-v1")
}

/// Example addon
///
/// A Fabric mod that uses the public API the way the docs describe it.
/// Docs pages import its snippets by `// #region` markers, so the snippets always compile.

val example = sourceSets.create("example") {
	compileClasspath += sourceSets.fabric.get().output
	runtimeClasspath += sourceSets.fabric.get().output
}
configurations.named(example.compileClasspathConfigurationName) {
	extendsFrom(configurations.named("fabricCompileClasspath").get())
}
configurations.named(example.runtimeClasspathConfigurationName) {
	extendsFrom(configurations.named("fabricRuntimeClasspath").get())
}

tasks.test {
	useJUnitPlatform()

	val testRunDir = layout.buildDirectory.dir("junit-run")
	workingDir = testRunDir.get().asFile
	doFirst { testRunDir.get().asFile.mkdirs() }

	// ResourceFormatsTest decodes every shipped resource file, including the example addon's
	val resourceRoots = listOf("src/main/resources", "src/example/resources").map { rootProject.file(it) }
	inputs.files(resourceRoots).withPropertyName("resourceRoots")
	systemProperty("controlify.test.resourceRoots", resourceRoots.joinToString(File.pathSeparator) { it.absolutePath })
}

/// Run configs

// Fabric
loom.runConfigs {
	configureEach {
		runDirectory = layout.projectDirectory.dir("run")
	}

	register("exampleClient") {
		client()
		sourceSet = example.name
	}
}

// NeoForge
runs {
	configureEach {
		workingDirectory = layout.projectDirectory.dir("run")
	}

	ifPresent("dep.neoforge") {
		register("neoforgeClient") {
			runType("client")
		}
	}
}

/// Publishing

publishMods {
    from(rootProject.publishMods)
    dryRun = rootProject.publishMods.dryRun

	version = "$modVersion+mc$minecraftVersion"
	displayName = version.map { "Controlify $it" }

    file = tasks.universalJar.flatMap { it.archiveFile }
    modLoaders.addAll("fabric", "neoforge")

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth.id")
        environment = CLIENT_ONLY_SERVER_OPTIONAL
        announcementTitle = "Modrinth ($minecraftVersion)"
        minecraftVersions.addAll(supportedMinecraftVersions)

        requires("fabric-api")
        requires("yacl")
        optional("modmenu")
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = providers.gradleProperty("curseforge.id")
        projectSlug = providers.gradleProperty("curseforge.slug")
        client = true
        server = true
        announcementTitle = "Curseforge ($minecraftVersion)"
        minecraftVersions.addAll(supportedMinecraftVersions)

        requires("fabric-api")
        requires("yacl")
        optional("modmenu")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
			artifactId = "controlify"

            from(components["java"])
            artifact(tasks.universalJar)
            artifact(tasks.universalSourcesJar)
        }
    }
}

/// Utilities

fun <T> ifPresent(property: String, block: (String) -> T): T? {
    return if (hasProperty(property)) {
        block(property(property).toString())
    } else {
        null
    }
}
