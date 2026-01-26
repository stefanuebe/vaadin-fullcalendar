import type { ExtractedData, SearchResult, JavaClass, CodeExample, EntryProperty, CalendarViewInfo, EventTypeInfo } from '../types.js';
import { HybridSearch } from '../search/index.js';

export class ToolHandlers {
  private data: ExtractedData;
  private search: HybridSearch;

  constructor(data: ExtractedData, search: HybridSearch) {
    this.data = data;
    this.search = search;
  }

  /**
   * Search across all documentation, API, and examples
   */
  async searchDocs(params: { query: string; limit?: number; mode?: 'auto' | 'semantic' | 'keyword' }): Promise<{
    results: SearchResult[];
    mode: string;
  }> {
    const { query, limit = 10, mode = 'auto' } = params;
    const results = await this.search.search(query, { limit, mode });

    return {
      results,
      mode: this.search.getSearchMode(),
    };
  }

  /**
   * Get API reference for a specific class
   */
  getApiReference(params: { className: string }): JavaClass | { error: string } {
    const { className } = params;
    const normalizedName = className.toLowerCase();

    // Try exact match first
    let cls = this.data.classes.find(c => c.name.toLowerCase() === normalizedName);

    // Try full name match
    if (!cls) {
      cls = this.data.classes.find(c => c.fullName.toLowerCase() === normalizedName);
    }

    // Try partial match
    if (!cls) {
      cls = this.data.classes.find(c =>
        c.name.toLowerCase().includes(normalizedName) ||
        c.fullName.toLowerCase().includes(normalizedName)
      );
    }

    if (!cls) {
      return {
        error: `Class '${className}' not found. Available classes: ${this.data.classes.map(c => c.name).slice(0, 20).join(', ')}...`,
      };
    }

    return cls;
  }

  /**
   * List all available classes
   */
  listClasses(params: { filter?: string; source?: 'addon' | 'addon-scheduler' }): { classes: { name: string; fullName: string; type: string; description: string }[] } {
    let classes = this.data.classes;

    if (params.source) {
      classes = classes.filter(c => c.source === params.source);
    }

    if (params.filter) {
      const filterLower = params.filter.toLowerCase();
      classes = classes.filter(c =>
        c.name.toLowerCase().includes(filterLower) ||
        c.description.toLowerCase().includes(filterLower)
      );
    }

    return {
      classes: classes.map(c => ({
        name: c.name,
        fullName: c.fullName,
        type: c.type,
        description: c.description.slice(0, 200),
      })),
    };
  }

  /**
   * Get a code example by ID or search
   */
  getCodeExample(params: { id?: string; search?: string; category?: string }): CodeExample | CodeExample[] | { error: string } {
    if (params.id) {
      const example = this.data.examples.find(e => e.id === params.id);
      if (!example) {
        return { error: `Example '${params.id}' not found` };
      }
      return example;
    }

    let examples = this.data.examples;

    if (params.category) {
      examples = examples.filter(e => e.category === params.category);
    }

    if (params.search) {
      const searchLower = params.search.toLowerCase();
      examples = examples.filter(e =>
        e.title.toLowerCase().includes(searchLower) ||
        e.description.toLowerCase().includes(searchLower) ||
        e.tags.some(t => t.includes(searchLower))
      );
    }

    if (examples.length === 0) {
      return { error: 'No matching examples found' };
    }

    return examples.slice(0, 10);
  }

  /**
   * Get Entry model schema with all properties
   */
  getEntrySchema(): { properties: EntryProperty[]; description: string } {
    return {
      properties: this.data.entryProperties,
      description: 'Entry represents a calendar event with timing, display, and recurrence properties. Entries can be linked to a calendar and optionally to resources (in Scheduler mode).',
    };
  }

  /**
   * Get Resource model schema
   */
  getResourceSchema(): { properties: { name: string; type: string; description: string }[]; description: string } {
    // Resource properties are simpler, extract from classes if available
    const resourceClass = this.data.classes.find(c => c.name === 'Resource');

    const properties = resourceClass
      ? resourceClass.methods
        .filter(m => m.name.startsWith('get') && m.isPublic)
        .map(m => ({
          name: m.name.replace(/^get/, '').charAt(0).toLowerCase() + m.name.replace(/^get/, '').slice(1),
          type: m.returnType,
          description: m.description || `The ${m.name.replace(/^get/, '')} property`,
        }))
      : [
        { name: 'id', type: 'String', description: 'Unique identifier for the resource' },
        { name: 'title', type: 'String', description: 'Display title of the resource' },
        { name: 'color', type: 'String', description: 'Color associated with the resource' },
        { name: 'children', type: 'Set<Resource>', description: 'Child resources for hierarchical display' },
        { name: 'parent', type: 'Resource', description: 'Parent resource reference' },
      ];

    return {
      properties,
      description: 'Resource represents a schedulable entity (person, room, equipment) in the Scheduler extension. Resources can be hierarchical with parent/child relationships.',
    };
  }

  /**
   * List available calendar views
   */
  listCalendarViews(params: { includeScheduler?: boolean }): { views: CalendarViewInfo[] } {
    let views = this.data.calendarViews;

    if (params.includeScheduler === false) {
      views = views.filter(v => !v.isSchedulerView);
    }

    return { views };
  }

  /**
   * List event types
   */
  listEventTypes(params: { source?: 'core' | 'scheduler' }): { events: EventTypeInfo[] } {
    let events = this.data.eventTypes;

    if (params.source) {
      events = events.filter(e => e.source === params.source);
    }

    return { events };
  }

  /**
   * Get migration guide for a specific version
   */
  getMigrationGuide(params: { fromVersion?: string; toVersion?: string }): { content: string } | { error: string } {
    const migrationDoc = this.data.documentation.find(d =>
      d.path.toLowerCase().includes('migration')
    );

    if (!migrationDoc) {
      return { error: 'Migration guide not found' };
    }

    let content = migrationDoc.content;

    // If specific versions requested, try to extract relevant section
    if (params.fromVersion || params.toVersion) {
      const versionPattern = params.toVersion || params.fromVersion;
      if (versionPattern) {
        const sections = content.split(/^##\s+/m);
        const relevantSection = sections.find(s =>
          s.toLowerCase().includes(versionPattern.toLowerCase())
        );
        if (relevantSection) {
          content = '## ' + relevantSection;
        }
      }
    }

    return { content };
  }

  /**
   * Get full documentation page
   */
  getDocumentation(params: { path: string }): { title: string; content: string } | { error: string } {
    const doc = this.data.documentation.find(d =>
      d.path.toLowerCase() === params.path.toLowerCase() ||
      d.id === params.path.toLowerCase()
    );

    if (!doc) {
      const available = this.data.documentation.map(d => d.path).join(', ');
      return { error: `Documentation '${params.path}' not found. Available: ${available}` };
    }

    return {
      title: doc.title,
      content: doc.content,
    };
  }

  /**
   * Get server info and statistics
   */
  getServerInfo(): {
    version: string;
    extractedAt: string;
    searchMode: string;
    counts: {
      documentation: number;
      classes: number;
      examples: number;
      entryProperties: number;
      calendarViews: number;
      eventTypes: number;
    };
  } {
    return {
      version: this.data.version,
      extractedAt: this.data.extractedAt,
      searchMode: this.search.getSearchMode(),
      counts: {
        documentation: this.data.documentation.length,
        classes: this.data.classes.length,
        examples: this.data.examples.length,
        entryProperties: this.data.entryProperties.length,
        calendarViews: this.data.calendarViews.length,
        eventTypes: this.data.eventTypes.length,
      },
    };
  }
}
