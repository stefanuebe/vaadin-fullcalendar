This page gives you an overview of the major changes, that came with the release of [FullCalendar for Flow, version 4.1](https://vaadin.com/directory/component/full-calendar-flow).

Please also have a look on our [migration guide](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/FullCalendar-MigrationGuides#migrating-from-40--41) and our [examples](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/FullCalendar-Examples).

## Entry Provider
With 4.1 we introduced a new way of providing calendar items. Up to now the official supported way was to register all known entries to the server side, which itself would push it then to the client. This means that both, the server and the client, needed to keep all the data in memory.

With the newly introduces `EntryProvider` we provide a way of providing entries similar to the common Vaadin `DataProvider`. When using an `EntryProvider`, the client will control via parameters, which entries are to be fetched to the client. The server on the otherside only needs to create and provide the entries, that are requested.

### In memory variants
Of course, it is up to the server side, how and when the entries are created. Beside the base interface we also introduced the sub interface `InMemoryEntryProvider`, which comes in two pre-implemented variants
* EagerInMemoryEntryProvider
* LazyInMemoryEntryProvider

The eager variant is an exception to the above described behavior of the `EntryProvider`. While it implements the `EntryProvider` interface, it behaves like the old way the FullCalendar transported entries to the client: it pushes them itself. So if you still want to have all entries on the client available at any time (e. g. when your application operats in a scope with a slow network access), you can use this instance.

The lazy variant is a mixture between the old and the new way. It acts a bit like the common `ListDataProvider`, where all known entries are cached on the server side, but only the necessary ones are fetched from the client.

### Initial EntryProvider on FullCalendar
With this update the FullCalendar itself does not cache any entries directly, but uses an `EntryProvider` for all cases. A new FullCalendar is initialized with an instance of the `EagerInMemoryEntryProvider`. This shall prevent any breaking changes for updating applications. Yet we recommend to change to a different `EntryProvider` due to the advantages of fetching only the necessary entries.

### Entry CRUD operations on FullCalendar
All entry CRUD operations (e. g. `addEntries()`) are therefore delegated to the calendar's provider, when that provider supports those methods. For that, the provider has to be at least an `InMemoryEntryProvider`. If you want to use the `update` api, it needs to be the `EagerInMemoryEntryProvider`, since for the lazy variant the update api is not necessary any more as updating the client side is handled by the `refresh` api.

Be aware, that any calls to the CRUD operations will lead to an exception, if you don't use the correct data provider.

### Callback Entry Provider
For easy integration of using a callback based variant we introduced the `CallbackEntryProvider`. You can simply create an instance by calling `EntryProvider.fromCallbacks()`.

### Custom implemenation
If none of the predefined variants fits your needs, you are of course free to implement your own version. In that case we recommend to extend the `AbstractEntryProvider` as a starting point.

## Info on missing offset api for recurrence
With 4.0.x the date time api for entries has changed. You might have seen, that the recurring start / end time variants do not provide an offset variant. This is due to an [issue in the native FullCalendar library](https://github.com/fullcalendar/fullcalendar/issues/5273). As soon as the issue has been fixed, we try to add the respective api.

## Deprecation
Some methods have become deprecated, especially the CRUD operations in the `FullCalendar`. We recommend to replace them with the respective replacements, since they might be removed with the next major release.


 