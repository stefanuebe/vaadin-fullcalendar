# 4.0.x
- introduced a new type JsonItem for creating item classes with dynamic property handling and automated conversion from and to json
- integrated json item api into Entry types for dynamic type conversion
- changed entries do not send whole data to the client, but changed only
- changed date time handling on server side and communication to be always utc based to increase performance on massive entry count (breaking change)
- related so that overhauled date time api to be utc bases by default (breaking change)
- removed the possibility to have different timezones for entry start/end (breaking change) - might be re-introduced at a later point, but needs reimplementation due to change of internal time management.
- entries are not resend to server anymore when changing timezone on server
- client side entries ("event") have now a getCusromProperty method inside eventDidMount or eventContent callbacks
