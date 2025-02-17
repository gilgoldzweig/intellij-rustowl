import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.10"
  id("org.jetbrains.intellij.platform") version "2.2.1"
  id("com.ncorti.ktfmt.gradle") version "0.22.0"
}

group = "jp.s6n.idea"

version = "0.1.1"

kotlin { jvmToolchain(21) }

repositories {
  mavenCentral()

  intellijPlatform { defaultRepositories()
  localPlatformArtifacts()
    jetbrainsRuntime()}
}


dependencies {
  intellijPlatform {
//    rustRover("2024.3.4")
    local("C:\\Users\\Gil Goldzweig\\AppData\\Local\\Programs\\RustRover")
//    bundledPlugin("org.jetbrains.rust")
    pluginVerifier()
  }
}

intellijPlatform {

  pluginConfiguration {
    ideaVersion {
      sinceBuild = "243"
      untilBuild = "243.*"
    }
  }

  pluginVerification { ides { recommended() } }

  signing {
    certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
    privateKey = providers.environmentVariable("PRIVATE_KEY")
    password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
  }

  publishing { token = providers.environmentVariable("PUBLISH_TOKEN") }
}

ktfmt { kotlinLangStyle() }
