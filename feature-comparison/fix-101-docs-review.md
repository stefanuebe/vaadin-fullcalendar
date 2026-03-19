# Fix 101: Full documentation review and update

**Voraussetzung:** Alle anderen Fixes (01–12, 99, 100) müssen abgeschlossen sein.

## Context

Die Dokumentation in `docs/` wurde bei früheren Fixes nur punktuell angepasst. Durch Fix 01 (Remove typed setters), Fix 02 (Event→Entry), und die CustomButton/buttonIcons-Entfernung sind viele Referenzen veraltet. Ziel ist eine vollständige Durchsicht aller Docs gegen den tatsächlichen Stand der API.

## Scope

Alle Dateien unter `docs/`:
- `Features.md`
- `Samples.md`
- `Release-notes.md`
- `Migration-guides.md`
- `FAQ.md`
- `Known-issues.md`
- `Home.md`

## Was zu prüfen ist

1. Entfernte Methoden (Fix 01)
2. Umbenannte Methoden (Fix 02)
3. Entfernte Klassen (CustomButton-Entfernung)
4. Neue API (sicherstellen, dass sie dokumentiert ist)
5. Samples.md — Code-Beispiele

Alle Code-Beispiele gegen aktuelle API compilieren/prüfen:
- Scheduler-Beispiele mit Resource-Methoden aktualisieren

6. Release-notes.md

Den gesamten Release-notes-Eintrag für v7 reviewen und sicherstellen, dass er den tatsächlichen API-Stand widerspiegelt (keine entfernten Methoden mehr nennen, neue Methoden ergänzen).

## Verification

1. Grep über `docs/` nach allen gefundenen veralteten Punkten — zero hits für entfernte Methoden
2. Spot-check: 5 zufällige Code-Beispiele aus Samples.md gegen tatsächliche API verifizieren
3. Peer-review: Jemand anderes liest Features.md und prüft, ob die beschriebenen Features wirklich so funktionieren
