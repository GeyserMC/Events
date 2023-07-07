dependencies {
  implementation(projects.events)
  implementation(libs.auto.service)
  implementation(libs.spoon)

  testImplementation(libs.compile.testing)
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
}

tasks.getByName<Test>("test") { useJUnitPlatform() }
