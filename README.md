# Protolot — M2 Assembly + parts trust

**Protolot** turns a plain-language hardware idea into a build pack — wiring, BOM, assembly, CAD hooks — then lets labs batch lots and kit. Clean-room, Android-first, open-source.

- **Package / applicationId:** `app.protolot.build`
- **Display name:** Protolot
- **Brand:** Copper Bench (graphite + copper)
- **License:** MIT
- **Version:** `0.3.0-m2`
- **minSdk 26 · compileSdk 35 · Compose + Material 3 + Navigation**
- **Ship path:** GitHub APK first (this repo)

> Clean-room: no Blueprint, Whatnot, or OFH marks or copy.

## Open in Android Studio

1. Clone this repo.
2. Open the project root in Android Studio (Giraffe+ / Koala recommended).
3. Let Gradle sync (wrapper uses Gradle 8.9).

## Build debug APK

```bash
./gradlew :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

CI runs `assembleDebug` on every push to `main` and uploads the APK artifact.

## Sideload

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## What's in M2

- **CMP-ASSEMBLY-LIST** ordered steps with check-off + progress
- **Confidence** on BOM lines; low-conf (`< 0.6`) callout + **CMP-PART-OVERRIDE** sheet that actually swaps the part
- **≥2 vendor link-outs** (DigiKey / Mouser / LCSC-class) open externally — **no vendor API keys**
- BOM estimate honesty (never verified checkout)
- **SCR-KITS** shell polish (list / from-BOM / bundle preview; commerce M3)
- M1 retained: Wiring nets, lot CSV/JSON, global BOM edit, JSON export, LLM BYOK, on-device safety
- Live / Maker Hub stubs under **More** only (M3)

See [SUMMARY.md](SUMMARY.md) and [docs/AC-CHECKLIST.md](docs/AC-CHECKLIST.md).

## Lot CSV template

Canonical headers (exact):

```
name,board,variant,mpn,qty,notes
```

Also shipped as `app/src/main/assets/lot-template.csv`.

## Optional cut-release

Push a `.release-request` file on `main` (contents = tag name, e.g. `v0.3.0-m2`) to trigger the cut-release job.
