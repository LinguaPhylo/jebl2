import java.text.SimpleDateFormat
import java.util.Calendar
import java.nio.file.*

plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.linguaphylo"
base.archivesName.set("jebl")
version = "3.1.0-b.1"

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
    //withJavadocJar() // not working
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

// shared attributes
tasks.withType<Jar>() {
    manifest {
        attributes(
            "Implementation-Title" to base.archivesName.get(),
            "Implementation-Vendor" to "Andrew Rambaut and Alexei Drummond",
            "Implementation-Version" to archiveVersion,
            "Built-By" to "Walter Xie", //System.getProperty("user.name"),
            "Build-Jdk" to JavaVersion.current().majorVersion.toInt(),
            "Built-Date" to formatter.format(calendar?.time)
        )
    }
}

// define and create the release folder under root
val releaseDir = "releases"
tasks.withType<AbstractPublishToMaven>().configureEach {
    doFirst {
        val path: java.nio.file.Path = Paths.get("${rootDir}", releaseDir)
        if (Files.exists(path)) {
            println("Delete the existing previous release : ${path.toAbsolutePath()}")
            project.delete(path)
        }
    }
}
// publish to maven central
publishing {
    publications {
        create<MavenPublication>("JEBL3") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        maven {
            // publish to local
            name = releaseDir
            url = uri(layout.buildDirectory.dir("${rootDir}/${releaseDir}"))
            // publish to maven
            // url = uri("sftp://repo.mycompany.com:22/maven2")
            // credentials {
            //     username = "user"
            //     password = "password"
            // }
            // authentication {
            //     create<BasicAuthentication>("basic")
            // }
            println("Set the base URL of $releaseDir repository to : ${url.path}")
        }
    }
}

