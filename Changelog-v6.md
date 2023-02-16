* FC6
* Lit 2
* light dom styling (interferes a bit with lumo styles and therefore needs some additional css), but should make life easier for devs to integrate custom styles
* client side uses lazy loading / fetching only (not yet fully integrated)
* usage of "private" and "protected" modifiers, removed underscore. Any remaining underscores are used for fields, that are masked by get/set
* Everything, that was marked deprecated in the previous version has been removed.

Minor, but important
* getResources now may return null. Use getOrCreateResources. Has been aligned to other namings in Entry.