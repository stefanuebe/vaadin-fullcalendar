# FullCalendar Vaadin MCP Server Dockerfile
# Build from project root: docker build -f mcp-server/Dockerfile -t fullcalendar-mcp .

# Stage 1: Build and extract
FROM node:20-alpine AS builder

WORKDIR /build

# Copy MCP server source
COPY mcp-server/package*.json ./
RUN npm ci

COPY mcp-server/tsconfig.json ./
COPY mcp-server/src ./src

# Copy project files needed for extraction
COPY docs /project/docs
COPY addon /project/addon
COPY addon-scheduler /project/addon-scheduler
COPY demo /project/demo

# Create data directory and run extraction + build
RUN mkdir -p data
ENV PROJECT_ROOT=/project
RUN npm run build

# Stage 2: Production image
FROM node:20-alpine AS production

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001

# Copy package files and install production dependencies only
COPY mcp-server/package*.json ./
RUN npm ci --omit=dev && npm cache clean --force

# Copy built files from builder
COPY --from=builder /build/dist ./dist
COPY --from=builder /build/data ./data

# Set ownership
RUN chown -R nodejs:nodejs /app

USER nodejs

# Environment variables
ENV NODE_ENV=production
ENV PORT=3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1

EXPOSE 3000

CMD ["node", "dist/index.js"]
