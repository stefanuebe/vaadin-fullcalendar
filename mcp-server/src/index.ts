import express, { Request, Response } from 'express';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StreamableHTTPServerTransport } from '@modelcontextprotocol/sdk/server/streamableHttp.js';
import { HybridSearch } from './search/index.js';
import { ToolHandlers } from './tools/index.js';
import type { ExtractedData } from './types.js';
import { z } from 'zod';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = parseInt(process.env.PORT || '3000', 10);
const DATA_PATH = process.env.DATA_PATH || path.join(__dirname, '../data/extracted.json');

// Server state
let data: ExtractedData;
let search: HybridSearch;
let tools: ToolHandlers;
let isInitialized = false;

async function loadData(): Promise<void> {
  console.log('Loading data...');
  console.log(`Data path: ${DATA_PATH}`);

  if (!fs.existsSync(DATA_PATH)) {
    throw new Error(`Data file not found: ${DATA_PATH}. Run 'npm run extract' first.`);
  }

  const rawData = fs.readFileSync(DATA_PATH, 'utf-8');
  data = JSON.parse(rawData) as ExtractedData;

  console.log(`Loaded: ${data.classes.length} classes, ${data.documentation.length} docs, ${data.examples.length} examples`);

  // Initialize search
  search = new HybridSearch();
  await search.initialize(data.documentation, data.classes, data.examples);
  console.log(`Search initialized (mode: ${search.getSearchMode()})`);

  // Initialize tool handlers
  tools = new ToolHandlers(data, search);

  isInitialized = true;
  console.log('Data loaded successfully');
}

