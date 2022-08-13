plugins {
  id("java")
  id("net.kyori.indra.license-header") version "2.1.1"
  id("com.jfrog.artifactory") version "4.29.0"
  checkstyle
  `java-library`
  `maven-publish`
}

group = "org.geysermc.event"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.google.guava", "guava", "17.0")
  implementation("org.lanternpowered", "lmbda", "2.0.0")

  compileOnly("org.checkerframework", "checker-qual", "3.19.0")

  testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.2")
  testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.8.2")
}

checkstyle {
  toolVersion = "10.3.2"
  maxErrors = 0
  maxWarnings = 0

  // wanted to include indra's checkstyle,
  // but wasn't able to set the tool version without having to use the base indra plugin
  val checkstyleDir = rootProject.projectDir.resolve(".checkstyle")
  configDirectory.set(checkstyleDir)

  val configProps = configProperties
  configProps["configDirectory"] = checkstyleDir
  configProps["severity"] = "error"
}

license {
  newLine(true)
}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8

  withSourcesJar()
}

fun Project.isSnapshot(): Boolean =
  version.toString().endsWith("-SNAPSHOT")

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      groupId = project.group as String
      artifactId = project.name
      version = project.version as String

      from(components["java"])
    }
  }
}

artifactory {
  setContextUrl("https://repo.opencollab.dev/artifactory")
  publish {
    repository {
      setRepoKey(if (isSnapshot()) "maven-snapshots" else "maven-releases")
      setMavenCompatible(true)
    }
    defaults {
      publications("mavenJava")
      setPublishArtifacts(true)
      setPublishPom(true)
      setPublishIvy(false)
    }
  }
}
