plugins {
    `java-library`
    id("dev.isxander.mtk.modrepos")
    `maven-publish`
}

group = "dev.isxander"

java {
    withSourcesJar()
}

repositories {
    mavenCentral()
    isxander()
    terraformersMC()
	nucleoid()
    exclusiveContent {
        forRepository { maven("https://maven.quiltmc.org/repository/release") }
        filter { includeGroupAndSubgroups("org.quiltmc") }
    }
    modrinthApi.exclusive()
    caffeineMC()
}

publishing {
    repositories {
        maven("https://maven.isxander.dev/releases") {
            name = "XanderMaven"
            credentials(PasswordCredentials::class)
        }
    }
}


