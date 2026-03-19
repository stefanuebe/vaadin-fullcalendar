# Next Step 2: Extract E2E Test Views into Dedicated App

## Goal
Test views (routes `/test/*`) currently live in the `demo/` module. They pollute
the demo app's nav and are conceptually separate. Move them to a dedicated
`e2e-test-app/` Spring Boot module so `demo/` stays clean.

## Status
✅ DONE — commit 7b621d0

## Current situation
- Test views: `demo/src/main/java/.../ui/view/testviews/` (7+ Java classes)
- Demo nav: TestLayout + MenuItem annotations expose these to the demo sidebar
- `e2e-test-app/` exists but currently contains only generated Vaadin frontend
  artifacts (no Java sources) — it appears to be a compiled frontend output, not
  a standalone Spring Boot app yet

## What needs to happen

### 1. Create `e2e-test-app/` as a proper Maven module
- Add `pom.xml` (Spring Boot, Vaadin 25, depends on `fullcalendar2` + `fullcalendar2-scheduler`)
- Add `Application.java` + `application.properties` (port 8080, context root `/`)
- Add Vaadin `MainLayout.java` (no nav needed — test views are accessed directly by URL)

### 2. Move test views
- Move all `Phase*TestView.java` (already renamed per next-1) from `demo/` to `e2e-test-app/`
- Remove `TestLayout` from `demo/` if no longer needed
- Remove `MenuItem` references to test views from demo nav

### 3. Update Playwright config
- `playwright.config.js` baseURL already points to `http://localhost:8080` — keep as-is
- Update `webServer` command in config if it currently starts the demo app;
  it should start `e2e-test-app` instead

### 4. Update Maven parent
- Add `e2e-test-app` as a module in root `pom.xml` if not already present

### 5. Verify
- `demo/` nav no longer shows any `/test/*` routes
- All Playwright tests still pass against `e2e-test-app`

## Notes
- Do next-1 (rename phase labels) BEFORE this step, so we move already-renamed files
- Keep `demo/` runnable independently — no shared state with `e2e-test-app`

## Mandatory: Code Review
Run the `code-reviewer` agent on all changed files before marking this step complete.
This is non-negotiable — no step is done without a code review.

## Mandatory: End-User Review
Run the `end-user-reviewer` agent on any changed docs or Javadoc.
Fix all findings and re-run until the reviewer gives a clean pass.
No step is done without end-user sign-off on documentation.
