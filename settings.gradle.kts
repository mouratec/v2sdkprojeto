pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            // Aponta para a pasta na raiz do projeto
            url = uri("${rootDir}/PaymentSDKAuttar-2.23.4")
        }

        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "v2-sdk-projeto"
include(":app")