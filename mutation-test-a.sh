#!/bin/bash
# Mutation Testing — Category A: E2E Test View Mutations
# Requires: Spring Boot app running in dev mode on port 8080

set -e
cd /workspace

KILLED=0
SURVIVED=0
ERRORS=0
TOTAL=0
RESULTS=""

VIEW_BASE="/workspace/e2e-test-app/src/main/java/org/vaadin/stefan/ui/view/testviews"

run_e2e_mutation() {
    local id="$1"
    local file="$2"
    local sed_cmd="$3"
    local spec="$4"
    local grep_filter="$5"
    local description="$6"

    TOTAL=$((TOTAL + 1))

    # Apply mutation
    sed -i "$sed_cmd" "$file"
    if git diff --quiet "$file"; then
        ERRORS=$((ERRORS + 1))
        RESULTS="${RESULTS}ERROR    | $id | $description (no change)\n"
        echo "  ERROR:    $id — $description (sed made no change)"
        return
    fi

    # Recompile
    if ! mvn compile -pl e2e-test-app -q 2>/dev/null; then
        git checkout -- "$file"
        ERRORS=$((ERRORS + 1))
        RESULTS="${RESULTS}ERROR    | $id | $description (compile failed)\n"
        echo "  ERROR:    $id — $description (compile failed)"
        return
    fi

    # Run spec
    local pw_args="tests/$spec"
    if [ -n "$grep_filter" ]; then
        pw_args="$pw_args --grep \"$grep_filter\""
    fi

    cd /workspace/e2e-tests
    if eval npx playwright test $pw_args 2>/dev/null | grep -q "passed"; then
        # All tests passed = mutation SURVIVED
        local passed=$(eval npx playwright test $pw_args 2>&1 | grep -oP '\d+ passed' | head -1)
        local failed=$(eval npx playwright test $pw_args 2>&1 | grep -oP '\d+ failed' | head -1)
        if echo "$failed" | grep -qP '\d+'; then
            KILLED=$((KILLED + 1))
            RESULTS="${RESULTS}KILLED   | $id | $description\n"
            echo "  KILLED:   $id — $description"
        else
            SURVIVED=$((SURVIVED + 1))
            RESULTS="${RESULTS}SURVIVED | $id | $description\n"
            echo "  SURVIVED: $id — $description"
        fi
    else
        KILLED=$((KILLED + 1))
        RESULTS="${RESULTS}KILLED   | $id | $description\n"
        echo "  KILLED:   $id — $description"
    fi
    cd /workspace

    # Revert
    git checkout -- "$file"
    mvn compile -pl e2e-test-app -q 2>/dev/null
}

# Simpler approach: just check exit code
run_mutation_simple() {
    local id="$1"
    local file="$2"
    local sed_cmd="$3"
    local spec="$4"
    local grep_filter="$5"
    local description="$6"

    TOTAL=$((TOTAL + 1))

    sed -i "$sed_cmd" "$file"
    if git diff --quiet "$file"; then
        ERRORS=$((ERRORS + 1))
        RESULTS="${RESULTS}ERROR    | $id | $description (no change)\n"
        echo "  ERROR:    $id — $description (no change)"
        return
    fi

    mvn compile -pl e2e-test-app -q 2>/dev/null || {
        git checkout -- "$file"
        ERRORS=$((ERRORS + 1))
        RESULTS="${RESULTS}ERROR    | $id | $description (compile failed)\n"
        echo "  ERROR:    $id — $description (compile failed)"
        return
    }

    local pw_cmd="npx playwright test tests/$spec"
    [ -n "$grep_filter" ] && pw_cmd="$pw_cmd --grep '$grep_filter'"

    cd /workspace/e2e-tests
    if eval $pw_cmd >/dev/null 2>&1; then
        SURVIVED=$((SURVIVED + 1))
        RESULTS="${RESULTS}SURVIVED | $id | $description\n"
        echo "  SURVIVED: $id — $description"
    else
        KILLED=$((KILLED + 1))
        RESULTS="${RESULTS}KILLED   | $id | $description\n"
        echo "  KILLED:   $id — $description"
    fi
    cd /workspace

    git checkout -- "$file"
    mvn compile -pl e2e-test-app -q 2>/dev/null
}

echo "=== Category A: E2E Mutation Testing ==="
echo ""

EPV="$VIEW_BASE/EntryPropertyTestView.java"
LDV="$VIEW_BASE/ListenerDataTestView.java"
COV="$VIEW_BASE/CalendarOptionsTestView.java"
RTV="$VIEW_BASE/RoundtripTestView.java"
ICV="$VIEW_BASE/InteractionCallbacksTestView.java"

