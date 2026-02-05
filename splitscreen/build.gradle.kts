import dev.isxander.controlify.branchProj
import dev.isxander.controlify.mcVersion

plugins {
    id("dev.isxander.controlify.project")
}

modstitch {
    metadata {
        modVersion = "0.1.0-alpha.3+${stonecutter.current.version}"
        modId = "controlify_splitscreen"
        modName = "Controlify (Splitscreen)"
    }

    mixin {
        addMixinsToModManifest = true
        configs.register("splitscreen")
        configs.register("splitscreen-client")
    }
}

repositories {
    exclusiveContent {
        forRepository {
            maven(url = "https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") { name = "DevAuth" }
        }
        filter {
            includeGroup("me.djtheredstoner")
        }
    }
}

val lwjglVersion = when (project.mcVersion) {
    "1.21.11" -> "3.3.3"
    else -> throw IllegalStateException("Can't get LWJGL version for: $mcVersion")
}

dependencies {
    fun Dependency?.jij() = this?.let { modstitchJiJ(it) }

    if (modstitch.isLoom && !modstitch.isUnobfuscated.get()) {
        api(project(path = branchProj.path, configuration = "namedElements"))
    } else if (modstitch.isModDevGradleRegular) {
        api(branchProj)
    }

    modstitchCompileOnly("org.lwjgl:lwjgl-vulkan:$lwjglVersion")
}
