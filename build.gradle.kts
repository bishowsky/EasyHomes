plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    
    // Database
    implementation("com.zaxxer:HikariCP:5.0.1")
    
    // Cache
    implementation("com.google.guava:guava:32.1.3-jre")
    
    // Hooks
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("easyhomes")
        archiveVersion.set("")
        archiveClassifier.set("")
        
        // Relocate dependencies to avoid conflicts
        relocate("com.zaxxer.hikari", "com.easyhomes.libs.hikari")
        relocate("com.google.common", "com.easyhomes.libs.guava")
        
        minimize()
    }
    
    build {
        dependsOn(shadowJar)
    }
}

tasks.jar {
    archiveBaseName.set("easyhomes")
    archiveVersion.set("")
}