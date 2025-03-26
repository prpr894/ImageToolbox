/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.image.toolbox.application)
    alias(libs.plugins.image.toolbox.hilt)
}

android {
    var isFoss = false

    val supportedAbi = arrayOf("armeabi-v7a", "arm64-v8a", "x86_64")

    namespace = "ru.tech.imageresizershrinker"

    defaultConfig {
        vectorDrawables.useSupportLibrary = true

        applicationId = "ru.tech.imageresizershrinker"
        versionCode = libs.versions.versionCode.get().toIntOrNull()
        versionName = System.getenv("VERSION_NAME") ?: libs.versions.versionName.get()

        ndk {
            abiFilters.clear()
            //noinspection ChromeOsAbiSupport
            abiFilters += supportedAbi.toSet()
        }

        setProperty("archivesBaseName", "image-toolbox-$versionName${if (isFoss) "-foss" else ""}")
    }

    androidResources {
        generateLocaleConfig = true
    }

    flavorDimensions += "app"

    productFlavors {
        create("foss") {
            dimension = "app"
            isFoss = true
            extra.set("gmsEnabled", false)
        }
        create("market") {
            dimension = "app"
            extra.set("gmsEnabled", true)
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_launcher_name", "Image Toolbox DEBUG")
            resValue("string", "file_provider", "ru.tech.imageresizershrinker.fileprovider.debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_launcher_name", "Image Toolbox")
            resValue("string", "file_provider", "ru.tech.imageresizershrinker.fileprovider")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            //noinspection ChromeOsAbiSupport
            include(*supportedAbi)
            isUniversalApk = true
        }
    }
    packaging {
        jniLibs {
            pickFirsts.add("lib/*/libcoder.so")
        }
        resources {
            excludes += "META-INF/"
            excludes += "kotlin/"
            excludes += "org/"
            excludes += ".properties"
            excludes += ".bin"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

    aboutLibraries {
        excludeFields = arrayOf("generated")
    }

    buildFeatures {
        resValues = true
    }
}

dependencies {
    implementation(projects.feature.root)
    implementation(projects.feature.mediaPicker)
    implementation(projects.feature.quickTiles)
    implementation(libs.bouncycastle.pkix)
    implementation(libs.bouncycastle.provider)
}

allprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.caverock:androidsvg-aar:1.4")).using(module("com.github.deckerst:androidsvg:cc9d59a88f"))
        }
    }
}

afterEvaluate {
    android.productFlavors.forEach { flavor ->
        tasks.matching { task ->
            listOf("GoogleServices", "Crashlytics").any {
                task.name.contains(it)
            }.and(
                task.name.contains(
                    flavor.name.replaceFirstChar(Char::uppercase)
                )
            )
        }.forEach { task ->
            task.enabled = flavor.extra.get("gmsEnabled") == true
        }
    }
}
