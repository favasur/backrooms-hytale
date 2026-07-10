/*
 * Wrong Yellow Mod — Flicker Plugin
 * 
 * Part of the Wrong Yellow mod pack. Adds flickering light effects
 * to the White Build Lightsource block by swapping between full,
 * dimmed, and off block variants at random intervals.
 * 
 * Build with: ./gradlew build
 * Deploy with: ./gradlew deployPlugin
 * Requires: Java 25, Hytale server >=0.5.3
 */

plugins {
    java
    id("com.azuredoom.hytale-tools") version "1.+"
}

tasks.withType<Javadoc>().configureEach {
    (options as org.gradle.external.javadoc.StandardJavadocDocletOptions).addStringOption("Xdoclint:-missing", "-quiet")
}

group = project.property("group").toString()

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(property("java_version").toString().toInt()))
}

hytaleTools {
    javaVersion = property("java_version").toString().toInt()
    hytaleVersion = property("hytale_version").toString()
    manifestServerVersion = property("manifestServerVersion").toString()
    manifestGroup = property("manifest_group").toString()
    modId = property("mod_id").toString()
    modDescription = property("mod_description").toString()
    modUrl = property("mod_url").toString()
    mainClass = property("main_class").toString()
    modCredits = property("mod_author").toString()
    manifestDependencies = property("manifest_dependencies").toString()
    manifestOptionalDependencies = property("manifest_opt_dependencies").toString()
    curseforgeId = property("curseforgeID").toString()
    disabledByDefault = property("disabled_by_default").toString().toBoolean()
    includesPack = property("includes_pack").toString().toBoolean()
    patchline = property("patchline").toString()
    injectServerJavadocsIntoSources = property("injectServerJavadocsIntoSources").toString().toBoolean()
    generateAssetsBinary = property("generateAssetsBinary").toString().toBoolean()
}

repositories {
    mavenCentral()
    // Local Maven repo for Hytale SDK (resolved by hytale-tools plugin)
    maven {
        name = "GradleCacheHytale"
        url = uri("${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1")
    }
}

dependencies {
    // Hytale Server SDK — provides JavaPlugin, TickingSystem, Component, BlockModule, etc.
    compileOnly(files(
        "C:/Users/User X/AppData/Roaming/Hytale/install/release/package/game/latest/Server/HytaleServer.jar"
    ))
    
    // Mojang DataFixerUpper — provides Codec, RecordCodecBuilder for serialization
    compileOnly(files(
        "${System.getProperty("user.home")}/.gradle/caches/minecraft/libraries/com/mojang/datafixerupper/8.0.16/datafixerupper-8.0.16.jar"
    ))
    
    // SLF4J API — provides Logger, LoggerFactory
    compileOnly(files(
        "${System.getProperty("user.home")}/.gradle/caches/minecraft/libraries/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar"
    ))
}

tasks.named<Jar>("jar") {
    archiveBaseName.set(project.property("mod_name").toString())
    archiveVersion.set(project.property("version").toString())

    manifest {
        attributes(
            "Main-Class" to project.property("main_class").toString()
        )
    }
}
