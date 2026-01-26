# FullCalendar Vaadin MCP Server

Model Context Protocol (MCP) server providing documentation, API reference, and code examples for the [FullCalendar Vaadin Flow](https://github.com/stefanuebe/vaadin-fullcalendar) addon.

Use this MCP server to help AI assistants (Claude, GitHub Copilot, etc.) understand and work with FullCalendar in your Vaadin projects.

## Quick Start

Add the MCP server to your AI assistant's configuration:

### Claude Code / Claude Desktop

Add to your MCP configuration (`.mcp.json`, `claude_desktop_config.json`, or settings):

```json
{
  "mcpServers": {
    "fullcalendar": {
      "type": "http",
      "url": "https://v-herd.eu/vaadin-fullcalendar-mcp/mcp"
    }
  }
}
```

### Project-Level Configuration (CLAUDE.md)

Add this to your project's `CLAUDE.md` to automatically enable the server for that project:

```markdown
## MCP Servers

FullCalendar Vaadin documentation server:

\`\`\`json
{
  "mcpServers": {
    "fullcalendar": {
      "type": "http",
      "url": "https://v-herd.eu/vaadin-fullcalendar-mcp/mcp"
    }
  }
}
\`\`\`
```

### Other MCP Clients

Point your MCP client to:
- **MCP endpoint**: `https://v-herd.eu/vaadin-fullcalendar-mcp/mcp`
- **Health check**: `https://v-herd.eu/vaadin-fullcalendar-mcp/health`

## What You Can Do

Once configured, your AI assistant can:

- **Set up new projects** - Get correct Maven dependencies and repository configuration
- **Search documentation** - Find relevant docs, examples, and API references
- **Explore the API** - Get detailed class documentation with methods and fields
- **Find code examples** - Get working code for common use cases
- **Understand the data model** - Learn Entry and Resource properties
- **Migrate between versions** - Get migration guides for version upgrades

## Available Tools

### `get_maven_dependency`
Get Maven coordinates, repository config, and pom.xml snippets for project setup.

```json
{ "name": "get_maven_dependency", "arguments": { "includeScheduler": true } }
```

Returns ready-to-use configuration:
- Artifact IDs: `org.vaadin.stefan:fullcalendar2` and `fullcalendar2-scheduler`
- Repository: Vaadin Directory (`https://maven.vaadin.com/vaadin-addons`)
- Version compatibility (v7 for Vaadin 25+, v6 for Vaadin 14-24)

### `search_docs`
Search across all documentation, API reference, and code examples.

```json
{ "name": "search_docs", "arguments": { "query": "how to add entries", "limit": 10 } }
```

### `get_api_reference`
Get detailed API reference for a Java class.

```json
{ "name": "get_api_reference", "arguments": { "className": "FullCalendar" } }
```

### `list_classes`
List all available Java classes, optionally filtered.

```json
{ "name": "list_classes", "arguments": { "filter": "Entry", "source": "addon" } }
```

### `get_code_example`
Get code examples for specific use cases.

```json
{ "name": "get_code_example", "arguments": { "search": "drag and drop", "category": "events" } }
```

### `get_entry_schema`
Get the Entry model schema with all properties, types, and descriptions.

### `get_resource_schema`
Get the Resource model schema (Scheduler extension) with all properties.

### `list_calendar_views`
List all available calendar views (DayGrid, TimeGrid, List, Timeline, etc.).

```json
{ "name": "list_calendar_views", "arguments": { "includeScheduler": true } }
```

### `list_event_types`
List all server-side event types that can be listened to.

```json
{ "name": "list_event_types", "arguments": { "source": "core" } }
```

### `get_migration_guide`
Get migration guide for upgrading between versions.

```json
{ "name": "get_migration_guide", "arguments": { "toVersion": "7" } }
```

### `get_documentation`
Get full documentation page content.

```json
{ "name": "get_documentation", "arguments": { "path": "Getting-Started.md" } }
```

Available pages: `Getting-Started.md`, `Samples.md`, `Features.md`, `FAQ.md`, `Migration-guides.md`, `Known-issues.md`, `Scheduler-license.md`

## Features

- **Hybrid Search**: Semantic search (OpenAI embeddings) with keyword search fallback
- **Complete API Reference**: All Java classes with methods, fields, and constructors
- **Code Examples**: 50+ examples extracted from documentation and demo sources
- **Model Schemas**: Entry and Resource property definitions with types
- **Calendar Views**: All 44 views with descriptions (including Scheduler views)
- **Event Types**: Server-side event documentation
- **Migration Guides**: Version upgrade instructions
- **Maven Setup**: Dependency coordinates and pom.xml snippets

---

## Self-Hosting

If you need to run your own instance of the MCP server (e.g., for custom modifications or offline use), follow the instructions below.

### Using Docker

```bash
cd mcp-server
docker build -t fullcalendar-mcp .
docker run -p 3000:3000 fullcalendar-mcp
```

For semantic search (optional), pass an OpenAI API key:

```bash
docker run -p 3000:3000 -e OPENAI_API_KEY=sk-... fullcalendar-mcp
```

### Local Development

```bash
cd mcp-server

# Install dependencies
npm install

# Extract data from source files
npm run extract

# Start development server
npm run dev

# Or build and start production server
npm run build
npm start
```

### Configuration

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `PORT` | Server port | `3000` |
| `OPENAI_API_KEY` | OpenAI API key for semantic search | (keyword search only) |
| `DATA_PATH` | Path to extracted data JSON | `./data/extracted.json` |
| `PROJECT_ROOT` | Root of FullCalendar project (for extraction) | `../` |

### Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /mcp` | MCP JSON-RPC endpoint |
| `POST /` | Alternative MCP endpoint |
| `GET /health` | Health check |
| `GET /info` | Server statistics |

### Architecture

```
src/
├── index.ts              # Express HTTP server with MCP protocol
├── types.ts              # TypeScript type definitions
├── tools/
│   └── index.ts          # Tool handler implementations
├── search/
│   ├── index.ts          # Hybrid search orchestration
│   ├── keyword-search.ts # FlexSearch-based keyword search
│   └── semantic-search.ts# OpenAI embeddings search
└── extractors/
    ├── extract-all.ts    # Main extraction script
    ├── java-extractor.ts # Java source parser
    ├── docs-extractor.ts # Markdown documentation parser
    └── model-extractor.ts# Model schema extractor
```

### Updating Documentation

The server extracts documentation at build time. To update:

1. Pull latest changes to the FullCalendar repository
2. Run `npm run extract` or rebuild the Docker image
3. Restart the server

---

## License

MIT License - see the main project LICENSE file.

The FullCalendar Scheduler extension requires a separate [commercial license](https://fullcalendar.io/pricing) from FullCalendar LLC for production use.
