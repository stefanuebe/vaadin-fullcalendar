* FC6
* Removed Polymer
* TS
* folder structure changed (important for subclasses / extensions)
* element tags changed, prefixed with "vaadin-" now
* light dom styling (interferes a bit with lumo styles and therefore needs some additional css), but should make life 
  easier for devs to integrate custom styles. also addCustomStyles or shadow dom styling has been removed. 
* client side uses lazy loading / fetching only 
* usage of "private" and "protected" modifiers, removed underscore. Any remaining underscores are used for fields, that are masked by get/set
* Everything, that was marked deprecated in the previous version has been removed.
* Entry is now back again a "normal" Java class, as the usage of JsonItem has led to issues with proxy classes. 
  But to prevent unecessary manual conversion, annotations have been introduced to annotate fields regarding their purpose 

Minor, but important
* getResources now may return null. Use getOrCreateResources. Has been aligned to other namings in Entry.
* CalendarLocale is now an enum. Use getLocale() to obtain the contained locale value.
* week numbers within days is no longer available, weeknumbers are now always display inside days. simply remove
* RenderingMode and alike namings have been named to DisplayMode / display to match the FC library naming. Also DisplayMode is now a top level class.
* added resize observer
* setHeight has been minimalized to be more aligned with Vaadin standards. FC internal height settings / options are not 
  supported anymore. Calendar content will take only as much space as needed.
 