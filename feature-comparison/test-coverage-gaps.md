# Test Coverage Gaps — Consolidated QA Audit (2026-03-20)

## CRITICAL

### 1. applyEntryDidMountMerge — 4 branches, 0 unit tests

**File:** `FullCalendar.java` lines 1001-1036
**Test file:** `JsCallbackTest.java`

The merge logic combines user callback + native event listeners into one eventDidMount function.
Four distinct branches, all untested:

- (a) User callback + native listeners → merged string (listener code spliced before closing `}`)
- (b) No user callback + native listeners → auto-generated `function(info) { ... }` wrapper
- (c) User callback only, no native listeners → user callback string as-is
- (d) Both cleared → sends null to client (the null-clear fix)

**Tests to write:**
```
applyEntryDidMountMerge_userCallbackOnly_sendsCallbackAsIs
applyEntryDidMountMerge_nativeListenersOnly_generatesWrapper
applyEntryDidMountMerge_userCallbackAndNativeListeners_mergesCorrectly
applyEntryDidMountMerge_bothCleared_sendsNull
```

Note: These tests need the calendar to be attached (isAttached() == true) for the merge to fire.
Pre-attach path just returns early. May need to use reflection or mock to verify the merged string
since callJsFunction is not observable in unit tests without a UI.

---

## HIGH

### 2. Entry.isRecurring() with RRule

**File:** `Entry.java` line 1132-1134 (rrule != null branch)
**Test file:** `EntryModelTest.java`

The `rrule != null` early-return was added to fix the bug where RRule entries were filtered out.
No test verifies this branch.

**Tests to write:**
```
entry_isRecurring_true_whenRRuleSet
entry_isRecurring_false_afterRRuleCleared
```

### 3. Entry.constraint per-entry serialization

**File:** `Entry.java` — setConstraint(String), setConstraint(BusinessHours), setConstraintToBusinessHours()
**Test file:** `EntryModelTest.java`

Per-entry constraint has zero serialization tests.

**Tests to write:**
```
constraint_default_notInJson
constraint_string_serializedToJson
constraint_businessHours_serializedToJson
constraint_setToBusinessHours_serializedAsString
constraint_null_notInJson
```

### 4. ResourceAreaColumn cell-level render hooks

**File:** `ResourceAreaColumn.java` lines 53-56, toJson() lines 330-333
**Test file:** `SchedulerFeaturesTest.java`

Header hooks are tested; cell hooks have zero coverage.

**Tests to write:**
```
resourceAreaColumn_cellContent_string_serialized
resourceAreaColumn_cellContent_jsCallback_serializedAsMarker
resourceAreaColumn_cellClassNames_string_serialized
resourceAreaColumn_cellClassNames_jsCallback_serializedAsMarker
resourceAreaColumn_cellDidMount_serializedAsMarker
resourceAreaColumn_cellWillUnmount_serializedAsMarker
resourceAreaColumn_cellHooks_defaultAbsent
```

### 5. callOptionUpdate JsCallback → marker conversion + round-trip

**File:** `FullCalendar.java` callOptionUpdate lines 731-738
**Test file:** `JsCallbackTest.java`

The core mechanism: JsCallback is converted to marker JSON for the client,
original JsCallback preserved in serverSideOptions for getOption() round-trip.
Only tested via assertDoesNotThrow.

**Tests to write:**
```
setOption_withJsCallback_getOptionReturnsJsCallback
setOption_withJsCallback_stringKey_getOptionReturnsJsCallback
```

Note: getOption uses serverSideOptions fallback. Verify getOption(Option.X) returns
the original JsCallback, not the ObjectNode marker.

### 6. Converter classes — isolated tests

**Files:** `addon/src/main/java/org/vaadin/stefan/fullcalendar/converters/`
**Test file:** NEW — `ConverterTest.java`

No converter has isolated unit tests. Some are exercised indirectly through setOption.

**Tests to write:**
```
DurationConverter:
  duration_ofHours_convertsToString         // Duration.ofHours(2) → "02:00:00"
  duration_localTime_convertsToString       // LocalTime.of(9,30) → "09:30:00"
  duration_null_returnsNull

DayOfWeekArrayConverter:
  dayOfWeekArray_convertsToIntArray         // [MONDAY, FRIDAY] → [1, 5]
  dayOfWeekArray_empty_convertsToEmptyArray

ToolbarConverter:
  toolbar_header_convertsToJson             // Header → {left, center, right}
  toolbar_map_convertsToJson                // Map → {left, center, right}

StringArrayConverter:
  stringArray_convertsToJsonArray           // String[] → JsonArray
  stringArray_collection_convertsToJsonArray
```

---

## MEDIUM

### 7. Option/SchedulerOption callback key verification (parametrized)

**File:** Option enum, SchedulerOption enum
**Test file:** `FullCalendarOptionsTest.java` (extend existing)

43 of 47 callback Option constants and 12 of 23 SchedulerOption callback constants
have no test verifying their getOptionKey() value. A single parametrized test
would cover all.

**Tests to write:**
```
allCallbackOptionKeys_areCorrectCamelCase   // parametrized over all callback constants
allSchedulerCallbackOptionKeys_areCorrectCamelCase
```

### 8. Deprecated wrapper methods — delegation tests

**File:** `FullCalendar.java` — setEntryClassNamesCallback, setEntryContentCallback, setEntryWillUnmountCallback
**Test file:** `JsCallbackTest.java`

Only setEntryDidMountCallback is tested. The other 3 are one-line delegations
to setOption(Option.X, JsCallback.of(s)) — worth a basic regression test.

**Tests to write:**
```
setEntryClassNamesCallback_delegatesToSetOption
setEntryContentCallback_delegatesToSetOption
setEntryWillUnmountCallback_delegatesToSetOption
```

### 9. SchedulerOption RESOURCE_LABEL/LANE smoke tests

**File:** `FullCalendarScheduler.java`
**Test file:** `SchedulerFeaturesTest.java`

RESOURCE_GROUP_* and RESOURCE_AREA_HEADER_* have assertDoesNotThrow tests.
RESOURCE_LABEL_* and RESOURCE_LANE_* do not.

**Tests to write:**
```
setOption_resourceLabelClassNames_doesNotThrow
setOption_resourceLabelContent_doesNotThrow
setOption_resourceLabelDidMount_doesNotThrow
setOption_resourceLabelWillUnmount_doesNotThrow
setOption_resourceLaneClassNames_doesNotThrow
setOption_resourceLaneContent_doesNotThrow
setOption_resourceLaneDidMount_doesNotThrow
setOption_resourceLaneWillUnmount_doesNotThrow
setOption_resourceGroupLaneClassNames_doesNotThrow
setOption_resourceGroupLaneContent_doesNotThrow
setOption_resourceGroupLaneDidMount_doesNotThrow
setOption_resourceGroupLaneWillUnmount_doesNotThrow
```
