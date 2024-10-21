plugins {
    `java-library`

    id("dev.architectury.loom")
}

val parentNode = stonecutter.node.sibling("")!!
val parentProject = parentNode.project

// FOR THE LOVE OF GOD DO NOT SET THE GROUP, IT BREAKS EVERYTHING
version = parentProject.version
base.archivesName.set("sodium-0.6-compat")

// loader stuff
val loader = loom.platform.get().name.lowercase()
val isFabric = loader == "fabric"
val isNeoforge = loader == "neoforge"
val isForge = loader == "forge"
val isForgeLike = isNeoforge || isForge

dependencies {
    minecraft("com.mojang:minecraft:${parentNode.property("mcVersion")!!}")
    mappings(loom.officialMojangMappings())

    if (isNeoforge) {
        "neoForge"("net.neoforged:neoforge:${parentNode.property("deps.neoforge")}")
    }

    implementation(project(parentProject.path, "namedElements"))

    modImplementation("maven.modrinth:sodium:${property("deps.sodium")}")
}
