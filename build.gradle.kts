plugins {
    id("java")
}

group = "net.morphserver"
version = "2.0.0"
description = "Show item worth in item lore"

java {
    // Use Java 21 for Minecraft 1.20.5+ / 1.21+
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    // Default repository
    mavenCentral()

    // PaperMC API repository
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Paper API for Minecraft 1.21.4
    // Works on Spigot/Purpur servers as well (compileOnly)
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks.test {
    // Disable tests entirely
    useJUnitPlatform()
}
