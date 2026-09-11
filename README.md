# Protolot — M0 Scaffold

**Protolot** turns a plain-language hardware idea into a build pack — wiring, BOM, assembly, CAD hooks — then lets labs batch lots and kit. Clean-room, Android-first, open-source.

- **Package / applicationId:** `app.protolot.build`
- **Display name:** Protolot
- **Brand:** Copper Bench (graphite + copper)
- **License:** MIT
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

## What's in M0

- **SCR-HOME** Ideas: `CMP-PROMPT-COMPOSE`, templates, recents, `CMP-SAFETY-REFUSE` dialog
- Prompt → local stub project pack → **SCR-PROJECT** Overview
- On-device safety refuse (weapons/explosives) — not LLM-only
- Project tabs: Overview · Wiring · BOM · Assembly · CAD hooks (stubs)
- BOM stub shows confidence; **< 0.6** → low-confidence chip + Override CTA
- **SCR-LOTS** CSV/JSON import stub; canonical headers `name,board,variant,mpn,qty,notes`
- **SCR-KITS** shell · **More** hosts Providers, Settings, Live & Maker Hub stubs
- Live / Maker Hub: deferred M3 copy — under More only (not primary nav)
- Copper Bench theme · MIT LICENSE

See [SUMMARY.md](SUMMARY.md) for M0 vs M1–M2.

## Optional cut-release

Push a `.release-request` file on `main` (contents = tag name, e.g. `v0.1.0-m0`) to trigger the cut-release job.
