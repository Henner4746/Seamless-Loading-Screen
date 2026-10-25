plugins {
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

fun prop(name: String): String = findProperty(name)?.toString() ?: throw IllegalArgumentException("Missing property: $name")

val minecraft = stonecutter.current.version
val modVersion = prop("mod.version")

base { archivesName.set(prop("mod.archives_name")) }
group = prop("mod.group")
version = "$modVersion+mc$minecraft"

repositories {
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.terraformersmc.com/releases")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    implementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fapi")}")
    implementation("dev.isxander:yet-another-config-lib:${prop("deps.yacl")}")
    implementation("com.terraformersmc:modmenu:${prop("deps.modmenu")}")
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.encoding = "UTF-8"
}

tasks.withType<Jar>().configureEach {
    from(rootDir.resolve("LICENSE"))
}

loom {
    // Not runConfigs.all: the game test run config has no environment yet when it gets added
    runConfigs.named("client") {
        ideConfigGenerated(true)
        runDir("../../run")
    }
    runConfigs.named("server") {
        ideConfigGenerated(false)
        runDir("../../run")
    }
}

// Scripted in-game tests: ./gradlew :26.3:runClientGameTest
fabricApi {
    configureTests {
        createSourceSet = true
        modId = "seamless_loading_screen_test"
        enableGameTests = false
        enableClientGameTests = true
        eula = true
    }
}

tasks.processResources {
    val properties = mapOf(
        "version" to modVersion,
        "minecraft" to prop("mod.mc_range"),
    )
    inputs.properties(properties)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

publishMods {
    val modrinthToken = findProperty("modrinthToken")
    dryRun = modrinthToken == null

    file = tasks.jar.flatMap { it.archiveFile }
    additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })

    displayName = "${prop("mod.name")} $modVersion for Fabric $minecraft"
    version = "$modVersion+mc$minecraft-fabric"
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    modrinth {
        projectId = prop("publish.modrinth")
        accessToken = modrinthToken.toString()
        minecraftVersions.add(minecraft)
        requires("fabric-api")
        requires("yacl")
        optional("modmenu")
    }
}
