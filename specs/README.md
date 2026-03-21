# Specification Folder

> Specs are written first, then used as input for AI-driven implementation and verification.
> They are the single source of truth for this Vaadin Flow addon. Keep them up to date as the project evolves.

Status lifecycle: **Draft** → **Approved** → **Implemented**

## File Overview

| File | Purpose | When to Read |
|------|---------|--------------|
| `project-context.md` | Vision, users, scope, constraints | First — before anything else |
| `architecture.md` | Module structure, tech stack, client-server communication | After project context |
| `datamodel/datamodel.md` | Entry, Resource, EntryProvider, and supporting types | When adding data-driven features |
| `design-system.md` | Theming, CSS customization, entry styling, layout | When modifying appearance |
| `use-cases/use-case-template.md` | Template for individual feature specs | Copy per feature as `use-case-NNN-short-name.md` |
| `verification.md` | Testing strategy (JUnit, Playwright E2E, visual verification) | During and after implementation |

## Workflow

1. **Define context** — Fill in `project-context.md` with problem, vision, scope, and constraints.
2. **Outline architecture** — Fill in `architecture.md` with module structure and communication patterns.
3. **Review design system** — Reference `design-system.md` for theming, styling, and layout rules.
4. **Specify features** — Copy `use-cases/use-case-template.md` once per feature.
5. **Review specs and plans** — As part of the planning phase, have all new or changed specs and implementation plans reviewed by specialized agents (requirements-reviewer, architecture-guard, end-user-reviewer, code-reviewer, ui-designer, fullstack-developer). Fix all issues above nitpick level. No spec or plan moves to "Approved" without passing review.
6. **Implement** — Build each use case, referencing its approved spec for acceptance criteria.
7. **Verify** — Run unit tests + E2E tests. Follow `verification.md` checklists for visual verification.
8. **Mark complete** — Update use case status to **Implemented** once all tests pass.

## Naming Convention

FullCalendar JS calls them "events"; this addon calls them **"entries"**. All specs use `Entry` / `entry` terminology, never "event" (except when referring to Vaadin component events or FullCalendar JS documentation).
