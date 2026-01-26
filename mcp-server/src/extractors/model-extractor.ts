import * as fs from 'fs';
import * as path from 'path';
import type { EntryProperty, CalendarViewInfo, EventTypeInfo } from '../types.js';

/**
 * Extracts model schemas from Java source files.
 */
export class ModelExtractor {
  private addonRoot: string;
  private schedulerRoot: string;

  constructor(addonRoot: string, schedulerRoot: string) {
    this.addonRoot = addonRoot;
    this.schedulerRoot = schedulerRoot;
  }

  async extractEntryProperties(): Promise<EntryProperty[]> {
    const entryPath = path.join(this.addonRoot, 'src/main/java/org/vaadin/stefan/fullcalendar/Entry.java');

    if (!fs.existsSync(entryPath)) {
      console.warn('Entry.java not found');
      return this.getDefaultEntryProperties();
    }

    const content = fs.readFileSync(entryPath, 'utf-8');
    const properties: EntryProperty[] = [];

    // Match field declarations with their annotations and Javadoc
    const fieldRegex = /(?:\/\*\*\s*([\s\S]*?)\s*\*\/\s*)?((?:@\w+(?:\([^)]*\))?\s*)*)(private|protected)\s+([\w<>[\],\s.?]+)\s+(\w+)\s*(?:=\s*([^;]+))?;/g;

    let match;
    while ((match = fieldRegex.exec(content)) !== null) {
      const [, javadoc, annotations, visibility, type, name, defaultValue] = match;

      // Skip internal fields
      if (name.startsWith('_') || name === 'calendar' || name === 'customProperties') continue;

      const cleanedJavadoc = javadoc
        ? javadoc.replace(/^\s*\*\s?/gm, '').replace(/@\w+.*/g, '').trim()
        : '';

      // Extract JSON name annotation
      const jsonNameMatch = annotations.match(/@JsonName\s*\(\s*"([^"]+)"\s*\)/);
      const jsonName = jsonNameMatch ? jsonNameMatch[1] : undefined;

      // Check if client can update
      const clientUpdatable = annotations.includes('@JsonUpdateAllowed');

      properties.push({
        name,
        type: this.simplifyType(type),
        description: cleanedJavadoc || `The ${name} property of the entry`,
        defaultValue: defaultValue?.trim(),
        jsonName,
        clientUpdatable,
      });
    }

    // Add additional properties from getter methods if not already found
    const getterProperties = this.extractPropertiesFromGetters(content, properties);
    properties.push(...getterProperties);

