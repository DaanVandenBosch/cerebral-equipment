import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    kotlin("multiplatform") version "1.7.20"
    `maven-publish`
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
}

group = "equipment.cerebral"
version = "1.0-SNAPSHOT"

kotlin {
    js(BOTH) {
        browser()
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.contracts.ExperimentalContracts")
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

detekt {
    config = files(rootDir.resolve("detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = true
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
}

tasks.register("detektAll") {
    group = "verification"
    dependsOn(tasks.withType<Detekt>())
}

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0") {
        exclude("org.slf4j:slf4j-nop")
    }
}
