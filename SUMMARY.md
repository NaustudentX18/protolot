# Protolot — M0 vs M1–M2

## M0 — Scaffold (this milestone)

| Area | M0 |
|------|----|
| Home / Ideas | `CMP-PROMPT-COMPOSE` + templates + recents; board chips |
| Project | Tabs Overview · Wiring · BOM · Assembly · CAD — stub content |
| Prompt flow | Create local stub pack → navigate to Overview |
| Safety | On-device `SafetyGate` refuse; hard dialog; in-session block log |
| Confidence | Threshold **0.6**; low-conf lines show Override CTA stub |
| Lots | CSV/JSON stubs; headers exactly `name,board,variant,mpn,qty,notes` |
| Kits | Shell stub |
| Live / Maker Hub | Under **More** only — deferred M3 copy; not ship-gate |
| Providers | LLM BYOK fields + DigiKey/Mouser/LCSC **link-out stubs only** |
| Settings | Safety policy, export formats, license/about (**MIT**) |
| Theme | **Copper Bench** (`#C47A3A` copper on `#12151A` graphite) |
| Identity | `app.protolot.build` · Protolot · **MIT** |
| CI | assembleDebug on main + APK artifact; optional `.release-request` |

### Out of M0

Real wiring viewer, real BOM import, assembly checklist interaction, Live commerce.

## M1 — Build pack + lots

- `CMP-WIRING-VIEW` structured nets
- `CMP-BOM-TABLE` + estimate disclaimer + lot CSV/JSON import + global BOM edit
- JSON project pack export (offline-readable)
- LLM BYOK wire-up (parts remain link-out stubs)

## M2 — Assembly + parts trust

- `CMP-ASSEMBLY-LIST` checklist
- Confidence + `CMP-PART-OVERRIDE` (threshold 0.6)
- ≥2 vendor link-out stubs (still **no vendor API keys**)
- Kits shell polish

## M3+ (deferred)

Live Build (BIN + auction), Maker Hub — stubs under More until Spec confirms.
