Q: The Calendar is not fully recognized / I get JavaScript errors like "setOption is not a function" or "addEvents is not a function".

A: This may hapen, when not including the addon packages into Vaadin's whitelisting. Please check, if your 'application.properties' contain the property "vaadin.whitelist" and if it does, if the whitelist includes "org.vaadin.stefan".
```
vaadin.whitelisted-packages=com.vaadin,org.vaadin.stefan,some.other.addon,etc.etc.etc
```

Please also see [Build problems / JS (client side) errors with V14+](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/Known-Issues#build-problems--js-client-side-errors-v14) for more details. If the issue still occurs, please [check](https://github.com/stefanuebe/vaadin_fullcalendar/issues/), if there might be an open issue already or create a new one.

Q: The `DatesRenderedEvent` is not fired when setting an option, that changes the view.

A: I deactivated the forwarding of the datesRendered event from the client side when an option is set, since
that would lead otherwise to a huge amount of datesRendered events. When setting options before the client side
is fully attached, the queueing messes up the event handling here.

When needed, you can activate or deactivate that by using the method `allowDatesRenderEventOnOptionChange(boolean)`.
By default this value is `false`, simply set it to true to also receive date render events on setOption.
