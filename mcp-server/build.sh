#!/bin/bash
# Build script for FullCalendar Vaadin MCP Server
# Run from project root or mcp-server directory

set -e

# Determine project root
if [ -f "package.json" ] && [ -d "src" ]; then
    # We're in mcp-server directory
    PROJECT_ROOT=".."
    MCP_DIR="."
else
    # We're in project root
    PROJECT_ROOT="."
    MCP_DIR="mcp-server"
fi

echo "Building FullCalendar Vaadin MCP Server..."
echo "Project root: $PROJECT_ROOT"

# Build Docker image from project root
cd "$PROJECT_ROOT"
docker build -f mcp-server/Dockerfile -t fullcalendar-mcp .

echo ""
echo "Build complete!"
echo ""
echo "Run the server:"
echo "  docker run -p 3000:3000 fullcalendar-mcp"
echo ""
echo "With semantic search (requires OpenAI API key):"
echo "  docker run -p 3000:3000 -e OPENAI_API_KEY=sk-... fullcalendar-mcp"
echo ""
echo "Test the server:"
echo "  curl http://localhost:3000/health"
echo "  curl http://localhost:3000/info"
