# Fix 12: Check missing Option enum variants + outdated comments

## Context

PR #223 review comment by stefanuebe on `dragScrollEls` (line ~3330):
> keine Option variante hiervon?

## Analysis

`setDragScrollEls(String... cssSelectors)` joins selectors with comma and delegates to `setOption(Option.DRAG_SCROLL_ELS, ...)`. The Option enum constant `DRAG_SCROLL_ELS` already exists. So this comment may refer to a different method nearby, or is already addressed.

**Action:** Verify that `DRAG_SCROLL_ELS` exists in the Option enum. If so, this is already done.

## Additional: Fix outdated Option comment

The Option enum has this comment on `SELECT_OVERLAP`:
```java
SELECT_OVERLAP, // function not yet supported
```
But `setSelectOverlapCallback(String)` at line ~3121 DOES support function callbacks. Remove the outdated comment.

## Also check: setSelectConstraint missing BusinessHours overload

`setEventConstraint` has three overloads:
- `setEventConstraint(String groupId)`
- `setEventConstraint(BusinessHours hours)`
- `setEventConstraintToBusinessHours()`

But `setSelectConstraint` only has:
- `setSelectConstraint(String constraint)`
- `setSelectConstraintToBusinessHours()`

If `setSelectConstraint` survives Fix 1 (it has real logic for BusinessHours), add the missing `setSelectConstraint(BusinessHours)` overload for consistency. If it gets removed in Fix 1, just ensure the Option enum Javadoc mentions that BusinessHours JSON can be passed.

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java` — fix comment, possibly add overload

## Verification

1. `mvn test -pl addon`
2. `mvn clean install -DskipTests`
