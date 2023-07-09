dependencies {
  implementation(libs.geantyref)
  implementation(libs.slf4j.api)

  compileOnly(libs.checker.qual)

  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
  testRuntimeOnly(libs.slf4j.simple)
  testCompileOnly(libs.checker.qual)
}

tasks.getByName<Test>("test") { useJUnitPlatform() }
