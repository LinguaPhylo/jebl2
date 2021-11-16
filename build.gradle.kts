import java.text.SimpleDateFormat
import java.util.Calendar
import java.nio.file.*

plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "io.github.linguaphylo"
base.archivesName.set("jebl")
version = "3.1.0"

repositories {
    mavenCentral()
}

dependencies {
    //testImplementation("junit:junit:5.8.1") // TODO
    //testRuntimeOnly("junit:junit:5.8.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
    withSourcesJar()
    withJavadocJar()
}

// overwrite compileJava to use module-path
tasks.compileJava {
    // use the project's version or define one directly
    options.javaModuleVersion.set(provider { project.version as String })

    doFirst {
        println("Java version used is ${JavaVersion.current()}.")
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }

    doLast {
        println("${project.name} compiler args = ${options.compilerArgs}")
    }
}

var calendar: Calendar? = Calendar.getInstance()
var formatter = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")

// create jar
tasks.withType<Jar>() {
    manifest {
        attributes(
            "Implementation-Title" to base.archivesName.get(),
            "Implementation-Vendor" to "Andrew Rambaut, Alexei Drummond and Walter Xie",
            "Implementation-Version" to archiveVersion,
            "Built-By" to "Walter Xie", //System.getProperty("user.name"),
            "Build-Jdk" to JavaVersion.current().majorVersion.toInt(),
            "Built-Date" to formatter.format(calendar?.time)
        )
    }
    // copy LICENSE to META-INF
    metaInf {
        from (rootDir) {
            include("LICENSE")
        }
    }
}

// define the release folder according to version
val releaseRepoUrl = layout.buildDirectory.dir("releases")
val snapshotRepoUrl = layout.buildDirectory.dir("snapshots")
val localUrl = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releaseRepoUrl)
// delete previous release under the local build folder
tasks.withType<AbstractPublishToMaven>().configureEach {
    doFirst {
        val path: java.nio.file.Path = Paths.get(localUrl.path)
        if (Files.exists(path)) {
            println("Delete the existing previous release : ${path.toAbsolutePath()}")
            project.delete(path)
        }
    }
}
// publish to maven central
val pubId = "JEBL3"
publishing {
    publications {
        create<MavenPublication>(pubId) {
            artifactId = base.archivesName.get()
            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(pubId)
                description.set("The Java Evolutionary Biology Library using the Java Platform Module System.")
                url.set("https://github.com/LinguaPhylo/jebl3")
                packaging = "jar"
                properties.set(mapOf(
                    "maven.compiler.source" to java.sourceCompatibility.majorVersion,
                    "maven.compiler.target" to java.targetCompatibility.majorVersion
                ))
                licenses {
                    license {
                        name.set("GNU Lesser General Public License, version 3")
                        url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Andrew Rambaut")
                    }
                    developer {
                        name.set("Alexei Drummond")
                    }
                    developer {
                        name.set("Walter Xie")
                    }
                }
                // https://central.sonatype.org/publish/requirements/
                scm {
                    connection.set("scm:git:https://github.com/LinguaPhylo/jebl3.git")
                    developerConnection.set("scm:git:https://github.com/LinguaPhylo/jebl3.git")
                    url.set("http://github.com/LinguaPhylo/jebl3/")
                }
            }
        }
    }

    repositories {
        maven {
            if (project.hasProperty("ossrh.user")) {
                // -Possrh.user=myuser -Possrh.pswd=mypswd
                val ossrhUser = project.property("ossrh.user")
                val ossrhPswd = project.property("ossrh.pswd")
                println("ossrhUser = $ossrhUser, ossrhPswd = ******") // $ossrhPswd

                // publish to maven
                val releaseOSSRH = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotOSSRH = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotOSSRH else releaseOSSRH)
                credentials {
                    username = "$ossrhUser"
                    password = "$ossrhPswd"
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }

            } else {
                // publish to local
                url = localUrl
            }

            println("Publish $base:$version to : ${url.path}")
        }
    }
}

// -Psigning.secretKeyRingFile=/path/to/mysecr.gpg -Psigning.password=mypswd -Psigning.keyId=last8chars
signing {
    sign(publishing.publications[pubId])
}

tasks.javadoc {
    // if (JavaVersion.current().isJava9Compatible)
    (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
}