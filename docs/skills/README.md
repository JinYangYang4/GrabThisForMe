# Project Skills Directory

## Purpose

`docs/skills/` stores reusable project rules and working patterns for UI, data, platform, and architectural decisions.

These documents are project-local guidance.
They are meant to help both humans and AI quickly understand:
- what conventions already exist
- what pattern should be reused first
- when a more specific skill should be opened

## Must Read First

Before adding new pages, new tables, or changing important data/UI behavior, read:

1. `docs/skills/README.md`
2. the relevant category README
3. the concrete skill document for the current task

For data and schema work, also read:
- `docs/skills/data/database-table-catalog.md`

## Directory Rules

- One skill topic should live in one independent folder.
- Each skill folder must contain `SKILL.md`.
- Parent `README.md` files are responsible for classification, lookup guidance, and priority rules.
- Folder names should prefer English keywords for reliable search by AI tools and developers.

## Current Categories

- `ui/`
  Purpose: interface, interaction, layout, and visual rules.

- `data/`
  Purpose: schema design, data chain, repository structure, UI-model decomposition, and table catalog.

- `platform/`
  Purpose: build, test, toolchain, and engineering environment rules.

## Lookup Principle

- Read the parent category README before opening a specific skill.
- Reuse general skills before creating or following page-specific rules.
- Only create or depend on a page-specific skill when the page has clearly unique structure or interaction.
- For build, compile, test, Gradle, and environment issues, check `platform/` first.

## Autonomous Improvement Rule

AI is allowed to proactively choose a more robust, clearer, or better-structured implementation when the current request or current code reveals a clearly better path.

This is allowed only when all of the following remain true:
- the business goal requested by the user does not change
- the user-visible intent is still satisfied
- the project structure and existing conventions are respected
- the change reduces risk, duplication, coupling, schema ambiguity, or maintenance cost
- the relevant docs are updated if the decision changes a reusable pattern or schema rule

Typical allowed examples:
- move a display-only field from a large domain model into a page `UiModel`
- place user-specific display state in a user-state table instead of a pure relation table
- use a relation table that already exists instead of adding a redundant new table
- switch from a destructive parent-table write pattern to a safer upsert strategy
- normalize a data chain when the current implementation obviously mixes unrelated responsibilities

Typical disallowed examples without explicit user confirmation:
- changing product behavior or business semantics
- replacing an established UI style with a different design direction
- introducing a large new abstraction that the repo does not need
- rewriting unrelated modules just because a cleaner architecture is possible

When using a better autonomous approach, AI should:
- keep the change scoped to the task
- preserve compatibility where reasonable
- explain the decision briefly in the final result
- update project docs if the decision becomes a reusable rule

## Cross-Agent Convention

- Folder names should use English lookup terms.
- `SKILL.md` content may use Chinese, but should keep enough stable keywords for search.
- Parent `README.md` files should clearly say which skill to read first and when to move to a narrower skill.

## Current Important Entries

### Data
- `docs/skills/data/database-table-catalog.md`
  Purpose: single source of truth for Room tables and schema lookup.

- `docs/skills/data/table-and-domain-model-guidelines/`
  Purpose: primary guidance for new tables, entity splits, and domain-model boundaries.

### UI
- `docs/skills/ui/layouts/nested-scroll-view-layout/`
  Purpose: common `NestedScrollView` layout constraints and composition rules.

- `docs/skills/ui/forms/create-information-form-pages-ui/`
  Purpose: common create/publish/information-entry page patterns.

### Platform
- `docs/skills/platform/android-build-and-test/`
  Purpose: Android compile, verification, test, and environment guidance.

- `docs/skills/platform/frontend-backend-handoff/`
  Purpose: frontend/backend project paths, backend compile location, sandbox escalation expectations, and handoff notes for new AI conversations.
