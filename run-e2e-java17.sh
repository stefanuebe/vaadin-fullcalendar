#!/usr/bin/env bash
# Runs E2E tests (mvn clean verify -Pit) using Java 17
# Usage: ./run-e2e-java17.sh

set -euo pipefail

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java: $(java -version 2>&1 | head -1)"

cd "$(dirname "$0")/e2e-test-app"
mvn clean verify -Pit
