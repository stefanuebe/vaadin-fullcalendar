# Fix 11: Remove ThemeSystem enum

## Context

PR #223 review comment by stefanuebe:
> Kann komplett raus, unterstützen wir nicht!

The `ThemeSystem` enum (STANDARD, BOOTSTRAP5, BOOTSTRAP) is not relevant for Vaadin apps which use Lumo theming.

## Files to DELETE

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/ThemeSystem.java`

## References to remove

Search for `ThemeSystem` across the codebase:
- `FullCalendar.java` — may have a `setThemeSystem()` method or Option reference
- Tests — remove any ThemeSystem tests
- Docs — remove any ThemeSystem mentions
- Option enum — remove `THEME_SYSTEM` if it exists

Also check:
```bash
grep -rn "ThemeSystem\|THEME_SYSTEM\|themeSystem" addon/ e2e-test-app/ e2e-tests/ docs/
```

## Verification

1. `mvn test -pl addon`
2. Grep for `ThemeSystem` — zero hits
3. `mvn clean install -DskipTests`
