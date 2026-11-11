# ሪያዱ ሳሊሂን — Riyad as-Salihin (Amharic)

An offline Android reader for **Riyad as-Salihin** (رياض الصالحين) with the full
Arabic text, the English translation, and an Amharic (አማርኛ) translation layer.

The project is a complete, ready-to-open **Android Studio** project: Kotlin,
Jetpack Compose, Material 3, no backend, no account, no ads — all 1,896 hadiths
ship inside the APK.

<p align="center">
  <img src="assets/branding/icon_preview.png" width="420" alt="App icon preview"/>
</p>

## What is inside

- **All 20 books, 1,896 hadiths**, numbered 1–1896 exactly as in the Darussalam /
  sunnah.com edition.
- **Amharic translation layer** — currently the opening twenty hadiths plus a
  curated set of well-known hadiths from every book (47 hadiths). The app shows
  live translation progress and marks hadiths that are not translated yet, so
  nothing is silently missing.
- **Read** with Arabic (RTL, diacritics preserved), Amharic below it, and an
  optional English translation.
- **Search** across Arabic, Amharic and English. Arabic search ignores
  diacritics and normalises alef/ya/ta-marbuta variants.
- **Bookmarks** saved on device, with a bookmark list screen.
- **Continue reading** — remembers your last book and hadith.
- **Copy / share** any hadith as formatted text.
- **Reading comfort** — Arabic and Amharic text sizes, light/dark/system theme.
- **Bilingual UI** — the interface is localised in English and Amharic, with an
  in-app language switch (System / English / አማርኛ).
- **Fully offline**, ~2 MB of bundled JSON assets, no internet permission.

## Open it in Android Studio

1. Install **Android Studio** (Ladybug or newer) with **JDK 17**.
2. `File ▸ Open…` and select the repository root (`riyad`).
3. Let Gradle sync (Gradle 8.9 wrapper, Android Gradle Plugin 8.6.1,
   Kotlin 2.0.21, `compileSdk 35`, `minSdk 24`).
4. Run ▶ on a device or emulator (API 24+).

From the command line:

```bash
./gradlew assembleDebug          # APK in app/build/outputs/apk/debug/
./gradlew testDebugUnitTest      # JVM unit tests
```

The GitHub Actions workflow in `.github/workflows/android.yml` builds the APK
and runs the unit tests on every push.

> If Gradle sync complains about the SDK, create `local.properties` with
> `sdk.dir=/path/to/Android/sdk` (Android Studio does this automatically).

## Project structure

```
app/src/main/
  assets/content/          generated offline content (index.json + 20 book files)
  java/com/riyadsalihin/amharic/
    data/                  models, asset repository, DataStore bookmarks/settings
    ui/home / reader / search / bookmarks / settings / about
    ui/components/         reusable Compose pieces (hadith card, states)
    ui/theme/              Material 3 colours, type, shapes
  res/values/  res/values-am/     English + Amharic UI strings
content/
  books.json               book metadata (Arabic / English / Amharic titles)
  am/hadiths.json          Amharic translations keyed by hadith number
tools/build_content.py     merges sources + translations into app assets
```

The app reads JSON through `kotlinx.serialization` and keeps everything in
memory (≈2 MB); bookmarks, settings and reading position use Jetpack DataStore.
There is no database, no network and no code generation step.

## Content pipeline

The Arabic/English text comes from the open
[`CheeseWithSauce/HadithsJSONFormat`](https://github.com/CheeseWithSauce/HadithsJSONFormat)
dataset (sunnah.com, Riyad as-Salihin, Darussalam numbering). The build script
downloads it once, cleans the text, merges the Amharic translations, validates
the numbering, and writes the assets:

```bash
python3 tools/build_content.py            # rebuild app/src/main/assets/content
python3 tools/build_content.py --check    # validate only
python3 tools/build_content.py --refresh  # re-download the source data
```

It fails loudly on gaps or duplicates in the numbering, empty Arabic text,
unknown translation keys, or Amharic values that do not look like Amharic.

## Help complete the Amharic translation

Translations live in [`content/am/hadiths.json`](content/am/hadiths.json):

```json
{
  "translations": {
    "1": "የአማኞች አለቃ አቡ ሐፍስ ዑመር ... (ቡኻሪና ሙስሊም)",
    "224": "ኑዕማን ኢብኑ በሽር ... (ቡኻሪና ሙስሊም)"
  }
}
```

- Keys are the global hadith numbers 1–1896.
- Add an entry, run `python3 tools/build_content.py`, and the app picks it up —
  no Kotlin changes needed. The home screen and each book row update their
  progress automatically.
- Keep the source citation at the end of the translation, e.g. `(ቡኻሪና ሙስሊም)`.

## Sources, credits and licence

- Arabic and English hadith text: **sunnah.com** (Riyad as-Salihin, Darussalam
  numbering), via the open *HadithsJSONFormat* repository.
- Amharic translation: prepared for this project. It is a **community draft** —
  please verify it against the Arabic original or with qualified scholars before
  relying on it, and open an issue for corrections.
- Code: MIT (see `LICENSE`). Bundled hadith text is a sacred text and is not
  claimed as original work; keep the attribution when redistributing.

Hadith numbering and translations can differ slightly between editions; this app
follows the sunnah.com/Darussalam ordering.
