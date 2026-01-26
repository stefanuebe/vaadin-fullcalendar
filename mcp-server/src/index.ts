import express, { Request, Response } from 'express';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';
import { HybridSearch } from './search/index.js';
import { ToolHandlers } from './tools/index.js';
import type { ExtractedData } from './types.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = parseInt(process.env.PORT || '3000', 10);
const DATA_PATH = process.env.DATA_PATH || path.join(__dirname, '../data/extracted.json');

// MCP Protocol Types
interface MCPRequest {
  jsonrpc: '2.0';
  id: string | number;
  method: string;
  params?: Record<string, unknown>;
}

interface MCPResponse {
  jsonrpc: '2.0';
  id: string | number;
  result?: unknown;
  error?: {
    code: number;
    message: string;
    data?: unknown;
  };
}

interface MCPTool {
  name: string;
  description: string;
  inputSchema: {
    type: 'object';
    properties: Record<string, unknown>;
    required?: string[];
  };
}

// Tool definitions for MCP
const TOOLS: MCPTool[] = [
  {
    name: 'search_docs',
    description: 'Search across FullCalendar Vaadin documentation, API reference, and code examples. Supports semantic search (with OpenAI) and keyword search.',
    inputSchema: {
      type: 'object',
      properties: {
        query: { type: 'string', description: 'Search query' },
        limit: { type: 'number', description: 'Maximum results (default: 10)' },
        mode: { type: 'string', enum: ['auto', 'semantic', 'keyword'], description: 'Search mode (default: auto)' },
      },
      required: ['query'],
    },
  },
  {
    name: 'get_api_reference',
    description: 'Get detailed API reference for a specific Java class including methods, fields, and constructors.',
    inputSchema: {
      type: 'object',
      properties: {
        className: { type: 'string', description: 'Class name (e.g., "FullCalendar", "Entry", "Resource")' },
      },
      required: ['className'],
    },
  },
  {
    name: 'list_classes',
    description: 'List all available Java classes in the FullCalendar addon.',
    inputSchema: {
      type: 'object',
      properties: {
        filter: { type: 'string', description: 'Filter by name or description' },
        source: { type: 'string', enum: ['addon', 'addon-scheduler'], description: 'Filter by module' },
      },
    },
  },
  {
    name: 'get_code_example',
    description: 'Get code examples for specific use cases.',
    inputSchema: {
      type: 'object',
      properties: {
        id: { type: 'string', description: 'Specific example ID' },
        search: { type: 'string', description: 'Search term to find examples' },
        category: { type: 'string', description: 'Category filter (e.g., "data-provider", "events", "scheduler")' },
      },
    },
  },
  {
    name: 'get_entry_schema',
    description: 'Get the Entry model schema with all properties, types, and descriptions.',
    inputSchema: {
      type: 'object',
      properties: {},
    },
  },
  {
    name: 'get_resource_schema',
    description: 'Get the Resource model schema (Scheduler extension) with all properties.',
    inputSchema: {
      type: 'object',
      properties: {},
    },
  },
  {
    name: 'list_calendar_views',
    description: 'List all available calendar views (DayGrid, TimeGrid, List, Timeline, etc.).',
    inputSchema: {
      type: 'object',
      properties: {
        includeScheduler: { type: 'boolean', description: 'Include Scheduler-specific views (default: true)' },
      },
    },
  },
  {
    name: 'list_event_types',
    description: 'List all server-side event types that can be listened to.',
    inputSchema: {
      type: 'object',
      properties: {
        source: { type: 'string', enum: ['core', 'scheduler'], description: 'Filter by module' },
      },
    },
  },
  {
    name: 'get_migration_guide',
    description: 'Get migration guide for upgrading between versions.',
    inputSchema: {
      type: 'object',
      properties: {
        fromVersion: { type: 'string', description: 'Source version (e.g., "6")' },
        toVersion: { type: 'string', description: 'Target version (e.g., "7")' },
      },
    },
  },
  {
    name: 'get_documentation',
    description: 'Get full documentation page by path.',
    inputSchema: {
      type: 'object',
      properties: {
        path: { type: 'string', description: 'Documentation path (e.g., "Samples.md", "FAQ.md")' },
      },
      required: ['path'],
    },
  },
  {
    name: 'get_maven_dependency',
    description: 'Get Maven dependency coordinates, repository configuration, and version compatibility for setting up a new project with FullCalendar for Vaadin Flow.',
    inputSchema: {
      type: 'object',
      properties: {
        includeScheduler: { type: 'boolean', description: 'Include Scheduler extension dependency (default: true)' },
      },
    },
  },
];

// Server state
let data: ExtractedData;
let search: HybridSearch;
let tools: ToolHandlers;
let isInitialized = false;

async function initialize(): Promise<void> {
  console.log('Initializing MCP server...');
  console.log(`Loading data from: ${DATA_PATH}`);

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
  console.log('MCP server initialized successfully');
}

