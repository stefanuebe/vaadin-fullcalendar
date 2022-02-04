# 4.0.x
- introduced a new type JsonItem for creating item classes with dynamic property handling and automated conversion from and to json
- integrated json item api into Entry types for dynamic type conversion. Due to that entries will not send all data to the client, when updating existing ones
- changed date time handling on server side and communication to be always utc 
- entries are not resent to server anymore when changing timezone on server 
- client side entries ("event") have now a getCustomProperty method inside eventDidMount or eventContent callbacks
- removed feature of custom timezones for entries (for now, might be reintegrated later)
- renamed several methods
- recurrence has some changes regarding enable recurrence and timezones