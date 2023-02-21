* FC6
* Lit 2
* light dom styling (interferes a bit with lumo styles and therefore needs some additional css), but should make life easier for devs to integrate custom styles
* client side uses lazy loading / fetching only (not yet fully integrated)
* usage of "private" and "protected" modifiers, removed underscore. Any remaining underscores are used for fields, that are masked by get/set
* Everything, that was marked deprecated in the previous version has been removed.

Minor, but important
* getResources now may return null. Use getOrCreateResources. Has been aligned to other namings in Entry.
* CalendarLocale is now an enum. Use getLocale() to obtain the contained locale value.
* week numbers within days is no longer available, weeknumbers are now always display inside days. simply remove
* RenderingMode and alike namings have been named to DisplayMode / display to match the FC library naming. Also DisplayMode is now a top level class.
