plugins {
    id("fabric-loom") version "1.1-SNAPSHOT"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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
val game = "1.19.4"

dependencies {
    minecraft("com.mojang:minecraft:$game")
    mappings("net.fabricmc:yarn:$game+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.14.17")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.75.3+1.19.4")

    modImplementation("meteordevelopment:meteor-client:0.5.3-SNAPSHOT")
    modImplementation("com.terraformersmc:modmenu:6.1.0-rc.4")

    include(modImplementation("io.gitlab.jfronny:google-chat:0.6.1")!!)
    val libjfVersion = "3.7.0"
    include(modImplementation("io.gitlab.jfronny.libjf:libjf-config-core-v1:$libjfVersion")!!)
    include(modImplementation("io.gitlab.jfronny.libjf:libjf-translate-v1:$libjfVersion")!!)
    include("io.gitlab.jfronny.libjf:libjf-base:$libjfVersion")
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "version" to version,
            "mc_version" to game,
            "gh_hash" to (System.getenv("GITHUB_SHA") ?: "")
        ))
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}
