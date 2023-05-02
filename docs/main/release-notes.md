# Index
* [4.1.x](#41x)
* [4.0.x](#40x)

# 4.1.x (in progress)
[Details](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/Release-Notes-4.1.x)
- added EntryProvider, a data provider like callback based class to allow lazy loading entries based on the actual displayed timespan

# 4.0.x
[Details](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/Release-Notes-4.0.x)
- introduced a new type JsonItem for creating item classes with dynamic property handling and automated conversion from and to json
- integrated json item api into Entry types for dynamic type conversion. Due to that entries will not send all data to the client, when updating existing ones
- changed date time handling on server side and communication to be always utc
- entries are not resent to server anymore when changing timezone on server
- entry data changes are now sent at once the the client
- client side entries ("event") have now a getCustomProperty method inside eventDidMount or eventContent callbacks
- removed official support of custom timezones for entries
- renamed several methods
- recurrence has some changes regarding enable recurrence and timezones
