package org.vaadin.stefan.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.stefan.fullcalendar.DatesRenderedEvent;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.ViewSkeletonRenderedEvent;
import org.vaadin.stefan.ui.menu.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Abstract base class for all demo views in Phase 10.2+.
 * <p>
 * Provides a consistent layout: Title (H2) → Description → optional Toolbar → Calendar →
 * optional collapsible source code panel.
 * </p>
 * <p>
 * This class does NOT extend {@link AbstractCalendarView}. Both coexist during migration.
 * {@link AbstractCalendarView} will be removed in Phase 10.17.
 * </p>
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDemoView extends VerticalLayout {

    private static final String PRISM_THEME_CSS =
            "https://cdnjs.cloudflare.com/ajax/libs/prism-themes/1.9.0/prism-coldark-dark.min.css";
    private static final String PRISM_CORE_JS =
            "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js";
    private static final String PRISM_JAVA_JS =
            "https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-java.min.js";

    private static final String DEMO_START_MARKER = "// DEMO-START";
    private static final String DEMO_END_MARKER = "// DEMO-END";

    private final FullCalendar<?> calendar;

    protected AbstractDemoView() {
        calendar = createCalendar();

        setSizeFull();
        setFlexGrow(1, calendar);

        VerticalLayout header = new VerticalLayout();
        header.setSpacing(false);
        header.setPadding(false);

        Component titleElement = buildTitleElement();
        if (titleElement != null) {
            header.add(titleElement);
        }

        Component descriptionElement = buildDescriptionElement();
        if (descriptionElement != null) {
            header.add(descriptionElement);
            header.setHorizontalComponentAlignment(Alignment.STRETCH, descriptionElement);
        }

        if (titleElement != null || descriptionElement != null) {
            add(header);
            setHorizontalComponentAlignment(Alignment.STRETCH, header);
        }

        Component toolbar = createToolbar();
        if (toolbar != null) {
            add(toolbar);
            setHorizontalComponentAlignment(Alignment.CENTER, toolbar);
            wireToolbar(toolbar);
        }

        calendar.getStyle().set("min-height", "50vh");
        add(calendar);
        setHorizontalComponentAlignment(Alignment.STRETCH, calendar);

        if (isCodeDisplayEnabled()) {
            buildCodePanel().ifPresent(panel -> {
                add(panel);
                setHorizontalComponentAlignment(Alignment.STRETCH, panel);
            });
        }
    }

    // -------------------------------------------------------------------------
    // Abstract / overridable hooks
    // -------------------------------------------------------------------------

    /**
     * Creates the calendar instance. Called once in the constructor.
     * Each concrete view is responsible for full calendar setup (provider, mapper, options, etc.).
     *
     * @return a fully configured calendar; never null
     */
    protected abstract FullCalendar<?> createCalendar();

    /**
     * Returns an optional toolbar component. Return {@code null} for no toolbar (default).
     * <p>
     * If the returned component implements {@link DemoToolbar}, it is automatically wired
     * to the calendar's dates-rendered and view-changed listeners.
     * </p>
     *
     * @return toolbar component, or null
     */
    protected Component createToolbar() {
        return null;
    }

    /**
     * Returns a short, warm-toned description shown above the calendar.
     * Return {@code null} to show no description (default).
     *
     * @return description text, or null
     */
    protected String createDescription() {
        return null;
    }

    /**
     * Returns the view title. Derived from the {@link MenuItem} annotation on the class,
     * or from the class name split by camel-case (same pattern as {@link AbstractCalendarView}).
     *
     * @return title string; never null
     */
    protected String createTitle() {
        MenuItem item = getClass().getAnnotation(MenuItem.class);
        return item != null
                ? item.label()
                : String.join(" ", StringUtils.splitByCharacterTypeCamelCase(getClass().getSimpleName()));
    }

    /**
     * Controls whether the collapsible source code panel is shown below the calendar.
     * Override and return {@code false} to disable (e.g. the Playground view).
     *
     * @return true to show code panel (default), false to suppress it entirely
     */
    protected boolean isCodeDisplayEnabled() {
        return true;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Component buildTitleElement() {
        String title = createTitle();
        if (title == null) {
            return null;
        }
        H2 h2 = new H2(title);
        h2.addClassName("title");
        return h2;
    }

    private Component buildDescriptionElement() {
        String description = createDescription();
        if (description == null) {
            return null;
        }
        Span span = new Span(description);
        span.addClassName("description");
        return span;
    }

    /**
     * Wires a {@link DemoToolbar} to the calendar's dates-rendered and view-changed listeners.
     * Non-DemoToolbar components are silently ignored.
     * <p>
     * The raw-type cast is intentional: {@code FullCalendar<?>} does not allow adding typed
     * listeners directly due to wildcard capture. Using the raw type is safe here because the
     * listener only reads event data (intervalStart, calendarView) which are type-independent.
     * </p>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void wireToolbar(Component toolbar) {
        if (toolbar instanceof DemoToolbar dt) {
            FullCalendar raw = calendar;
            raw.addDatesRenderedListener(event -> dt.updateInterval(((DatesRenderedEvent<?>) event).getIntervalStart()));
            raw.addViewChangedListener(event -> ((ViewSkeletonRenderedEvent<?>) event).getCalendarView().ifPresent(dt::updateSelectedView));
        }
    }

    /**
     * Reads the view's Java source from the classpath, extracts the region between
     * {@code // DEMO-START} and {@code // DEMO-END} markers, and builds a collapsible
     * {@link Details} panel with Prism.js syntax highlighting.
     *
     * @return the code panel, or empty if markers were not found or reading failed
     */
    private java.util.Optional<Component> buildCodePanel() {
        String sourceCode = readMarkedSourceRegion();
        if (sourceCode == null || sourceCode.isBlank()) {
            return java.util.Optional.empty();
        }

        // Load Prism.js CSS and JS from CDN on the current page
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().addStyleSheet(PRISM_THEME_CSS);
            ui.getPage().addJavaScript(PRISM_CORE_JS);
            ui.getPage().addJavaScript(PRISM_JAVA_JS);
        }

        // HTML-escape the source to prevent XSS
        String escaped = htmlEscape(sourceCode);

        // Build a Pre element with the escaped code inside a <code class="language-java"> tag
        Pre pre = new Pre();
        pre.getElement().setProperty("innerHTML",
                "<code class=\"language-java\">" + escaped + "</code>");
        pre.getStyle()
                .set("margin", "0")
                .set("overflow-x", "auto");

        Scroller codeScroller = new Scroller(pre);
        codeScroller.setMaxHeight("50vh");

        Details details = new Details("Show Source Code", codeScroller);
        details.setOpened(false);

        // Trigger Prism.highlightAll() when the panel is opened
        details.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                UI currentUi = UI.getCurrent();
                if (currentUi != null) {
                    currentUi.getPage().executeJs("if (window.Prism) { Prism.highlightAll(); }");
                }
            }
        });

        return java.util.Optional.of(details);
    }

    /**
     * Reads the Java source file for this view class from the classpath (under {@code /demo-sources/}),
     * then extracts the text between {@code // DEMO-START} and {@code // DEMO-END} markers.
     * Lines containing the markers themselves are excluded.
     *
     * @return extracted source text, or null if the file or markers were not found
     */
    private String readMarkedSourceRegion() {
        String resourcePath = "/demo-sources/" + getClass().getName().replace('.', '/') + ".java";

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }

            String fullSource = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            int startIdx = fullSource.indexOf(DEMO_START_MARKER);
            int endIdx = fullSource.indexOf(DEMO_END_MARKER);

            if (startIdx < 0 || endIdx < 0 || endIdx <= startIdx) {
                return null;
            }

            // Skip the marker line itself: advance past the newline after DEMO-START
            int regionStart = fullSource.indexOf('\n', startIdx);
            if (regionStart < 0 || regionStart >= endIdx) {
                return null;
            }
            regionStart++; // skip the newline character

            // Trim trailing newline before DEMO-END marker
            int regionEnd = fullSource.lastIndexOf('\n', endIdx);
            if (regionEnd <= regionStart) {
                return null;
            }

            return fullSource.substring(regionStart, regionEnd);
        } catch (IOException e) {
            // Graceful fallback — log but do not surface an error to the user
            System.err.println("[AbstractDemoView] Could not read source for " + getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * HTML-escapes the four characters that have special meaning in HTML content
     * ({@code &}, {@code <}, {@code >}, {@code "}).
     *
     * @param text raw text
     * @return escaped text safe for use inside an HTML element
     */
    private static String htmlEscape(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
