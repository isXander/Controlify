import dev.isxander.controlify.branchProj

plugins {
    id("dev.isxander.controlify.project")
}

modstitch {
    metadata {
        modId = "controlify-splitscreen"
        modName = "Controlify (Splitscreen)"
    }

    mixin {
        addMixinsToModManifest = true
        configs.register("controlify-splitscreen")
    }
}

dependencies {
    if (modstitch.isLoom) {
        api(project(path = branchProj.path, configuration = "namedElements"))
    } else if (modstitch.isModDevGradleRegular) {
        api(branchProj)
    }
}
