plugins {
  id("java")
  id("net.kyori.indra.license-header") version "2.1.1"
  checkstyle
  `java-library`
}

group = "org.geysermc.event"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.google.guava", "guava", "21.0")
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
