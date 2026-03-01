#!/bin/bash
curl -s -d "${1:-Aufmerksamkeit nötig}" ntfy.sh/claude-notify-a8f3x9k2m7b44o3goihefk2r9zweflihwei2ro8zwef > /dev/null
echo "$(date): notify called with: $1" >> /tmp/hook-debug.log
