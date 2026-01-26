// MCP Server Types for FullCalendar Vaadin

export interface DocumentationPage {
  id: string;
  title: string;
  path: string;
  content: string;
  category: 'docs' | 'api' | 'example';
  tags: string[];
}

export interface JavaClass {
  name: string;
  packageName: string;
  fullName: string;
  description: string;
  type: 'class' | 'interface' | 'enum';
  extends?: string;
  implements?: string[];
  methods: JavaMethod[];
  fields: JavaField[];
  constructors: JavaConstructor[];
  annotations: string[];
  isPublic: boolean;
  source: 'addon' | 'addon-scheduler';
}

export interface JavaMethod {
  name: string;
  description: string;
  returnType: string;
  parameters: JavaParameter[];
  annotations: string[];
  isPublic: boolean;
  isStatic: boolean;
}

export interface JavaField {
  name: string;
  type: string;
  description: string;
  annotations: string[];
  isPublic: boolean;
  isStatic: boolean;
  isFinal: boolean;
}

export interface JavaConstructor {
  description: string;
  parameters: JavaParameter[];
  annotations: string[];
  isPublic: boolean;
}

export interface JavaParameter {
  name: string;
  type: string;
  description?: string;
}

export interface CodeExample {
  id: string;
  title: string;
  description: string;
  code: string;
  language: 'java' | 'typescript';
  category: string;
  tags: string[];
}

export interface EntryProperty {
  name: string;
  type: string;
  description: string;
  defaultValue?: string;
  jsonName?: string;
  clientUpdatable: boolean;
}

export interface CalendarViewInfo {
  name: string;
  enumValue: string;
  description: string;
  isSchedulerView: boolean;
}

export interface EventTypeInfo {
  name: string;
  description: string;
  properties: { name: string; type: string; description: string }[];
  source: 'core' | 'scheduler';
}

export interface SearchResult {
  id: string;
  title: string;
  snippet: string;
  score: number;
  type: 'docs' | 'api' | 'example';
  path?: string;
}

export interface SearchIndex {
  keyword: FlexSearchIndex;
  embeddings?: Map<string, number[]>;
}

// FlexSearch types
export interface FlexSearchIndex {
  add(id: string | number, content: string): void;
  search(query: string, options?: { limit?: number }): string[] | number[];
  remove(id: string | number): void;
}

// Extracted data stored at build time
export interface ExtractedData {
  documentation: DocumentationPage[];
  classes: JavaClass[];
  examples: CodeExample[];
  entryProperties: EntryProperty[];
  calendarViews: CalendarViewInfo[];
  eventTypes: EventTypeInfo[];
  version: string;
  extractedAt: string;
}
