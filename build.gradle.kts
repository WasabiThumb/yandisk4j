plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

group = "io.github.wasabithumb"
version = "0.2.1"
description = "Wrapper for the Yandex Disk cloud storage API "

repositories {
    mavenCentral()
}

val targetJavaVersion = 17
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }

    withSourcesJar()
    withJavadocJar()

    registerFeature("nanohttpd") {
        usingSourceSet(sourceSets.main.get())
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    compileOnly("org.jetbrains:annotations:26.0.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    "nanohttpdImplementation"("org.nanohttpd:nanohttpd:2.3.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    (options as CoreJavadocOptions)
        .addBooleanOption("Xdoclint:none", true)
}

centralPortal {
    name = rootProject.name
    jarTask = tasks.jar
    sourcesJarTask = tasks.sourcesJar
    javadocJarTask = tasks.javadocJar
    pom {
        name = "YanDisk4J"
        description = project.description
        url = "https://github.com/WasabiThumb/yandisk4j"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "wasabithumb"
                email = "wasabithumbs@gmail.com"
                organization = "Wasabi Codes"
                organizationUrl = "https://wasabithumb.github.io/"
                timezone = "-5"
            }
        }
        scm {
            connection = "scm:git:git://github.com/WasabiThumb/yandisk4j.git"
            url = "https://github.com/WasabiThumb/yandisk4j"
        }
    }
}
