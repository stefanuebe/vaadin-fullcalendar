# FullCalendar Vaadin MCP Server

Model Context Protocol (MCP) server providing documentation, API reference, and code examples for the FullCalendar Vaadin Flow addon.

## Features

- **Hybrid Search**: Semantic search (with OpenAI embeddings) + keyword search fallback
- **API Reference**: Complete Java class documentation with methods, fields, and constructors
- **Code Examples**: Extracted from documentation and demo sources
- **Model Schemas**: Entry and Resource property definitions
- **Calendar Views**: List of all available views with descriptions
- **Event Types**: Server-side event documentation
- **Migration Guides**: Version upgrade instructions

## Quick Start

### Using Docker (Recommended)

Build and run from the project root:

```bash
cd mcp-server
docker build -t fullcalendar-mcp .
docker run -p 3000:3000 fullcalendar-mcp
```

For semantic search, pass the OpenAI API key:

```bash
docker run -p 3000:3000 -e OPENAI_API_KEY=sk-... fullcalendar-mcp
```

### Local Development

```bash
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

## Configuration

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `PORT` | Server port | `3000` |
| `OPENAI_API_KEY` | OpenAI API key for semantic search | (none, uses keyword search) |
| `DATA_PATH` | Path to extracted data JSON | `./data/extracted.json` |
| `PROJECT_ROOT` | Root of FullCalendar project (for extraction) | `../` |

## MCP Endpoints

- `POST /mcp` - MCP JSON-RPC endpoint
- `POST /` - Alternative MCP endpoint
- `GET /health` - Health check
- `GET /info` - Server statistics

## Available Tools

### `search_docs`
Search across all documentation, API reference, and code examples.

```json
{
  "name": "search_docs",
  "arguments": {
    "query": "how to add entries",
    "limit": 10,
    "mode": "auto"
  }
}
```

### `get_api_reference`
Get detailed API reference for a Java class.

```json
{
  "name": "get_api_reference",
  "arguments": {
    "className": "FullCalendar"
  }
}
```

### `list_classes`
List all available Java classes.

```json
{
  "name": "list_classes",
  "arguments": {
    "filter": "Entry",
    "source": "addon"
  }
}
```

### `get_code_example`
Get code examples for specific use cases.

```json
{
  "name": "get_code_example",
  "arguments": {
    "search": "drag and drop",
    "category": "events"
  }
}
```

### `get_entry_schema`
Get the Entry model schema with all properties.

### `get_resource_schema`
Get the Resource model schema (Scheduler extension).

### `list_calendar_views`
List all available calendar views.

```json
{
  "name": "list_calendar_views",
  "arguments": {
    "includeScheduler": true
  }
}
```

### `list_event_types`
List all server-side event types.

```json
{
  "name": "list_event_types",
  "arguments": {
    "source": "core"
  }
}
```

### `get_migration_guide`
Get migration guide for version upgrades.

```json
{
  "name": "get_migration_guide",
  "arguments": {
    "toVersion": "7"
  }
}
```

### `get_documentation`
Get full documentation page content.

```json
{
  "name": "get_documentation",
  "arguments": {
    "path": "Samples.md"
  }
}
```

## Using with AI Assistants

### Claude Code / Claude.ai

Add to your `.mcp.json` or MCP configuration:

```json
{
  "mcpServers": {
    "fullcalendar": {
      "type": "http",
      "url": "https://mcp.fullcalendar-flow.io/mcp"
    }
  }
}
```

### GitHub Copilot / Other MCP Clients

Point your MCP client to the server URL:
- MCP endpoint: `https://mcp.fullcalendar-flow.io/mcp`
- Health check: `https://mcp.fullcalendar-flow.io/health`

### Example: CLAUDE.md Integration

Add this to your project's `CLAUDE.md`:

```markdown
## MCP Servers

FullCalendar Vaadin documentation server:

\`\`\`json
{
  "mcpServers": {
    "fullcalendar": {
      "type": "http",
      "url": "https://mcp.fullcalendar-flow.io/mcp"
    }
  }
}
\`\`\`
```

## Architecture

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

## Auto-Update

The server extracts documentation at Docker build time. To update:

1. Pull latest changes to the FullCalendar repository
2. Rebuild the Docker image
3. Deploy the new image

For CI/CD, trigger a rebuild when the main repository is updated.

## License

MIT License - see the main project LICENSE file.

The FullCalendar Scheduler extension requires a separate commercial license from FullCalendar LLC.
