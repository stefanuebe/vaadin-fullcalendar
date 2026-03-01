#!/bin/bash
# Cleanup script for Claude DevContainer sessions
# Removes core dumps, JVM artifacts, Playwright leftovers, and hanging processes.
# Safe to run at any time — never touches source code, shell scripts, or Docker containers.

WORKSPACE="$(cd "$(dirname "$0")/../.." && pwd)"

# --- Core dumps ---
CORES=$(find "$WORKSPACE" -maxdepth 1 -name "core.*" -type f 2>/dev/null)
if [ -n "$CORES" ]; then
    COUNT=$(echo "$CORES" | wc -l)
    rm -f "$WORKSPACE"/core.*
    echo "Deleted $COUNT core dump(s)"
else
    echo "No core dumps found"
fi

# --- JVM attach files ---
ATTACH=$(find "$WORKSPACE" -maxdepth 1 -name ".attach_pid*" -type f 2>/dev/null)
if [ -n "$ATTACH" ]; then
    COUNT=$(echo "$ATTACH" | wc -l)
    rm -f "$WORKSPACE"/.attach_pid*
    echo "Deleted $COUNT JVM attach file(s)"
else
    echo "No JVM attach files found"
fi

# --- Playwright screenshots ---
SCREENSHOT_DIR="$WORKSPACE/.claude/screenshots"
if [ -d "$SCREENSHOT_DIR" ]; then
    SCREENSHOTS=$(find "$SCREENSHOT_DIR" -type f 2>/dev/null)
    if [ -n "$SCREENSHOTS" ]; then
        COUNT=$(echo "$SCREENSHOTS" | wc -l)
        rm -f "$SCREENSHOT_DIR"/*
        echo "Deleted $COUNT screenshot(s) from .claude/screenshots/"
    else
        echo "No screenshots found"
    fi
else
    echo "No screenshots directory found"
fi

# --- Playwright temp dirs ---
PW_TEMPS=$(find /tmp -maxdepth 1 -name "playwright-*" -type d 2>/dev/null)
if [ -n "$PW_TEMPS" ]; then
    COUNT=$(echo "$PW_TEMPS" | wc -l)
    rm -rf /tmp/playwright-*
    echo "Deleted $COUNT Playwright temp dir(s)"
else
    echo "No Playwright temp dirs found"
fi

# --- Stale server log ---
if [ -f /tmp/claude-server.log ] && [ ! -f /tmp/claude-server.pid ]; then
    rm -f /tmp/claude-server.log
    echo "Deleted stale server log"
fi

# --- Hanging Chromium/Playwright processes ---
CHROMIUM_PIDS=$(pgrep -f "chromium|playwright" 2>/dev/null)
if [ -n "$CHROMIUM_PIDS" ]; then
    COUNT=$(echo "$CHROMIUM_PIDS" | wc -l)
    kill $CHROMIUM_PIDS 2>/dev/null
    sleep 1
    # Force-kill survivors
    SURVIVORS=$(pgrep -f "chromium|playwright" 2>/dev/null)
    if [ -n "$SURVIVORS" ]; then
        kill -9 $SURVIVORS 2>/dev/null
    fi
    echo "Killed $COUNT browser process(es)"
else
    echo "No hanging browser processes"
fi

echo "--- Cleanup complete ---"
