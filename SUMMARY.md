# Protolot — M0 / M1 / M2

## M2 — Assembly + parts trust (this milestone) · `0.3.0-m2`

| Area | M2 |
|------|----|
| Assembly | `CMP-ASSEMBLY-LIST` ordered steps with check-off + progress + “Mark build pack reviewed” |
| Confidence | Full scores on BOM lines; `confidence < 0.6` → amber/low-conf + **Override** CTA |
| Override | `CMP-PART-OVERRIDE` bottom sheet — swaps MPN/notes/qty, refreshes DigiKey/Mouser/LCSC link-outs, marks `overridden`, bumps local confidence |
| Vendors | ≥2 (DigiKey + Mouser + LCSC) **link-out stubs** open externally via `ACTION_VIEW` — **no vendor API keys** |
| Estimates | Disclaimer + “Estimate only” total; never verified checkout |
| Kits | Shell polish: list, create-from-BOM, bundle preview; commerce/live drop stubbed M3 |
| Safety | On-device `SafetyGate` refuse; hard dialog; in-session block log |
| Live / Maker Hub | Under **More** only — deferred M3; not ship-gate |
| Theme | **Copper Bench** |
| Identity | `app.protolot.build` · Protolot · **MIT** |
| CI | assembleDebug on main + APK artifact |

### M2 ship gate (target)
1. Assembly ordered checklist with check-off  
2. Confidence scores + working Override that swaps part  
3. ≥2 vendor link-out stubs opening externally  
4. Safety refusals still solid; Live/Hub under More only  

## M1 — Build pack + lots (prior) · `0.2.0-m1`

Wiring structured nets · BOM table + estimate disclaimer · Lot CSV/JSON import (`name,board,variant,mpn,qty,notes`) · Global BOM edit · JSON export/reopen · LLM BYOK · link-out stubs · safety on-device.

### M1 ship gate (met — do not regress)
1. Project tabs: Overview · Wiring · BOM · Assembly · CAD  
2. Wiring = structured nets  
3. BOM table + estimate disclaimer + est. total  
4. Lot CSV and JSON import; clear errors on bad headers  
5. Global BOM field edit across selected members  
6. JSON project pack export + reopen offline  
7. Safety refuse still blocks weapons/explosives  

## M0 — Scaffold (prior)

Shell · Copper Bench · prompt→stub · on-device safety · nav · Providers/Settings · Live/Hub under More.

## M3+ (deferred)

Live Build (BIN + auction), Maker Hub, kit commerce depth — stubs under More until Spec confirms.
