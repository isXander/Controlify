plugins {
    `java-library`

    id("dev.architectury.loom")
}

val parentNode = stonecutter.node.sibling("")!!
val parentProject = parentNode.project

// FOR THE LOVE OF GOD DO NOT SET THE GROUP, IT BREAKS EVERYTHING
version = parentProject.version
base.archivesName.set("sodium-0.5-compat")

dependencies {
    minecraft("com.mojang:minecraft:${parentNode.property("mcVersion")!!}")
    mappings(loom.officialMojangMappings())

    implementation(project(parentProject.path, "namedElements"))

    modImplementation("maven.modrinth:sodium:${property("deps.sodium")}")
}
