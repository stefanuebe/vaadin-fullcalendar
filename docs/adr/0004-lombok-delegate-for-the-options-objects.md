# Lombok @Delegate for the options objects

The public options objects (`FullCalendarOptions`, `FullCalendarViewOptions`, `FullCalendarSchedulerOptions`, `FullCalendarSchedulerViewOptions`) get their option getters and setters from Lombok's `@Delegate`, which forwards every public method of the data classes (`CalendarOptions`, `ViewOptions`, `SchedulerCalendarOptions`, `SchedulerViewOptions`). A new option is then one field in one data class, and the public objects carry about 1,690 forwarding methods that nobody writes by hand. The delegates call the accessor methods (`calendarOptions()`, `viewOptions()`), not the fields, because each call schedules the option sync with the client.

The cost is that `@Delegate` is in `lombok.experimental`. A Java or Lombok release could break it or Lombok could drop it. The forwarding methods also carry no Javadoc of their own, and the Vaadin dev loop does not run Lombok, so a new option needs a Maven build before it shows up.

## Fallback if @Delegate stops working

The four files above use nothing from Lombok except `@Delegate`. Getters, setters and the other Lombok annotations are in other classes and stay.

1. Run `delombok` over the main sources of `addon` and `addon-scheduler` with Lombok on the classpath (the delegated getters come from `@Getter`, so delombok needs the whole source set).
2. Take only the four files from the output. Remove the `@lombok.Generated` and `@SuppressWarnings("all")` lines and replace the fully qualified type names with imports.
3. Remove the `lombok.experimental.Delegate` imports. Keep the forwarding through the accessor methods.
4. Add a reflection test that every public method of each data class exists on its public options object, respecting the current `excludes` (the scheduler objects exclude the core data classes). Without `@Delegate`, a new option needs its forwarding methods by hand, in two classes for a per-view option, and this test catches a missing one.

The four files grow from about 360 to about 7,100 lines. A trial run on 2026-11-19 compiled both the Lombok and the delomboked sources and compared every class with `javap -p`: no difference, so the public API and the serialized form stay the same. `OptionsApiTest` and `SchedulerOptionsApiTest` check the public signatures against the `*-api.txt` files and guard the step.

## Considered Options

- Forwarding methods by hand from the start: about 1,690 trivial methods, and each new option touches two or three classes instead of one.
- The public objects extend the data classes: not possible for the per-view options, which `FullCalendarOptions` and `FullCalendarViewOptions` both expose, because Java has single inheritance.
- Remove Lombok altogether: not needed for this risk. Getters, setters and builders are stable Lombok features.