echo "--- A1: EntryPropertyTestView ---"
run_mutation_simple "A1.1" "$EPV" 's/redEntry.setColor("red")/redEntry.setColor("blue")/' \
    "entry-properties.spec.js" "red entry" "red→blue"

run_mutation_simple "A1.2" "$EPV" 's/setBackgroundColor("#00ff00")/setBackgroundColor("#ff0000")/' \
    "entry-properties.spec.js" "green background" "green bg→red bg"

run_mutation_simple "A1.3" "$EPV" 's/setTextColor("#ffffff")/setTextColor("#000000")/' \
    "entry-properties.spec.js" "white text" "white text→black text"

run_mutation_simple "A1.4" "$EPV" 's/setBorderColor("blue")/setBorderColor("red")/' \
    "entry-properties.spec.js" "border entry" "blue border→red border"

run_mutation_simple "A1.5" "$EPV" 's/setDisplayMode(DisplayMode.BACKGROUND)/setDisplayMode(DisplayMode.AUTO)/' \
    "entry-properties.spec.js" "background display" "BACKGROUND→AUTO"

run_mutation_simple "A1.6" "$EPV" 's/Set.of("my-custom-class")/Set.of("other-class")/' \
    "entry-properties.spec.js" "custom class" "my-custom-class→other-class"

run_mutation_simple "A1.7" "$EPV" 's/setCustomProperty("department", "Engineering")/setCustomProperty("department", "Finance")/' \
    "entry-properties.spec.js" "extendedProps" "Engineering→Finance"

echo ""
echo "--- A2: ListenerDataTestView ---"
run_mutation_simple "A2.1" "$LDV" 's/calendar.addMoreLinkClickedListener/\/\/calendar.addMoreLinkClickedListener/' \
    "listener-data.spec.js" "more link" "remove MoreLinkClickedListener"

run_mutation_simple "A2.2" "$LDV" 's/calendar.addEntryClickedListener/\/\/calendar.addEntryClickedListener/' \
    "listener-data.spec.js" "clicking entry" "remove EntryClickedListener"

run_mutation_simple "A2.3" "$LDV" 's/entryClickTitle.setText(e.getEntry().getTitle())/entryClickTitle.setText("WRONG")/' \
    "listener-data.spec.js" "entry-click-title" "title→WRONG"

echo ""
echo "--- A3: CalendarOptionsTestView ---"
run_mutation_simple "A3.1" "$COV" 's/Locale.GERMAN/Locale.ENGLISH/' \
    "calendar-options.spec.js" "German" "German→English"

run_mutation_simple "A3.2" "$COV" 's/FullCalendar.Option.WEEKENDS, false/FullCalendar.Option.WEEKENDS, true/' \
    "calendar-options.spec.js" "weekends" "weekends false→true"

echo ""
echo "--- A4: RoundtripTestView ---"
run_mutation_simple "A4.1" "$RTV" 's/entry.setTitle("Renamed!")/entry.setTitle("Renamed!!")/' \
    "roundtrip.spec.js" "Rename" "Renamed!→Renamed!!"

run_mutation_simple "A4.2" "$RTV" 's/entry.setColor("red")/entry.setColor("green")/' \
    "roundtrip.spec.js" "Recolor" "red→green"

run_mutation_simple "A4.3" "$RTV" 's/provider.removeEntry(entry)/\/\/provider.removeEntry(entry)/' \
    "roundtrip.spec.js" "Remove" "disable removeEntry"

echo ""
echo "--- A5: InteractionCallbacksTestView ---"
run_mutation_simple "A5.1" "$ICV" 's/calendar.addEntryDragStartListener/\/\/calendar.addEntryDragStartListener/' \
    "interaction-callbacks.spec.js" "drag start" "remove DragStartListener"

echo ""
echo "========================================="
echo "RESULTS: $KILLED killed, $SURVIVED survived, $ERRORS errors out of $TOTAL mutations"
if [ $((KILLED + SURVIVED)) -gt 0 ]; then
    echo "Kill rate: $(( KILLED * 100 / (KILLED + SURVIVED) ))%"
fi
echo "========================================="
echo ""
printf "$RESULTS"

# Verify clean state
if [ -n "$(git diff --name-only)" ]; then
    echo "WARNING: Working tree is dirty!"
    git diff --name-only
    git checkout -- .
else
    echo "Working tree is clean."
fi
