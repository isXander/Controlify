plugins {
    java

    alias(libs.plugins.loom)

    alias(libs.plugins.mod.publish.plugin)
    alias(libs.plugins.machete)
    alias(libs.plugins.grgit)
    alias(libs.plugins.blossom)
    `maven-publish`
}

group = "dev.isxander"
version = "1.7.0+1.20.2"
val isAlpha = "alpha" in version.toString()
val isBeta = "beta" in version.toString()
if (isAlpha) println("Controlify alpha version detected.")
if (isBeta) println("Controlify beta version detected.")

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

val testmod by sourceSets.registering {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

loom {
    accessWidenerPath.set(file("src/main/resources/controlify.accesswidener"))

    runs {
        register("testmod") {
            client()
            ideConfigGenerated(true)
            name("Test Mod")
            source(testmod.get())
        }

        named("server") { ideConfigGenerated(false) }
    }

    createRemapConfigurations(testmod.get())
}

val minecraftVersion = libs.versions.minecraft.get()

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        mappings("org.quiltmc:quilt-mappings:$minecraftVersion+build.${libs.versions.quilt.mappings.get()}:intermediary-v2")
        officialMojangMappings()
    })
    modImplementation(libs.fabric.loader)

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
        modImplementation(fabricApi.module(it, libs.versions.fabric.api.get()))
    }
    modRuntimeOnly(libs.fabric.api)

    listOf(
        "fabric-rendering-fluids-v1",
    ).forEach {
        modRuntimeOnly(fabricApi.module(it, libs.versions.fabric.api.get()))
    }

    modApi(libs.yet.another.config.lib)
    modImplementation(libs.mod.menu)

    api(libs.mixin.extras)
    annotationProcessor(libs.mixin.extras)
    include(libs.mixin.extras)

    // used to identify controller connections
    implementation(libs.hid4java)
    include(libs.hid4java)

    // lots of controller stuff
    implementation(libs.libsdl4j)
    include(libs.libsdl4j)

    // used to parse hiddb.json5
    implementation(libs.quilt.json5)
    include(libs.quilt.json5)

    // sodium compat
    modImplementation(libs.sodium)
    // iris compat
    modImplementation(libs.iris)
    modRuntimeOnly("org.anarres:jcpp:1.4.14")
    modRuntimeOnly("io.github.douira:glsl-transformer:2.0.0-pre13")
    // immediately-fast compat
    modImplementation(libs.immediately.fast)
    modRuntimeOnly("net.lenni0451:Reflect:1.1.0")

    // simple-voice-chat compat
    modImplementation(libs.simple.voice.chat)

    // testmod
    "testmodImplementation"(sourceSets.main.get().output)
}

blossom {
    val sdl2ManagerClass = "src/main/java/dev/isxander/controlify/driver/SDL2NativesManager.java"
    replaceToken("<SDL2_VERSION>", libs.versions.libsdl4j.get(), sdl2ManagerClass)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    processResources {
        val modId: String by project
        val modName: String by project
        val modDescription: String by project
        val githubProject: String by project

        inputs.property("id", modId)
        inputs.property("group", project.group)
        inputs.property("name", modName)
        inputs.property("description", modDescription)
        inputs.property("version", project.version)
        inputs.property("github", githubProject)

        filesMatching(listOf("fabric.mod.json", "quilt.mod.json")) {
            expand(
                "id" to modId,
                "group" to project.group,
                "name" to modName,
                "description" to modDescription,
                "version" to project.version,
                "github" to githubProject,
            )
        }
    }

    register("releaseMod") {
        group = "mod"

        dependsOn("publishMods")
        dependsOn("publish")
    }
}

publishMods {
    file.set(tasks.remapJar.get().archiveFile)
    changelog.set(
        file("changelogs/${project.version}.md")
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
    val stableMCVersions = listOf("1.20.2")

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

