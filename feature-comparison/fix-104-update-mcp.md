# Fix 104: Update MCP Server

## Context

The MCP server (`mcp-server/`) serves documentation, API references, and code examples
to AI assistants. Its data (`mcp-server/data/`) is generated from the project's docs and
source code. After the changes in this branch (Fixes 99–103), the MCP data is stale.

`docs/MCP-Server.md` is a full copy of `mcp-server/README.md` — both must be kept in sync.

## What needs updating

### MCP data (`mcp-server/data/`)

The data files need to be regenerated to reflect all changes from this branch:

- New `setCallbackOption(CallbackOption, String)` / `setCallbackOption(SchedulerCallbackOption, String)` API
- `CallbackOption` / `SchedulerCallbackOption` as nested enums
- All deprecated callback setters (`setEntryClassNamesCallback` etc.)
- `FullCalendar.Option` / `FullCalendarScheduler.SchedulerOption` qualified names
- Updated Samples.md, Migration-guides.md, Release-notes.md content
- Fix 103 (once implemented): updated `addEntryNativeEventListener` + `ENTRY_DID_MOUNT` behavior

### README / docs

Check `mcp-server/README.md` (and `docs/MCP-Server.md`) for any references to old API
names or class names that were changed in this branch.

## Steps

1. Check how MCP data is generated (build script, source files)
2. Regenerate `mcp-server/data/extracted.json` and `mcp-server/data/summary.json`
3. Verify the MCP server still starts and responds correctly
4. Update `mcp-server/README.md` if needed
5. Sync `docs/MCP-Server.md` ← copy of README

## Note

Fix 103 (ENTRY_DID_MOUNT cleanup) should be implemented before this fix,
since it changes the `addEntryNativeEventListener` / `setEntryDidMountCallback` API surface.
