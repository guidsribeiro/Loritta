plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

group = "net.perfectdreams.showtime"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":common"))
    implementation(project(":web:showtime:web-common"))
    implementation(project(":discord:loritta-bot-common"))

    // Logging Stuff
    implementation(libs.logback.classic)

    // Logback GELF, used for Graylog logging
    implementation("de.siegmar:logback-gelf:3.0.0")

    // Ktor
    implementation("io.ktor:ktor-server-netty:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-html-builder:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-caching-headers:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-compression:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-status-pages:${Versions.KTOR}")

    // KotlinX HTML
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")

    implementation("org.jsoup:jsoup:1.14.3")

    // YAML
    implementation("org.yaml:snakeyaml:1.30")
    implementation("com.charleskorn.kaml:kaml:0.36.0")

    // Sequins
    api("net.perfectdreams.sequins.ktor:base-route:1.0.4")

    implementation("net.perfectdreams.etherealgambi:client:1.0.0")

    api("commons-codec:commons-codec:1.15")

    api("com.vladsch.flexmark:flexmark-all:0.64.0")
}

jib {
    extraDirectories {
        paths {
            path {
                setFrom("../../../content")
                into = "/content"
            }
        }
    }

    container {
        ports = listOf("8080")
    }

    to {
        image = "ghcr.io/lorittabot/showtime-backend"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        image = "eclipse-temurin:17-focal"
    }
}

val jsBrowserProductionWebpack = tasks.getByPath(":web:showtime:showtime-frontend:jsBrowserProductionWebpack") as org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

// Optimize image assets
val optimizeImageAssets = tasks.register<ImageOptimizerTask>("optimizeImageAssets") {
    sourceImagesDirectory.set(file("src/main/images"))
    outputImagesDirectory.set(file("$buildDir/images"))
    outputImagesInfoFile.set(file("$buildDir/generated-resources/images-info.json"))

    // Unused for now
    imagesOptimizationSettings.set(listOf())
}

// Annotates images width, height and file size on a JSON file, used for the commands view to avoid content shifting when loading images
// TODO: I wanted to annotate the images in the "$buildDir/build/resources/main", but I wasn't able to do it... Maybe there's a way?
val annotateOptimizedImageAttributes = tasks.register<AnnotateImageAttributesTask>("annotateOptimizedImageAttributes") {
    sourceImagesDirectory.set(file("src/main/images"))
    outputImagesInfoFile.set(file("$buildDir/generated-resources/optimized-images-attributes.json"))

    dependsOn(optimizeImageAssets)
}

val annotateBundledImageAttributes = tasks.register<AnnotateImageAttributesTask>("annotateBundledImageAttributes") {
    sourceImagesDirectory.set(file("src/main/resources"))
    outputImagesInfoFile.set(file("$buildDir/generated-resources/bundled-images-attributes.json"))
}

val sass = tasks.register<SassTask>("sass-style-scss") {
    this.inputSass.set(file("src/main/sass/style.scss"))
    this.inputSassFolder.set(file("src/main/sass/"))
    this.outputSass.set(file("$buildDir/sass/style-scss"))
}

tasks {
    processResources {
        from("../../../resources/") // Include folders from the resources root folder
        from("../../resources/") // Include folders from the resources web folder

        // We need to wait until the JS build finishes and the SASS files are generated
        dependsOn(jsBrowserProductionWebpack)
        dependsOn(sass)
        dependsOn(optimizeImageAssets)
        dependsOn(annotateOptimizedImageAttributes)
        dependsOn(annotateBundledImageAttributes)

        // Copy the output from the frontend task to the backend resources
        from(jsBrowserProductionWebpack.destinationDirectory) {
            into("static/v3/assets/js/")
        }

        // Same thing with the SASS output
        from(sass) {
            into("static/v3/assets/css/")
        }

        // Same thing with the images
        from(File(buildDir, "images")) {
            into("")
        }

        // Same thing with the generated-resources output
        from(File(buildDir, "generated-resources")) {
            into("")
        }
    }
}