# JEBL3 (Java Evolutionary Biology Library)

[![jebl3 tests](https://github.com/LinguaPhylo/jebl3/actions/workflows/jebl3.yml/badge.svg)](https://github.com/LinguaPhylo/jebl3/actions/workflows/jebl3.yml)

This is a fork from [jebl2](https://github.com/rambaut/jebl2) at ([1f13ac7f31106a8efa54ec99bdfb10a9f5a51496](https://github.com/rambaut/jebl2/tree/1f13ac7f31106a8efa54ec99bdfb10a9f5a51496)), 
but improved to use Java 16 and [the Java Platform Module System (JPMS)](https://www.infoq.com/articles/java9-osgi-future-modularity/).

The current exporting packages can be seen from [module-info.java](src/main/java/module-info.java).

From version 3.1.0, JEBL3 will only release to the Maven Central Repository.

## Gradle

1. Clean : `./gradlew clean`
2. Build with info : `./gradlew build --info`
3. Create jar files : `./gradlew jar` and look for the folder "build/libs"
4. Publish.
