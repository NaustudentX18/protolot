# Protolot — M1 Build pack + lots

**Protolot** turns a plain-language hardware idea into a build pack — wiring, BOM, assembly, CAD hooks — then lets labs batch lots and kit. Clean-room, Android-first, open-source.

- **Package / applicationId:** `app.protolot.build`
- **Display name:** Protolot
- **Brand:** Copper Bench (graphite + copper)
- **License:** MIT
- **Version:** `0.2.0-m1`
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

## What's in M1

- **CMP-WIRING-VIEW** structured readable nets (pin → net → pin)
- **CMP-BOM-TABLE** with estimate disclaimer, vendor search link-outs, est. total
- Lot **CSV/JSON** import (`name,board,variant,mpn,qty,notes`) + Classroom/Fleet labels + import errors
- **Global BOM edit** across selected lot members
- **JSON project pack** export + offline reopen/import
- **LLM BYOK** Providers (OpenAI-compatible); stub generation when unconfigured; Retry on fail
- On-device safety refuse (weapons/explosives) — not LLM-only
- Live / Maker Hub stubs under **More** only (M3)

See [SUMMARY.md](SUMMARY.md) for M0 vs M1–M2.

## Lot CSV template

Canonical headers (exact):

```
name,board,variant,mpn,qty,notes
```

Also shipped as `app/src/main/assets/lot-template.csv`.

## Optional cut-release

Push a `.release-request` file on `main` (contents = tag name, e.g. `v0.2.0-m1`) to trigger the cut-release job.
