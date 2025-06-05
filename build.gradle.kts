plugins {
    id ("com.gradleup.shadow") version "9.0.0-beta15"
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

group = "CombatiumLite"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    shadow(localGroovy())
    shadow(gradleApi())

    implementation("com.github.retrooper:packetevents-spigot:2.8.1-SNAPSHOT")
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
}

tasks {
    runServer {
        minecraftVersion("1.21.5")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}