    return properties;
  }

  private extractPropertiesFromGetters(content: string, existingProps: EntryProperty[]): EntryProperty[] {
    const properties: EntryProperty[] = [];
    const existingNames = new Set(existingProps.map(p => p.name.toLowerCase()));

    // Match public getter methods
    const getterRegex = /(?:\/\*\*\s*([\s\S]*?)\s*\*\/\s*)?public\s+([\w<>[\],\s.?]+)\s+(get|is)(\w+)\s*\(\s*\)/g;

    let match;
    while ((match = getterRegex.exec(content)) !== null) {
      const [, javadoc, returnType, prefix, namePart] = match;
      const propertyName = namePart.charAt(0).toLowerCase() + namePart.slice(1);

      if (existingNames.has(propertyName.toLowerCase())) continue;
      if (propertyName === 'calendar' || propertyName === 'customProperties') continue;

      const cleanedJavadoc = javadoc
        ? javadoc.replace(/^\s*\*\s?/gm, '').replace(/@\w+.*/g, '').trim()
        : '';

      properties.push({
        name: propertyName,
        type: this.simplifyType(returnType),
        description: cleanedJavadoc || `The ${propertyName} property`,
        clientUpdatable: false,
      });

      existingNames.add(propertyName.toLowerCase());
    }

    return properties;
  }

  private simplifyType(type: string): string {
    return type
      .replace(/java\.lang\./g, '')
      .replace(/java\.time\./g, '')
      .replace(/java\.util\./g, '')
      .trim();
  }

  async extractCalendarViews(): Promise<CalendarViewInfo[]> {
    const views: CalendarViewInfo[] = [];

    // Extract from CalendarViewImpl enum
    const viewImplPath = path.join(this.addonRoot, 'src/main/java/org/vaadin/stefan/fullcalendar/CalendarViewImpl.java');
    if (fs.existsSync(viewImplPath)) {
      const content = fs.readFileSync(viewImplPath, 'utf-8');
      const enumValues = this.extractEnumValues(content);

      for (const value of enumValues) {
        views.push({
          name: this.humanizeEnumValue(value),
          enumValue: value,
          description: this.getViewDescription(value),
          isSchedulerView: false,
        });
      }
    }

    // Extract from SchedulerView enum
    const schedulerViewPath = path.join(this.schedulerRoot, 'src/main/java/org/vaadin/stefan/fullcalendar/SchedulerView.java');
    if (fs.existsSync(schedulerViewPath)) {
      const content = fs.readFileSync(schedulerViewPath, 'utf-8');
      const enumValues = this.extractEnumValues(content);

      for (const value of enumValues) {
        views.push({
          name: this.humanizeEnumValue(value),
          enumValue: value,
          description: this.getViewDescription(value),
          isSchedulerView: true,
        });
      }
    }

    return views;
  }

  private extractEnumValues(content: string): string[] {
    const values: string[] = [];

    // Match enum constants (before the first semicolon or method)
    const enumBodyMatch = content.match(/enum\s+\w+[^{]*\{([^;]+)/);
    if (!enumBodyMatch) return values;

    const enumBody = enumBodyMatch[1];
    const constantRegex = /([A-Z_]+)(?:\s*\([^)]*\))?/g;

    let match;
    while ((match = constantRegex.exec(enumBody)) !== null) {
      if (match[1] && !match[1].includes('(')) {
        values.push(match[1]);
      }
    }

    return values;
  }

  private humanizeEnumValue(value: string): string {
    return value
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  }

  private getViewDescription(value: string): string {
    const descriptions: Record<string, string> = {
      DAY_GRID_MONTH: 'Monthly view with days displayed in a grid',
      DAY_GRID_WEEK: 'Weekly view with days displayed in a grid',
      DAY_GRID_DAY: 'Single day view displayed in a grid format',
      DAY_GRID_YEAR: 'Yearly view with days displayed in a grid',
      TIME_GRID_WEEK: 'Weekly view with time slots displayed vertically',
      TIME_GRID_DAY: 'Single day view with time slots displayed vertically',
      LIST_WEEK: 'Weekly list view of events',
      LIST_DAY: 'Daily list view of events',
      LIST_MONTH: 'Monthly list view of events',
      LIST_YEAR: 'Yearly list view of events',
      MULTI_MONTH: 'Multiple months displayed at once',
      TIMELINE_DAY: 'Timeline view for a single day with resources',
      TIMELINE_WEEK: 'Timeline view for a week with resources',
      TIMELINE_MONTH: 'Timeline view for a month with resources',
      TIMELINE_YEAR: 'Timeline view for a year with resources',
      VERTICAL_RESOURCE_MONTH: 'Monthly view with resources displayed vertically',
      VERTICAL_RESOURCE_WEEK: 'Weekly view with resources displayed vertically',
    };

    return descriptions[value] || `Calendar view: ${value}`;
  }

  async extractEventTypes(): Promise<EventTypeInfo[]> {
    const events: EventTypeInfo[] = [];
    const eventDir = path.join(this.addonRoot, 'src/main/java/org/vaadin/stefan/fullcalendar');

    if (!fs.existsSync(eventDir)) {
      return events;
    }

    const files = fs.readdirSync(eventDir);

    for (const file of files) {
      if (!file.endsWith('Event.java')) continue;

      const filePath = path.join(eventDir, file);
      const content = fs.readFileSync(filePath, 'utf-8');

      // Check if it's an event class
      if (!content.includes('extends ComponentEvent') && !content.includes('ComponentEvent<')) continue;

      const className = file.replace('.java', '');

      // Extract class Javadoc
      const javadocMatch = content.match(/\/\*\*\s*([\s\S]*?)\s*\*\/\s*(?:@\w+[^\n]*\n\s*)*public\s+(?:abstract\s+)?class/);
      const description = javadocMatch
        ? javadocMatch[1].replace(/^\s*\*\s?/gm, '').replace(/@\w+.*/g, '').trim()
        : `Event fired when ${this.humanizeClassName(className)}`;

      // Extract properties from getter methods
      const properties = this.extractEventProperties(content);

      events.push({
        name: className,
        description,
        properties,
        source: 'core',
      });
    }

    // Also check scheduler events
    const schedulerEventDir = path.join(this.schedulerRoot, 'src/main/java/org/vaadin/stefan/fullcalendar');
    if (fs.existsSync(schedulerEventDir)) {
      const schedulerFiles = fs.readdirSync(schedulerEventDir);

      for (const file of schedulerFiles) {
        if (!file.endsWith('Event.java')) continue;

        const filePath = path.join(schedulerEventDir, file);
        const content = fs.readFileSync(filePath, 'utf-8');

        if (!content.includes('extends') || !content.includes('Event')) continue;

        const className = file.replace('.java', '');

        const javadocMatch = content.match(/\/\*\*\s*([\s\S]*?)\s*\*\/\s*(?:@\w+[^\n]*\n\s*)*public\s+class/);
        const description = javadocMatch
          ? javadocMatch[1].replace(/^\s*\*\s?/gm, '').replace(/@\w+.*/g, '').trim()
          : `Scheduler event: ${this.humanizeClassName(className)}`;

        const properties = this.extractEventProperties(content);

        events.push({
          name: className,
          description,
          properties,
          source: 'scheduler',
        });
      }
    }

    return events;
  }

  private extractEventProperties(content: string): { name: string; type: string; description: string }[] {
    const properties: { name: string; type: string; description: string }[] = [];

    const getterRegex = /(?:\/\*\*\s*([\s\S]*?)\s*\*\/\s*)?public\s+([\w<>[\],\s.?]+)\s+(get|is)(\w+)\s*\(\s*\)/g;

    let match;
    while ((match = getterRegex.exec(content)) !== null) {
      const [, javadoc, returnType, prefix, namePart] = match;
      const propertyName = namePart.charAt(0).toLowerCase() + namePart.slice(1);

      // Skip common inherited properties
      if (['source', 'fromClient', 'unregisterListener'].includes(propertyName)) continue;

      const description = javadoc
        ? javadoc.replace(/^\s*\*\s?/gm, '').replace(/@\w+.*/g, '').trim()
        : '';

      properties.push({
        name: propertyName,
        type: this.simplifyType(returnType),
        description,
      });
    }

    return properties;
  }

  private humanizeClassName(className: string): string {
    return className
      .replace(/([A-Z])/g, ' $1')
      .toLowerCase()
      .trim();
  }

  private getDefaultEntryProperties(): EntryProperty[] {
    // Fallback properties if Entry.java cannot be parsed
    return [
      { name: 'id', type: 'String', description: 'Unique identifier for the entry', clientUpdatable: false },
      { name: 'title', type: 'String', description: 'Display title of the entry', clientUpdatable: true },
      { name: 'start', type: 'Instant', description: 'Start date/time of the entry', clientUpdatable: true },
      { name: 'end', type: 'Instant', description: 'End date/time of the entry', clientUpdatable: true },
      { name: 'allDay', type: 'boolean', description: 'Whether the entry spans the entire day', clientUpdatable: true },
      { name: 'color', type: 'String', description: 'Background and border color', clientUpdatable: true },
      { name: 'editable', type: 'boolean', description: 'Whether the entry can be modified', clientUpdatable: false },
      { name: 'displayMode', type: 'DisplayMode', description: 'How the entry is displayed', clientUpdatable: false },
    ];
  }
}
