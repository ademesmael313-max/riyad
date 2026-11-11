# Publishing the app

The project builds a debug APK out of the box. This note covers producing a
signed release build for the Play Store or direct distribution.

## 1. Create a signing key

```bash
keytool -genkeypair -v -keystore riyad-release.jks \
  -alias riyad -keyalg RSA -keysize 2048 -validity 10000
```

Keep `riyad-release.jks` and its passwords out of Git (the `.gitignore` already
excludes `*.jks` and `keystore.properties`).

## 2. Add the signing config

Create `keystore.properties` in the repository root:

```properties
storeFile=riyad-release.jks
storePassword=…
keyAlias=riyad
keyPassword=…
```

Then add this to `app/build.gradle.kts`:

```kotlin
import java.util.Properties

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        release {
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}
```

`kotlinx.serialization` is covered by the keep rules in `app/proguard-rules.pro`,
but always smoke-test the minified build on a device before publishing:

```bash
./gradlew bundleRelease     # app/build/outputs/bundle/release/app-release.aab
./gradlew assembleRelease   # app/build/outputs/apk/release/app-release.apk
```

## 3. Before you upload

- **Content review** — have a qualified reviewer read the Amharic translations
  in `content/am/hadiths.json`. The app itself labels them as a draft.
- **Version** — bump `versionCode` / `versionName` in `app/build.gradle.kts`.
- **Play Console declarations** — the app requests **no permissions**, has no
  internet access, collects no data, and shows no ads. Fill the Data safety form
  accordingly.
- **Store listing** — the launcher icon is already provided at every density
  (`app/src/main/res/mipmap-*`); `assets/branding/app_icon_source.png` is the
  1024×1024 source for the Play Store listing.
- **Translation progress** — the home screen shows "85 of 1896 hadiths
  translated". It updates automatically when new translations are added.
