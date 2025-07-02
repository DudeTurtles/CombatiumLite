plugins {
    id ("com.gradleup.shadow") version "9.0.0-beta15"
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

version = "${project.property("version")}.${project.property("sub_version")}"
group = project.property("maven_group")!!

base {
    archivesName = "${project.property("archives_base_name")}-${project.property("minecraft_version")!!}"
}

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

    implementation("com.github.retrooper:packetevents-spigot:2.9.0-SNAPSHOT")
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
}

tasks {
    runServer {
        minecraftVersion(project.property("minecraft_version")!!.toString())
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}