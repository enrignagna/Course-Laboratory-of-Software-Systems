plugins {
    `java-gradle-plugin`
    jacoco
    kotlin("jvm") version "1.3.72"
    id("org.danilopianini.git-sensitive-semantic-versioning") version "0.2.2"
    id("com.gradle.plugin-publish") version "0.12.0"
    id("pl.droidsonroids.jacoco.testkit") version "1.0.7"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.14.1"
}

group = "it.unibo.lss2020"

gitSemVer {
    version = computeGitSemVer()
}

repositories {
    jcenter()
}

dependencies {
    implementation(gradleApi()) // Implementation: available at compile and runtime, non transitive
    testImplementation(gradleTestKit()) // Test implementation: available for testing compile and runtime
    testImplementation("io.kotest:kotest-runner-junit5:4.1.3") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:4.1.3") // for kotest core assertions
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.1.3") // for kotest core jvm assertions
}

tasks.withType<Test> {
    useJUnitPlatform() // Use JUnit 5 engine
    testLogging.showStandardStreams = true
    testLogging {
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// This task creates a file with a classpath descriptor, to be used in tests
val createClasspathManifest by tasks.registering {
    val outputDir = file("$buildDir/$name")
    inputs.files(sourceSets.main.get().runtimeClasspath)
    outputs.dir(outputDir)
    doLast {
        outputDir.mkdirs()
        file("$outputDir/plugin-classpath.txt").writeText(sourceSets.main.get().runtimeClasspath.joinToString("\n"))
    }
}

// Add the classpath file to the test runtime classpath
dependencies {
    // This way "createClasspathManifest" is always executed before the tests!
    // Gradle auto-resolves dependencies if there are dependencies on inputs/outputs
    testRuntimeOnly(files(createClasspathManifest))
}

pluginBundle { // These settings are set for the whole plugin bundle
    website = "https://danysk.github.io/Course-Laboratory-of-Software-Systems/"
    vcsUrl = "https://github.com/DanySK/Course-Laboratory-of-Software-Systems"
    tags = listOf("example", "greetings", "lss", "unibo")
}
gradlePlugin {
    plugins {
        create("GradleLatex") { // One entry per plugin
            id = "${project.group}.${project.name}"
            displayName = "LSS Greeting plugin"
            description = "Example plugin for the LSS course"
            implementationClass = "it.unibo.lss.firstplugin.GreetingPlugin"
        }
    }
}

tasks.jacocoTestReport {
    reports {
        // xml.isEnabled = true
        html.isEnabled = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}
