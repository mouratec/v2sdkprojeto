plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "br.com.auttar.v2sdkprojeto"
    compileSdk = 34

    // Obrigatório para POS
    useLibrary("org.apache.http.legacy")

    defaultConfig {
        applicationId = "br.com.auttar.v2sdkprojeto"
        minSdk = 22 // Conforme manual v1.49
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Suporte Java 8 Obrigatório
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Exclusão de arquivos duplicados
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/license.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/notice.txt")
            excludes.add("META-INF/ASL2.0")
        }
    }
}

// BLOCO CORRIGIDO (Sem aninhamento)
dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // SDK Auttar e auxiliares [cite: 227-229]
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.4")
    implementation("org.apache.commons:commons-lang3:3.8")
    implementation("org.apache.commons:commons-text:1.7")

    // Sua versão do SDK
    implementation("br.com.auttar.mobile:payment-sdk-homologRelease:2.23.4")

    // --- CORREÇÃO PARA O ERRO JAVA 21 (bcprov) ---
    // Isso resolve o erro "Unsupported class file major version 65" mantendo o Java 17
    constraints {
        implementation("org.bouncycastle:bcprov-jdk18on:1.77") {
            because("A versão 1.79 exige Java 21. Forçando 1.77 para compatibilidade com Java 17.")
        }
    }
}