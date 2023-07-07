dependencies {
  implementation(libs.geantyref)
  implementation(libs.lmbda)
  implementation(libs.slf4j.api)

  compileOnly(libs.checker.qual)

  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
  testRuntimeOnly(libs.slf4j.simple)
}

tasks.getByName<Test>("test") { useJUnitPlatform() }
