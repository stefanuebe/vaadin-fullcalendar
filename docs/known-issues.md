# Startup issue when using webpack (Vaadin 14-23)
You may encounter a TypeScript compilation issue, when using the addon in Vaadin 14 (and later versions with webpack), e.g.
```
ERROR in ../node_modules/@vaadin/flow-frontend/vaadin-full-calendar/full-calendar.ts?babel-target=es6
Module build failed (from ../node_modules/.pnpm/ts-loader@8.0.12_typescript@4.0.3+webpack@4.42.0/node_modules/ts-loader/index.js):
Error: TypeScript emitted no output for ...
```

This seems to be a webpack issue, that we cannot fix. 

To overcome this issue, simply modify your tsconfig.json and add the following line to the "include" array
` "node_modules/@vaadin/flow-frontend/vaadin-full-calendar/*.ts"`, so that it looks something like this

```json
  // ... other things
  "include": [
    "frontend/**/*.ts",
    "types.d.ts",
    "node_modules/@vaadin/flow-frontend/vaadin-full-calendar/*.ts"
  ],
  // ... other things
```

For details see: https://github.com/vaadin/flow/issues/11976


# Multiline events drag & drop cause the scrollbar to lost position
The problem appear when you remove the events and add them back, when you remove all the events from the calendar it resize it-self. When the calendar has no events, it is much shorter which explains why the scroll position is changing.

Take a look here  for a workaround https://github.com/stefanuebe/vaadin_fullcalendar/issues/76

Another good way to fix it is to provide the resources/events as JSON feed and refetch the events instead of removing and adding them back

# Calendar crashes when clicking (V14+)
For some, currently unknown reason, sizing a calendar after the view has changed manually on a newly created calendar lets the calendar crash, when clicking inside somewhere. I have no idea, why that is so. Please see https://github.com/stefanuebe/vaadin_fullcalendar/issues/45 for details and progress.

# Build problems / JS (client side) errors (V14+)
It might be, that the transitive dependencies are not resolved correctly. This mostly happens in Spring Boot due to its built in class path scanning, which is adapted by Vaadin.

Please ensure, that, if you are using the `vaadin.whitelist` property, that it lists the addon's package `org.vaadin.stefan` (or simply `org.vaadin`).

If you are not using any whitelist / blacklist in the properties and still have the issue, please check, if you have added the `@EnableVaadin` annotation to your Spring application class. If that is the case, check, if there are packages listed. If yes, add the package `org.vaadin.stefan` to it.

If the annotation is not added or added without any parameter and the issue occur, please add
the package `org.vaadin.stefan` plus other necessary package, that have to be scanned, as parameters.

This should enable Spring to analyze all relevant npm dependencies at runtime. Other CDI version should work the same.