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

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    fabricLoader(libs.fabric.loader)
    neoforgeImplementation("net.neoforged:neoforge:${property("dep.neoforge")}")

    implementation(platform("net.fabricmc.fabric-api:fabric-api-bom:${property("dep.fapi")}"))
	fabricImplementation(platform("net.fabricmc.fabric-api:fabric-api-bom:${property("dep.fapi")}"))
	fabricImplementation("net.fabricmc.fabric-api:fabric-resource-loader-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-networking-api-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-command-api-v2")
	fabricImplementation("net.fabricmc.fabric-api:fabric-lifecycle-events-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-screen-api-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-rendering-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-creative-tab-api-v1")
	fabricImplementation("net.fabricmc.fabric-api:fabric-key-mapping-api-v1")
	// this needs to be on main because fabric is shared with main jar
	implementation("net.fabricmc.fabric-api:fabric-transitive-access-wideners-v1")
    fabricRuntimeOnly("net.fabricmc.fabric-api:fabric-api")

    commonApi(libs.sdl.java.api)
    commonInclude(libs.sdl.java.api)
    commonApi(libs.sdl.java.backend.ffm)
    commonInclude(libs.sdl.java.backend.ffm)

    commonApi(libs.steamdeck4j)
    commonInclude(libs.steamdeck4j)

    commonApi(libs.quilt.parsers.json)

    compileOnlyApi("dev.isxander:yet-another-config-lib:${property("dep.yacl")}") {
		exclude(group = "net.fabricmc.fabric-api")
	}
    fabricApi("dev.isxander:yet-another-config-lib:${property("dep.yacl")}")
    neoforgeApi("dev.isxander:yet-another-config-lib:${property("dep.yacl-neoforge")}")

	ifPresent("dep.mod-menu") {
		fabricImplementation("com.terraformersmc:modmenu:$it")
	}

    ifPresent("dep.sodium") {
        compileOnly("net.caffeinemc:sodium-fabric:$it") {
			exclude(group = "net.fabricmc.fabric-api")
		}
        fabricImplementation("net.caffeinemc:sodium-fabric:$it")
        neoforgeImplementation("net.caffeinemc:sodium-neoforge:$it")
        neoforgeImplementation("net.caffeinemc:sodium-neoforge-mod:$it")
    }

    ifPresent("dep.iris") {
        compileOnly("maven.modrinth:iris:$it")
        fabricImplementation("maven.modrinth:iris:$it")
    }
    ifPresent("dep.iris-neoforge") {
        neoforgeImplementation("maven.modrinth:iris:$it")
    }

    ifPresent("dep.rso") {
        compileOnly("maven.modrinth:reeses-sodium-options:$it")
        fabricImplementation("maven.modrinth:reeses-sodium-options:$it")
    }
    ifPresent("dep.rso-neoforge") {
        neoforgeImplementation("maven.modrinth:reeses-sodium-options:$it")
    }

    ifPresent("dep.svc") {
        compileOnly("maven.modrinth:simple-voice-chat:$it")
        fabricImplementation("maven.modrinth:simple-voice-chat:$it")
    }
    ifPresent("dep.svc-neoforge") {
        neoforgeImplementation("maven.modrinth:simple-voice-chat:$it")
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
        put("reeses_sodium_options", hasProperty("dep.rso"))
        put("fancy_menu", hasProperty("dep.fancy-menu"))
    }
}

/// Run configurations

runs.register("neoforgeClient") {
	runType("client")
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
    licenses.add("LGPL-3.0")
    issueTrackerUrl = providers.gradleProperty("mod.issuesUrl")
    sourcesUrl = providers.gradleProperty("mod.sourcesUrl")
    homepage = sourcesUrl

    mixin("controlify.mixins.json")
    mixin("controlify-compat.yacl.mixins.json")
    ifPresent("deps.iris") { mixin("controlify-compat.iris.mixins.json") }
    ifPresent("deps.sodium") { mixin("controlify-compat.sodium.mixins.json") }
    ifPresent("deps.reeses-sodium-options") { mixin("controlify-compat.reeses-sodium-options.mixins.json") }
    ifPresent("deps.svc") { mixin("controlify-compat.svc.mixins.json") }
    mixin("controlify-compat.rrls.mixins.json")

    dependency("minecraft", REQUIRED, minecraftRange)
    dependency("yet_another_config_lib_v3", REQUIRED, "*")
}
manifests {
    val rrlsData = mapOf<String, Any>("rrls" to mapOf(
        "force_load" to listOf(
            "controlify:default_config",
            "controlify:controller_type",
            "controlify:default_binds",
        )
    ))

    fabricModJson(sourceSets.fabric.get()) {
        from(commonManifest)

        entrypoint("modmenu", "dev.isxander.controlify.fabric.compatibility.ModMenuIntegration")
        entrypoint("main", "dev.isxander.controlify.fabric.ControlifyBootstrap")
        entrypoint("client", "dev.isxander.controlify.fabric.ControlifyBootstrap")
        entrypoint("server", "dev.isxander.controlify.fabric.ControlifyBootstrap")
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

val includeNatives = sc.current.parsed < "26.3"

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

/// Publishing

publishMods {
    from(rootProject.publishMods)

    file = tasks.universalJar.flatMap { it.archiveFile }
    version = "$modVersion+mc$minecraftVersion"
    displayName = commonManifest.displayName
    modLoaders.addAll("fabric", "neoforge")

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("modrinth.id")
        environment = CLIENT_ONLY_SERVER_OPTIONAL
        announcementTitle = "Modrinth ($minecraftVersion)"
        minecraftVersions.addAll(minecraftVersions)

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
        minecraftVersions.addAll(minecraftVersions)

        requires("fabric-api")
        requires("yacl")
        optional("modmenu")
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
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
