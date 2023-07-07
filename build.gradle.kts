plugins {
  id("net.kyori.indra") version "3.1.1"
  id("net.kyori.indra.publishing") version "3.1.1"
  id("net.kyori.indra.licenser.spotless") version "3.1.1"
}

group = "org.geysermc.event"

version = "1.2-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  implementation("io.leangen.geantyref", "geantyref", "1.3.13")
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

  javaVersions { target(17) }

  publishSnapshotsTo("geysermc", "https://repo.opencollab.dev/maven-snapshots")
  publishReleasesTo("geysermc", "https://repo.opencollab.dev/maven-releases")
}

spotless {
  java {
    palantirJavaFormat()
    formatAnnotations()
  }
}
