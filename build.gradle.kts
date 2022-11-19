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
    maven("https://gitlab.com/api/v4/projects/32473285/packages/maven") // google-chat
    maven("https://gitlab.com/api/v4/projects/25805200/packages/maven") // libjf
}

// https://fabricmc.net/develop
val game = "1.19.2"

dependencies {
    minecraft("com.mojang:minecraft:$game")
    mappings("net.fabricmc:yarn:$game+build.28:v2")
    modImplementation("net.fabricmc:fabric-loader:0.14.10")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.66.0+1.19.2")

    modImplementation("meteordevelopment:meteor-client:0.5.1-SNAPSHOT")
    modImplementation("com.terraformersmc:modmenu:4.0.6")

    include(modImplementation("io.gitlab.jfronny:google-chat:0.4.2")!!)
    val libjfVersion = "3.2.0"
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
