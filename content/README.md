# Translation source

- `books.json` — the 20 books of Riyad as-Salihin: id, Arabic title, English
  title, Amharic title and the source file it maps to.
- `am/hadiths.json` — Amharic translations keyed by the global hadith number
  (1–1896). Hadiths without a key are shown as "translation coming soon".

After editing either file, rebuild the offline assets:

```bash
python3 tools/build_content.py
```

The script validates that every key is a real hadith number, that the Arabic and
English text is present for all 1,896 hadiths, and that Amharic values actually
contain Ethiopic script. It exits non-zero on problems, so it can be used in CI.

Translation style used so far:

- honourifics: `(ሰ.ዐ.ወ)` after the Prophet, `(ረ.ዐ)` after a Companion;
- Quranic and technical terms transliterated as Ethiopian Muslims use them
  (ሶላት, ዚክር, ጀነት, ገሃነም, ሶዳቃ, ውዱእ);
- the source of the hadith in Amharic at the end, e.g. `(ቡኻሪና ሙስሊም)`.
