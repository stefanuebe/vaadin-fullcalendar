#!/bin/bash
# Mutation Testing — Category B: Production Code (Unit Tests)
# Improved version with better sed patterns for Lombok-based code

set -e
cd /workspace

KILLED=0
SURVIVED=0
ERRORS=0
TOTAL=0
RESULTS=""

run_mutation() {
    local id="$1"
    local file="$2"
    local sed_cmd="$3"
    local test_class="$4"
    local description="$5"

    TOTAL=$((TOTAL + 1))

    # Apply mutation
    if ! sed -i "$sed_cmd" "$file"; then
        ERRORS=$((ERRORS + 1))
        RESULTS="${RESULTS}ERROR    | $id | $description (sed failed)\n"
        echo "  ERROR:    $id — $description (sed failed)"
        return
    fi

    # Check if mutation actually changed the file
    if git diff --quiet "$file"; then
        ERRORS=$((ERRORS + 1))
        RESULTS="${RESULTS}ERROR    | $id | $description (no change)\n"
        echo "  ERROR:    $id — $description (sed made no change)"
        return
    fi

    # Run test (suppress output)
    if mvn test -pl addon -Dtest="$test_class" -q 2>/dev/null; then
        SURVIVED=$((SURVIVED + 1))
        RESULTS="${RESULTS}SURVIVED | $id | $description\n"
        echo "  SURVIVED: $id — $description"
    else
        KILLED=$((KILLED + 1))
        RESULTS="${RESULTS}KILLED   | $id | $description\n"
        echo "  KILLED:   $id — $description"
    fi

    # Revert
    git checkout -- "$file"
}

echo "=== Category B: Unit Test Mutation Testing (Improved) ==="
echo ""

ENTRY="addon/src/main/java/org/vaadin/stefan/fullcalendar/Entry.java"
RRULE="addon/src/main/java/org/vaadin/stefan/fullcalendar/RRule.java"
BH="addon/src/main/java/org/vaadin/stefan/fullcalendar/BusinessHours.java"
FC="addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java"
DM="addon/src/main/java/org/vaadin/stefan/fullcalendar/DisplayMode.java"
PROVIDER="addon/src/main/java/org/vaadin/stefan/fullcalendar/dataprovider/InMemoryEntryProvider.java"

echo "--- B1: Entry.java field mutations ---"
run_mutation "B1.1" "$ENTRY" 's/private boolean editable = true;/private boolean editable = false;/' \
    "EntryTest" "editable default true→false"

run_mutation "B1.2" "$ENTRY" 's/@JsonName("display")/@JsonName("displayXX")/' \
    "EntryModelTest" "@JsonName display→displayXX"

run_mutation "B1.3" "$ENTRY" 's/@JsonName("duration")/@JsonName("durationXX")/' \
    "EntryModelTest#recurringDuration_serializedAsDuration" "@JsonName duration→durationXX"

run_mutation "B1.4" "$ENTRY" 's/private Boolean overlap;/private Boolean overlap = Boolean.TRUE;/' \
    "EntryModelTest#overlap_default_null_notInJson" "overlap default null→TRUE"

run_mutation "B1.5" "$ENTRY" 's/private DisplayMode displayMode = DisplayMode.AUTO;/private DisplayMode displayMode = DisplayMode.NONE;/' \
    "EntryTest#testToJson" "displayMode default AUTO→NONE"

echo ""
echo "--- B2: RRule.java mutations ---"
run_mutation "B2.1" "$RRULE" 's/append("INTERVAL=").append(interval)/append("INTERVAL=").append(interval + 1)/' \
    "EntryModelTest#rrule_interval_inJson" "INTERVAL off-by-one"

run_mutation "B2.2" "$RRULE" 's/case MONDAY -> "MO"/case MONDAY -> "TU"/' \
    "EntryModelTest#rrule_byWeekday_DayOfWeek_convertedToAbbreviation" "MONDAY→TU abbreviation"

run_mutation "B2.3" "$RRULE" 's/case MONTHLY -> "MONTHLY"/case MONTHLY -> "WEEKLY"/' \
    "EntryModelTest#rrule_factoryMethods_produceCorrectFrequency" "MONTHLY freq→WEEKLY"

echo ""
echo "--- B3: BusinessHours.java mutations ---"
run_mutation "B3.1" "$BH" 's/startTime/startXX/' \
    "BusinessHoursTest#testToJson" "JSON key startTime→startXX"

run_mutation "B3.2" "$BH" 's/endTime/endXX/' \
    "BusinessHoursTest#testToJson" "JSON key endTime→endXX"

echo ""
echo "--- B4: DisplayMode.java mutations ---"
run_mutation "B4.1" "$DM" 's/BACKGROUND("background")/BACKGROUND("foreground")/' \
    "EntryTest#testToJson" "BACKGROUND value→foreground"

echo ""
echo "--- B5: FullCalendar.java mutations ---"
run_mutation "B5.1" "$FC" 's/options.remove(option);/\/\/ options.remove(option);/' \
    "FullCalendarTest#testGetAndSetOption" "don't remove option on null"

run_mutation "B5.2" "$FC" 's/serverSideOptions.remove(option);/\/\/ serverSideOptions.remove(option);/' \
    "FullCalendarTest#testGetAndSetOptionWithServerSideValues" "don't remove serverSide option on null"

echo ""
echo "--- B6: InMemoryEntryProvider.java mutations ---"
run_mutation "B6.1" "$PROVIDER" 's/entries.put(entry.getId(), entry);/entries.putIfAbsent(entry.getId(), entry);/' \
    "InMemoryEntryProviderTest" "put→putIfAbsent (no update on re-add)"

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
    echo ""
    echo "WARNING: Working tree is dirty after mutations!"
    git diff --name-only
    git checkout -- .
else
    echo ""
    echo "Working tree is clean."
fi