function createMCPResponse(id: string | number, result: unknown): MCPResponse {
  return {
    jsonrpc: '2.0',
    id,
    result,
  };
}

function createMCPError(id: string | number, code: number, message: string, errorData?: unknown): MCPResponse {
  return {
    jsonrpc: '2.0',
    id,
    error: {
      code,
      message,
      data: errorData,
    },
  };
}

async function handleMCPRequest(request: MCPRequest): Promise<MCPResponse> {
  const { id, method, params } = request;

  try {
    switch (method) {
      case 'initialize':
        return createMCPResponse(id, {
          protocolVersion: '2024-11-05',
          capabilities: {
            tools: {},
          },
          serverInfo: {
            name: 'fullcalendar-vaadin-mcp',
            version: data.version,
          },
        });

      case 'tools/list':
        return createMCPResponse(id, { tools: TOOLS });

      case 'tools/call': {
        const toolName = params?.name as string;
        const toolArgs = (params?.arguments || {}) as Record<string, unknown>;

        const result = await executeToolCall(toolName, toolArgs);
        return createMCPResponse(id, {
          content: [
            {
              type: 'text',
              text: typeof result === 'string' ? result : JSON.stringify(result, null, 2),
            },
          ],
        });
      }

      case 'resources/list':
        return createMCPResponse(id, {
          resources: data.documentation.map(doc => ({
            uri: `fullcalendar://docs/${doc.id}`,
            name: doc.title,
            mimeType: 'text/markdown',
          })),
        });

      case 'resources/read': {
        const uri = params?.uri as string;
        const docId = uri.replace('fullcalendar://docs/', '');
        const doc = data.documentation.find(d => d.id === docId);

        if (!doc) {
          return createMCPError(id, -32602, `Resource not found: ${uri}`);
        }

        return createMCPResponse(id, {
          contents: [
            {
              uri,
              mimeType: 'text/markdown',
              text: doc.content,
            },
          ],
        });
      }

      default:
        return createMCPError(id, -32601, `Method not found: ${method}`);
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unknown error';
    return createMCPError(id, -32603, message);
  }
}

async function executeToolCall(toolName: string, args: Record<string, unknown>): Promise<unknown> {
  switch (toolName) {
    case 'search_docs':
      return tools.searchDocs(args as { query: string; limit?: number; mode?: 'auto' | 'semantic' | 'keyword' });

    case 'get_api_reference':
      return tools.getApiReference(args as { className: string });

    case 'list_classes':
      return tools.listClasses(args as { filter?: string; source?: 'addon' | 'addon-scheduler' });

    case 'get_code_example':
      return tools.getCodeExample(args as { id?: string; search?: string; category?: string });

    case 'get_entry_schema':
      return tools.getEntrySchema();

    case 'get_resource_schema':
      return tools.getResourceSchema();

    case 'list_calendar_views':
      return tools.listCalendarViews(args as { includeScheduler?: boolean });

    case 'list_event_types':
      return tools.listEventTypes(args as { source?: 'core' | 'scheduler' });

    case 'get_migration_guide':
      return tools.getMigrationGuide(args as { fromVersion?: string; toVersion?: string });

    case 'get_documentation':
      return tools.getDocumentation(args as { path: string });

    case 'get_maven_dependency':
      return tools.getMavenDependency(args as { includeScheduler?: boolean });

    default:
      throw new Error(`Unknown tool: ${toolName}`);
  }
}

// Express server setup
const app = express();
app.use(express.json());

// Health check
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

// MCP endpoint - handles JSON-RPC requests
app.post('/mcp', async (req: Request, res: Response) => {
  if (!isInitialized) {
    res.status(503).json(createMCPError(0, -32603, 'Server not initialized'));
    return;
  }

  const request = req.body as MCPRequest;

  if (!request.jsonrpc || request.jsonrpc !== '2.0') {
    res.status(400).json(createMCPError(request.id || 0, -32600, 'Invalid JSON-RPC version'));
    return;
  }

  const response = await handleMCPRequest(request);
  res.json(response);
});

// Also support POST to root for compatibility
app.post('/', async (req: Request, res: Response) => {
  if (!isInitialized) {
    res.status(503).json(createMCPError(0, -32603, 'Server not initialized'));
    return;
  }

  const request = req.body as MCPRequest;

  if (!request.jsonrpc || request.jsonrpc !== '2.0') {
    res.status(400).json(createMCPError(request.id || 0, -32600, 'Invalid JSON-RPC version'));
    return;
  }

  const response = await handleMCPRequest(request);
  res.json(response);
});

// SSE endpoint for MCP streaming (if needed in future)
app.get('/sse', (_req: Request, res: Response) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');

  res.write(`data: ${JSON.stringify({ type: 'ready', version: data?.version })}\n\n`);

  // Keep connection alive
  const keepAlive = setInterval(() => {
    res.write(': keepalive\n\n');
  }, 30000);

  _req.on('close', () => {
    clearInterval(keepAlive);
  });
});

// Start server
async function start(): Promise<void> {
  try {
    await initialize();

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
