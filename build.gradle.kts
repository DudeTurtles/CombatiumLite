import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id ("com.gradleup.shadow") version "8.3.5"
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "CombatiumLite"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

dependencies {
    shadow(localGroovy())
    shadow(gradleApi())

    implementation("com.github.retrooper:packetevents-spigot:2.6.0")
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
}

tasks {
    runServer {
        minecraftVersion("1.21.3")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}
