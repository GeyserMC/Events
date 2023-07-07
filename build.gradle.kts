plugins {
  id("net.kyori.indra") version "3.1.1"
  id("net.kyori.indra.publishing") version "3.1.1"
  id("net.kyori.indra.licenser.spotless") version "3.1.1"
}

group = "org.geysermc.event"

allprojects { repositories { mavenCentral() } }

subprojects {
  apply {
    plugin("net.kyori.indra")
    plugin("net.kyori.indra.publishing")
    plugin("net.kyori.indra.licenser.spotless")
  }

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
}
