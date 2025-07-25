plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
}

version = "1.2.0"
group = "io.gitlab.jfronny"

repositories {
    maven("https://maven.meteordev.org/releases")
    maven("https://maven.meteordev.org/snapshots")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.frohnmeyer-wds.de/artifacts")
}

// https://fabricmc.net/develop
val game = "1.21.7" // Note: remember to update ServerFinderScreen with the new game version

dependencies {
    minecraft("com.mojang:minecraft:$game")
    mappings("net.fabricmc:yarn:$game+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.14")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.128.1+$game")

    modImplementation("meteordevelopment:meteor-client:$game-SNAPSHOT")
    modImplementation("com.terraformersmc:modmenu:15.0.0-beta.3")

    include(modImplementation("io.gitlab.jfronny:google-chat:0.9.4")!!)
    val libjfVersion = "3.19.0"
    include(modImplementation("io.gitlab.jfronny.libjf:libjf-config-core-v2:$libjfVersion")!!)
    include(modImplementation("io.gitlab.jfronny.libjf:libjf-translate-v1:$libjfVersion")!!)
    include("io.gitlab.jfronny.libjf:libjf-base:$libjfVersion")
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to version,
                "mc_version" to game,
                "gh_hash" to (System.getenv("GITHUB_SHA") ?: "")
            ))
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName}" }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }
}
