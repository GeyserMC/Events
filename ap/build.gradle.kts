dependencies {
  implementation(projects.events)
  implementation(libs.auto.service)
  implementation(libs.javapoet)

  testImplementation(libs.compile.testing)
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
}

tasks.getByName<Test>("test") { useJUnitPlatform() }

tasks.withType<Test>().configureEach {
  // cannot use javaVersion because it throws errors, but the version is also in the test name
  if (name.startsWith("testJava")) {
    var javaVersion = JavaVersion.toVersion(name.substring(8).toInt())
    if (!javaVersion.isCompatibleWith(JavaVersion.VERSION_16)) {
      return@configureEach
    }
  }

  // See: https://github.com/google/compile-testing/issues/222
  jvmArgs(
      "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
}
