plugins {
    id("fabric-loom") version "1.0-SNAPSHOT"
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
val game = "1.19.3"

dependencies {
    minecraft("com.mojang:minecraft:$game")
    mappings("net.fabricmc:yarn:$game+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.14.11")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.69.1+1.19.3")

    modImplementation("meteordevelopment:meteor-client:0.5.2-SNAPSHOT")
    modImplementation("com.terraformersmc:modmenu:5.0.2")

    include(modImplementation("io.gitlab.jfronny:google-chat:0.5.0")!!)
    val libjfVersion = "3.3.1"
    include(modImplementation("io.gitlab.jfronny.libjf:libjf-config-core-v1:$libjfVersion")!!)
    include(modImplementation("io.gitlab.jfronny.libjf:libjf-translate-v1:$libjfVersion")!!)
    include("io.gitlab.jfronny.libjf:libjf-base:$libjfVersion")
}

tasks.withType<ProcessResources> {
    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "version" to version,
            "mc_version" to game,
            "gh_hash" to (System.getenv("GITHUB_SHA") ?: "")
        ))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
