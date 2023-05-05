import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import org.gradle.jvm.tasks.Jar

plugins {
    java

    alias(libs.plugins.loom)
    alias(libs.plugins.loom.quiltflower)

    alias(libs.plugins.minotaur)
    alias(libs.plugins.cursegradle)
    alias(libs.plugins.github.release)
    alias(libs.plugins.machete)
    alias(libs.plugins.grgit)
    alias(libs.plugins.blossom)
    `maven-publish`
}

group = "dev.isxander"
version = "1.1.1+1.19.4"

repositories {
    mavenLocal()
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
    mavenLocal()
    maven("https://maven.flashyreese.me/snapshots")
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
    ).forEach {
        modImplementation(fabricApi.module(it, libs.versions.fabric.api.get()))
    }

    listOf(
        // sodium requirements
        "fabric-rendering-data-attachment-v1",
        "fabric-rendering-fluids-v1",
    ).forEach {
        modRuntimeOnly(fabricApi.module(it, libs.versions.fabric.api.get()))
    }

    modImplementation(libs.yet.another.config.lib)
    modImplementation(libs.mod.menu)

    api(libs.mixin.extras)
    annotationProcessor(libs.mixin.extras)
    include(libs.mixin.extras)

    // used to identify controller connections
    implementation(libs.hid4java)
    include(libs.hid4java)

    // controller rumble
    implementation(libs.sdl2.jni)
    include(libs.sdl2.jni)

    // used to parse hiddb.json5
    implementation(libs.quilt.json5)
    include(libs.quilt.json5)

    // sodium compat
    modImplementation(libs.sodium)
    // iris compat
    modImplementation(libs.iris)
    modRuntimeOnly("org.anarres:jcpp:1.4.14")
    modRuntimeOnly("io.github.douira:glsl-transformer:2.0.0-pre9")
    // immediately-fast compat
    modImplementation(libs.immediately.fast)
    modRuntimeOnly("net.lenni0451:Reflect:1.1.0")

    // testmod
    "testmodImplementation"(sourceSets.main.get().output)
}

blossom {
    val sdl2ManagerClass = "src/main/java/dev/isxander/controlify/controller/sdl2/SDL2NativesManager.java"
    replaceToken("<SDL2_VERSION>", libs.versions.sdl2.jni.get(), sdl2ManagerClass)
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
    
    remapJar {
        archiveClassifier.set("fabric-$minecraftVersion")   
    }
    
    remapSourcesJar {
        archiveClassifier.set("fabric-$minecraftVersion-sources")   
    }

    named<Jar>("javadocJar") {
        archiveClassifier.set("fabric-$minecraftVersion-javadoc")
    }

    register("releaseMod") {
        group = "mod"

        dependsOn("modrinth")
        dependsOn("modrinthSyncBody")
        dependsOn("curseforge")
        dependsOn("publish")
        dependsOn("githubRelease")
    }

    named("modrinth") {
        dependsOn("optimizeOutputsOfRemapJar")
    }
}

val changelogText = file("changelogs/${project.version}.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."

val modrinthId: String by project
if (modrinthId.isNotEmpty()) {
    modrinth {
        token.set(findProperty("modrinth.token")?.toString())
        projectId.set(modrinthId)
        versionNumber.set("${project.version}")
        versionType.set("release")
        uploadFile.set(tasks["remapJar"])
        gameVersions.set(listOf("1.19.4"))
        loaders.set(listOf("fabric", "quilt"))
        changelog.set(changelogText)
        syncBodyFrom.set(file(".github/README.md").readText())
    }
}

val curseforgeId: String by project
if (hasProperty("curseforge.token") && curseforgeId.isNotEmpty()) {
    curseforge {
        apiKey = findProperty("curseforge.token")
        project(closureOf<me.hypherionmc.cursegradle.CurseProject> {
            mainArtifact(tasks["remapJar"], closureOf<me.hypherionmc.cursegradle.CurseArtifact> {
                displayName = "${project.version}"
            })

            id = curseforgeId
            releaseType = "release"
            addGameVersion("1.19.4")
            addGameVersion("Fabric")
            addGameVersion("Java 17")

            changelog = changelogText
            changelogType = "markdown"
        })

        options(closureOf<me.hypherionmc.cursegradle.Options> {
            forgeGradleIntegration = false
        })
    }
}

githubRelease {
    token(findProperty("github.token")?.toString())

    val githubProject: String by project
    val split = githubProject.split("/")
    owner(split[0])
    repo(split[1])
    tagName("${project.version}")
    targetCommitish("1.19.x/dev")
    body(changelogText)
    releaseAssets(tasks["remapJar"].outputs.files)
}

tasks.getByName<GithubReleaseTask>("githubRelease") {
    dependsOn("optimizeOutputsOfRemapJar")
}

publishing {
    publications {
        create<MavenPublication>("mod") {
            groupId = "dev.isxander"
            artifactId = "controlify"

            artifact(tasks["remapJar"])
            artifact(tasks["remapSourcesJar"])
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
