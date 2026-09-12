# Protolot AC checklist — honest status (`0.3.0-m2`)

Legend: ✅ met · 🟡 partial / shell · ❌ not met · ⏸ deferred (by design)

## AC-Shell (M0)
- ✅ Installs as `app.protolot.build`, display name Protolot
- ✅ Nav: Home, Project, Lots, Providers, Settings (+ Kits primary; Live/Hub under More)
- ✅ Prompt → stub/LLM project
- ✅ No Blueprint / Whatnot / OFH marks

## AC-Safety (M0+)
- ✅ Weapons/explosives primary-purpose refuse with hard dialog
- ✅ On-device `SafetyGate` (not LLM-only)
- ✅ In-session blocked log; no partial weapon BOM
- ✅ Settings safety policy summary

## AC-Project-Pack (M1)
- ✅ Tabs: Overview · Wiring · BOM · Assembly · CAD
- ✅ Wiring structured nets (non-empty stub)
- ✅ BOM table (ref, qty, notes, vendor link columns)
- ✅ JSON export + offline reopen/import

## AC-Lots (M1)
- ✅ CSV and JSON lot import
- ✅ Headers exactly `name,board,variant,mpn,qty,notes` + template asset
- ✅ Classroom / Fleet distinguishable labels + batch count
- ✅ Global BOM edit across selected members
- ✅ Import errors actionable (ERR-IMPORT-PARSE)

## AC-Assembly-Parts (M2)
- ✅ Assembly ordered steps with check-off (`CMP-ASSEMBLY-LIST`)
- ✅ BOM confidence scores; Override CTA when `confidence < 0.6`
- ✅ `CMP-PART-OVERRIDE` sheet swaps MPN/notes/qty, refreshes link-outs, marks overridden
- ✅ ≥2 vendor link-out stubs (DigiKey + Mouser + LCSC) open externally — **no vendor API keys**
- ✅ Estimate copy / disclaimer / “Estimate only” total (never verified checkout)

## AC-Providers (M1–M2)
- ✅ LLM BYOK OpenAI-compatible + Test connection
- ✅ Parts = DigiKey/Mouser/LCSC link-out stubs; graceful unkeyed degrade
- ✅ Failed generation Retry + Check Providers (not silent empty)

## AC-Brand-Ship
- ✅ MIT LICENSE
- ✅ Debug APK path documented (README + CI)

## Kits / Live (scope honesty)
- 🟡 SCR-KITS shell polished (list, from-BOM, bundle preview) — commerce/pricing/stock/live drop **⏸ M3**
- ⏸ SCR-LIVE / CMP-BIN / CMP-AUCTION / SCR-MAKER-HUB — stubs under **More only**; not M2 ship-gate
- ⏸ CAD export artifacts — hooks UI only (**M4**)

## Gaps / known limits (honest)
- In-memory store only (no Room persistence across process death)
- Stub/LLM packs still use canned wiring/BOM structure when unkeyed
- Override bumps *local display* confidence; no live catalog verification (by lock)
- Kits cannot price, stock, or list on Live (M3)
- No instrumented UI tests in this milestone

## M1 gate regression check
- ✅ Wiring + BOM + lot import/export still present — do not treat M2 as chat-only
