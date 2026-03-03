# Calendar Item Provider
## Current state
Currently the full calendar gets its data via the entry provider and entry type (and resource entry for the scheduler).

This has the downside, that any data model from the backend has to be translated into Entry instnaces first, just to be fetched by
the client. This is unnecessary overhead for the JVM and also the application development.

## Idea
The idea is to generalize the entry provider to a CalendarItemProvider. The CIP shall use a more generalized type than the Entry Provider. 

There shall be the same types and api, that are already there for Entries, e.g. the CalendarItemProvider shall provide static methods to
create new instances based on lambdas. There will be concrete implementations, that work with in memory caches, etc.

## Fetch from server / Properties mapper
The Entry class comes with a set of fields, that are converted to json in the FC fetchEntriesFromServer method. Since the CIP will allow any
type of Pojo to be used, we have to find a way to map the Pojo structure to the structure, that the client side expects.

For this, there will be a new type classed CalendarItemPropertyMapper. This class will be used to map Pojo properties to calendar item properties.
Internally the mapper will have a value provider for each expected property, that will be applied on the given pojos, for instance
```java
public class CalendarItemPropertyMapper<T> {
    private ValueProvider<T, String> titleProvider; // reads the title
}

// usage
var mapper = ... // init;

var boundMapper = mapper.forItem(helloPojo); // the pojo will produce a title "Hello"
var title = boundMapper.getTitle(); // returns "Hello";

boundMapper = mapper.forItem(worldPojo);
title = boundMapper.getTitle(); // returns "World"
```

To initialize the mapper, the class will provide a builder like api:
```java
// init via lambda / method reference
var mapper = CalendarItemPropertyMapper.of(MyPojo.class)
                .title(MyPojo::getName)
                .color(MyPojo::getCategoryColor)
                .startDate(MyPojo::from)
                .endDate(myPojo -> myPojo.from().plusHours(1));
                ...;
```

```java
// init via string prperties
var mapper = CalendarItemPropertyMapper.of(MyPojo.class)
                .title("name")
                .color("categoryColor")
                .startDate("from")
                ...;
```


## Additional Types
The entry provider uses different types of classes for events, listener, querying, etc. These need to be adapted as well
* EntryQuery -> CalendarQuery
* EntriesChangeListener/-Event -> CalendarItemsChangeListener/-Event
* EntryRefreshListener/-Event -> CalendarItemRefreshListener/-Event
* etc

## Entries and EntryProvider
With the CIP, entries and entry provider are more or less already existing implementations, that are ready-to-use, when there is
no existing backend model. To prevent any code duplicates, the current existing Entry Provider api will be mapped to the new CIP classes and api,
e.g. EntryProvider extends CalendarItemProvider<Entry>.

Entry will be kept untouched.

## FullCalendar API
### User API
CIP api, similar to the existing entry provider api will be created.
FullCalendar will be typed with a generic type <T>, that matches the CIP.
The newly created `setCalendarItemProvider` will take two parameters, 
* CalendarItemProvider
* CalendarItemProviderMapper

There will also be a variant with only the CIP as parameter, but that will check internally, if there is a mapper set. If none
is set yet, the method will throw a respective exception, that point to the other method.

### Client / internal API
Methods or fields, that are used internally (like cache or fetchFromServer) will be rewritten to use the new api.  To prevent breaking changes
with potential subclasses, the internal methods will be deprecated and use the newly created api.