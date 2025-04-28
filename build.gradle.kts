plugins {
    `java-library`
    `maven-publish`
    signing
    idea
    id("fabric-loom") version "1.10-SNAPSHOT"
}

group = property("maven_group") as String
version = property("maven_version") as String
description = property("maven_description") as String

base {
    archivesName.set(property("maven_name") as String)
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.parchmentmc.org")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // ModMenu
    val fabricApiVersion = property("fabric_api_version") as String
    modImplementation(fabricApi.module("fabric-api-base", fabricApiVersion))
    modImplementation(fabricApi.module("fabric-resource-loader-v0", fabricApiVersion))
    modImplementation(fabricApi.module("fabric-screen-api-v1", fabricApiVersion))
    modImplementation(fabricApi.module("fabric-key-binding-api-v1", fabricApiVersion))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersion))
    modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    jar {
        val projectName = project.name

        // Rename the project's license file to LICENSE_<project_name> to avoid conflicts
        from("LICENSE") {
            rename { "LICENSE_${projectName}" }
        }
    }

    processResources {
        val projectVersion = project.version
        val projectDescription = project.description
        val mcVersion = mcVersion()
        filesMatching("fabric.mod.json") {
            expand(
                "version" to projectVersion,
                "description" to projectDescription,
                "mcVersion" to mcVersion,
            )
        }
    }

    withType<PublishToMavenRepository>().configureEach {
        dependsOn(withType<Sign>())
    }
}

fun mcVersion(): String {
    val supportedVersions = property("supported_versions") as String
    return supportedVersions.ifEmpty {
        property("minecraft_version") as String
    }
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

publishing {
    repositories {
        maven {
            name = "reposilite"
            url = uri("https://maven.lenni0451.net/" + if (version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            name = "ossrh"
            val releasesUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl)
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set(artifactId)
                description.set(project.description)
                url.set("https://github.com/FlorianMichael/MyConnectionMyChoice")
                licenses {
                    license {
                        name.set("GPL-3.0 License")
                        url.set("https://github.com/FlorianMichael/MyConnectionMyChoice/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("FlorianMichael")
                        name.set("EnZaXD")
                        email.set("florian.michael07@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/FlorianMichael/MyConnectionMyChoice.git")
                    developerConnection.set("scm:git:ssh://github.com/FlorianMichael/MyConnectionMyChoice.git")
                    url.set("https://github.com/FlorianMichael/MyConnectionMyChoice")
                }
            }
        }
    }
}

signing {
    isRequired = false
    sign(publishing.publications["maven"])
}
