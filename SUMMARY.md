# Protolot — M0 vs M1–M2

## M1 — Build pack + lots (this milestone) · `0.2.0-m1`

| Area | M1 |
|------|----|
| Wiring | `CMP-WIRING-VIEW` structured nets (named pins / connections) — not empty stub |
| BOM | `CMP-BOM-TABLE` ref · qty · notes · vendor link-out columns + estimate disclaimer + est. total |
| Confidence | Threshold **0.6**; low-conf chip + Override CTA (stub; full override M2) |
| Lots | CSV **and** JSON import; headers exactly `name,board,variant,mpn,qty,notes`; Classroom/Fleet labels; ERR-IMPORT-PARSE |
| Global BOM | `CMP-GLOBAL-BOM-EDIT` applies field change across selected lot members |
| Export | JSON project pack export + offline reopen/import in-app |
| Providers | LLM BYOK wire-up (OpenAI-compatible); Test connection; stub gen when unconfigured; ERR-GEN-FAIL Retry |
| Parts | DigiKey/Mouser/LCSC **link-out stubs only** — no vendor API keys |
| Safety | On-device `SafetyGate` refuse; hard dialog; in-session block log |
| Live / Maker Hub | Under **More** only — deferred M3; not ship-gate |
| Theme | **Copper Bench** |
| Identity | `app.protolot.build` · Protolot · **MIT** |
| CI | assembleDebug on main + APK artifact |

### M1 ship gate (met)
1. Project tabs: Overview · Wiring · BOM · Assembly · CAD  
2. Wiring = structured nets (non-empty for stub/LLM packs)  
3. BOM table + estimate disclaimer + est. total  
4. Lot CSV and JSON import; clear errors on bad headers  
5. Global BOM field edit across selected members  
6. JSON project pack export + reopen offline  
7. Safety refuse still blocks weapons/explosives  

## M0 — Scaffold (prior)

Shell · Copper Bench · prompt→stub · on-device safety · nav · Providers/Settings stubs · Live/Hub under More.

## M2 — Assembly + parts trust (next)

- `CMP-ASSEMBLY-LIST` checklist interaction  
- Full `CMP-PART-OVERRIDE`  
- ≥2 vendor link-out stubs polish (still **no vendor API keys**)  
- Kits shell polish  

## M3+ (deferred)

Live Build (BIN + auction), Maker Hub — stubs under More until Spec confirms.
