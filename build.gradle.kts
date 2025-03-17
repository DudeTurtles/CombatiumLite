plugins {
    id ("com.gradleup.shadow") version "9.0.0-beta4"
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.8"
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

    implementation("com.github.retrooper:packetevents-spigot:2.7.0")
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}
