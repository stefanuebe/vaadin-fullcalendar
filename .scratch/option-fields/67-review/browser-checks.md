# Browser checks of ticket 67 (2026-10-17)

Run against `e2e-test-app` (library 7.1.0), Chromium, by calling the library's `calendar.setOption` on the page.
These settle the questions the reports and verifiers left open.

- **allDayText with a newline:** a line break. `"Alpha\nBeta"` renders on two lines, in the Vaadin and the classic
  theme; the text sits in an element with `white-space: pre`. The wave-2 example in ticket 67 ("a newline in
  `allDayText` is no line break") is wrong; the old Javadoc was right.
- **listDayFormat / listDayAltFormat `false`:** no effect. The list day header shows the same default text as without
  the option. `ListDayFormatValue.NONE` is removed (decision D5).
- **hiddenDays with all seven days:** the library throws `invalid hiddenDays`. The setter rejects it (D6).
- **WillUnmount fields, callback changed after elements were rendered:** the NEW callback runs when those elements
  are removed (`eventWillUnmount`, `moreLinkWillUnmount` checked). So the "only newly rendered elements" paragraph
  is wrong for WillUnmount fields: it applies to DidMount fields only. A WillUnmount field says instead that the
  callback set at the time an element is removed runs for it.
- **entryDidMount with dayMaxEntries 1 and two entries on a day:** runs only for the entry that is rendered, not for
  the one behind the "+1 more" link (until the popover renders it).
- **unselectCancel `""`:** no error; an empty string acts like no value. No finding.
- **refetchResourcesOnNavigate:** with a resources function, the function runs again on navigation and gets the
  range. The resources the add-on manages are sent as a fixed list, for which the option has no effect.
