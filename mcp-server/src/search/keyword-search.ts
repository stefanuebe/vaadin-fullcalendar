import FlexSearch from 'flexsearch';
import type { DocumentationPage, JavaClass, CodeExample, SearchResult } from '../types.js';

interface IndexedItem {
  id: string;
  title: string;
  content: string;
  type: 'docs' | 'api' | 'example';
  path?: string;
}

export class KeywordSearch {
  private index: FlexSearch.Index;
  private items: Map<string, IndexedItem> = new Map();

  constructor() {
    this.index = new FlexSearch.Index({
      tokenize: 'forward',
      resolution: 9,
      cache: true,
    });
  }

  addDocumentation(docs: DocumentationPage[]): void {
    for (const doc of docs) {
      const item: IndexedItem = {
        id: `doc:${doc.id}`,
        title: doc.title,
        content: doc.content,
        type: 'docs',
        path: doc.path,
      };
      this.items.set(item.id, item);
      this.index.add(item.id, `${doc.title} ${doc.content} ${doc.tags.join(' ')}`);
    }
  }

  addClasses(classes: JavaClass[]): void {
    for (const cls of classes) {
      const methodNames = cls.methods.map(m => m.name).join(' ');
      const methodDescs = cls.methods.map(m => m.description).join(' ');
      const item: IndexedItem = {
        id: `api:${cls.fullName}`,
        title: cls.name,
        content: `${cls.description} ${methodNames} ${methodDescs}`,
        type: 'api',
        path: cls.fullName,
      };
      this.items.set(item.id, item);
      this.index.add(item.id, `${cls.name} ${cls.fullName} ${cls.description} ${methodNames} ${methodDescs}`);
    }
  }

  addExamples(examples: CodeExample[]): void {
    for (const example of examples) {
      const item: IndexedItem = {
        id: `example:${example.id}`,
        title: example.title,
        content: example.code,
        type: 'example',
        path: example.id,
      };
      this.items.set(item.id, item);
      this.index.add(item.id, `${example.title} ${example.description} ${example.code} ${example.tags.join(' ')}`);
    }
  }

  search(query: string, limit: number = 10): SearchResult[] {
    const resultIds = this.index.search(query, { limit }) as string[];
    const results: SearchResult[] = [];

    for (const id of resultIds) {
      const item = this.items.get(id);
      if (item) {
        const snippet = this.extractSnippet(item.content, query);
        results.push({
          id: item.id,
          title: item.title,
          snippet,
          score: 1 - results.length / limit, // Simple decreasing score
          type: item.type,
          path: item.path,
        });
      }
    }

    return results;
  }

  private extractSnippet(content: string, query: string): string {
    const lowerContent = content.toLowerCase();
    const lowerQuery = query.toLowerCase();
    const queryWords = lowerQuery.split(/\s+/);

    // Find the first occurrence of any query word
    let bestPos = -1;
    for (const word of queryWords) {
      const pos = lowerContent.indexOf(word);
      if (pos !== -1 && (bestPos === -1 || pos < bestPos)) {
        bestPos = pos;
      }
    }

    if (bestPos === -1) {
      // No match found, return beginning
      return content.slice(0, 200) + (content.length > 200 ? '...' : '');
    }

    // Extract snippet around the match
    const start = Math.max(0, bestPos - 50);
    const end = Math.min(content.length, bestPos + 150);
    let snippet = content.slice(start, end);

    if (start > 0) snippet = '...' + snippet;
    if (end < content.length) snippet = snippet + '...';

    return snippet.replace(/\n+/g, ' ').trim();
  }
}
