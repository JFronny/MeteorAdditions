plugins {
    alias(libs.plugins.fabric.loom)
}

base {
    version = libs.versions.mod.version.get()
    group = "io.gitlab.jfronny"
}

repositories {
    maven("https://maven.meteordev.org/releases")
    maven("https://maven.meteordev.org/snapshots")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.frohnmeyer-wds.de/artifacts")
}

val modInclude: Configuration by configurations.creating
configurations {
    implementation.configure { extendsFrom(modInclude) }
    include.configure { extendsFrom(modInclude) }
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)

    implementation(libs.fabric.api)

    implementation(libs.meteor.client)
    implementation(libs.modmenu)

    modInclude(libs.google.chat)
    modInclude(libs.libjf.config.core.v2)
    modInclude(libs.libjf.translate.v1)
    include(libs.libjf.base)
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to version,
                "mc_version" to libs.versions.minecraft.get(),
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
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 25
    }
}