function createMcpServer(): McpServer {
  const mcp = new McpServer(
    {
      name: 'fullcalendar-vaadin-mcp',
      version: data.version,
    },
    {
      capabilities: {
        tools: {},
        resources: {},
      },
    },
  );

  // --- Tools ---

  mcp.registerTool('search_docs', {
    description: 'Search across FullCalendar Vaadin documentation, API reference, and code examples. Supports semantic search (with OpenAI) and keyword search.',
    inputSchema: {
      query: z.string().describe('Search query'),
      limit: z.number().optional().describe('Maximum results (default: 10)'),
      mode: z.enum(['auto', 'semantic', 'keyword']).optional().describe('Search mode (default: auto)'),
    },
  }, async (args) => {
    const result = await tools.searchDocs({ query: args.query, limit: args.limit, mode: args.mode });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('get_api_reference', {
    description: 'Get detailed API reference for a specific Java class including methods, fields, and constructors.',
    inputSchema: {
      className: z.string().describe('Class name (e.g., "FullCalendar", "Entry", "Resource")'),
    },
  }, async (args) => {
    const result = tools.getApiReference({ className: args.className });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('list_classes', {
    description: 'List all available Java classes in the FullCalendar addon.',
    inputSchema: {
      filter: z.string().optional().describe('Filter by name or description'),
      source: z.enum(['addon', 'addon-scheduler']).optional().describe('Filter by module'),
    },
  }, async (args) => {
    const result = tools.listClasses({ filter: args.filter, source: args.source });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('get_code_example', {
    description: 'Get code examples for specific use cases.',
    inputSchema: {
      id: z.string().optional().describe('Specific example ID'),
      search: z.string().optional().describe('Search term to find examples'),
      category: z.string().optional().describe('Category filter (e.g., "data-provider", "events", "scheduler")'),
    },
  }, async (args) => {
    const result = tools.getCodeExample({ id: args.id, search: args.search, category: args.category });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('get_entry_schema', {
    description: 'Get the Entry model schema with all properties, types, and descriptions.',
  }, async () => {
    const result = tools.getEntrySchema();
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('get_resource_schema', {
    description: 'Get the Resource model schema (Scheduler extension) with all properties.',
  }, async () => {
    const result = tools.getResourceSchema();
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('list_calendar_views', {
    description: 'List all available calendar views (DayGrid, TimeGrid, List, Timeline, etc.).',
    inputSchema: {
      includeScheduler: z.boolean().optional().describe('Include Scheduler-specific views (default: true)'),
    },
  }, async (args) => {
    const result = tools.listCalendarViews({ includeScheduler: args.includeScheduler });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('list_event_types', {
    description: 'List all server-side event types that can be listened to.',
    inputSchema: {
      source: z.enum(['core', 'scheduler']).optional().describe('Filter by module'),
    },
  }, async (args) => {
    const result = tools.listEventTypes({ source: args.source });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('get_migration_guide', {
    description: 'Get migration guide for upgrading between versions.',
    inputSchema: {
      fromVersion: z.string().optional().describe('Source version (e.g., "6")'),
      toVersion: z.string().optional().describe('Target version (e.g., "7")'),
    },
  }, async (args) => {
    const result = tools.getMigrationGuide({ fromVersion: args.fromVersion, toVersion: args.toVersion });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('get_documentation', {
    description: 'Get full documentation page by path.',
    inputSchema: {
      path: z.string().describe('Documentation path (e.g., "Samples.md", "FAQ.md")'),
    },
  }, async (args) => {
    const result = tools.getDocumentation({ path: args.path });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  mcp.registerTool('get_maven_dependency', {
    description: 'Get Maven dependency coordinates, repository configuration, and version compatibility for setting up a new project with FullCalendar for Vaadin Flow.',
    inputSchema: {
      includeScheduler: z.boolean().optional().describe('Include Scheduler extension dependency (default: true)'),
    },
  }, async (args) => {
    const result = tools.getMavenDependency({ includeScheduler: args.includeScheduler });
    return { content: [{ type: 'text', text: JSON.stringify(result, null, 2) }] };
  });

  // --- Resources ---

  for (const doc of data.documentation) {
    mcp.registerResource(
      doc.title,
      `fullcalendar://docs/${doc.id}`,
      { mimeType: 'text/markdown' },
      async () => ({
        contents: [{ uri: `fullcalendar://docs/${doc.id}`, mimeType: 'text/markdown', text: doc.content }],
      }),
    );
  }

  return mcp;
}

// Express server setup
const app = express();
app.use(express.json());

// Map to store transports by session ID for stateful sessions
const transports = new Map<string, StreamableHTTPServerTransport>();

// Health check (non-MCP, always available)
app.get('/health', (_req: Request, res: Response) => {
  res.json({
    status: isInitialized ? 'healthy' : 'initializing',
    version: data?.version || 'unknown',
    searchMode: search?.getSearchMode() || 'unknown',
  });
});

// Server info (non-MCP endpoint for debugging)
app.get('/info', (_req: Request, res: Response) => {
  if (!isInitialized) {
    res.status(503).json({ error: 'Server not initialized' });
    return;
  }
  res.json(tools.getServerInfo());
});

// MCP Streamable HTTP endpoint — handles POST, GET, and DELETE on the same path
app.all('/mcp', async (req: Request, res: Response) => {
  if (!isInitialized) {
    res.status(503).json({ error: 'Server not initialized' });
    return;
  }

  // Check for existing session
  const sessionId = req.headers['mcp-session-id'] as string | undefined;

  if (sessionId && transports.has(sessionId)) {
    // Reuse existing transport for this session
    const transport = transports.get(sessionId)!;
    await transport.handleRequest(req, res, req.body);
    return;
  }

  if (req.method === 'POST' && !sessionId) {
    // New session — create transport and connect a fresh McpServer
    const transport = new StreamableHTTPServerTransport({
      sessionIdGenerator: () => crypto.randomUUID(),
    });

    transport.onclose = () => {
      if (transport.sessionId) {
        transports.delete(transport.sessionId);
      }
    };

    const mcp = createMcpServer();
    await mcp.connect(transport);

    // handleRequest processes the initialize message and assigns the session ID
    await transport.handleRequest(req, res, req.body);

    // Store AFTER handleRequest — that's when sessionId gets assigned
    if (transport.sessionId) {
      transports.set(transport.sessionId, transport);
    }
    return;
  }

  // Invalid request: non-POST without session, or unknown session
  res.status(400).json({ error: 'Bad request: missing or invalid session' });
});

// Start server
async function start(): Promise<void> {
  try {
    await loadData();

    app.listen(PORT, '0.0.0.0', () => {
      console.log(`\nFullCalendar Vaadin MCP Server running on port ${PORT}`);
      console.log(`  Health check: http://localhost:${PORT}/health`);
      console.log(`  Server info:  http://localhost:${PORT}/info`);
      console.log(`  MCP endpoint: http://localhost:${PORT}/mcp`);
      console.log(`\nSearch mode: ${search.getSearchMode()}`);
      if (search.getSearchMode() === 'keyword') {
        console.log('  (Set OPENAI_API_KEY for semantic search)');
      }
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
}

start();
