plugins {
  id("java")
  id("net.kyori.indra.licenser.spotless") version "3.1.1"
  id("net.kyori.indra.publishing") version "3.1.1"
  `java-library`
  `maven-publish`
}

group = "org.geysermc.event"

version = "1.1-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  implementation("com.google.guava", "guava", "17.0")
  implementation("org.lanternpowered", "lmbda", "2.0.0")
  implementation("org.slf4j", "slf4j-api", "2.0.7")

  compileOnly("org.checkerframework", "checker-qual", "3.19.0")

  testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.2")
  testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.8.2")
  testRuntimeOnly("org.slf4j", "slf4j-simple", "2.0.7")
}

tasks.getByName<Test>("test") { useJUnitPlatform() }

indra {
  github("GeyserMC", "Events") {
    ci(true)
    issues(true)
    scm(true)
  }

  mitLicense()

  javaVersions { target(8) }

  publishSnapshotsTo("geysermc", "https://repo.opencollab.dev/maven-snapshots")
  publishReleasesTo("geysermc", "https://repo.opencollab.dev/maven-releases")
}

spotless {
  java {
    palantirJavaFormat()
    formatAnnotations()
  }
}
