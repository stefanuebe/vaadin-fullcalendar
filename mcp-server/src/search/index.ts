import { KeywordSearch } from './keyword-search.js';
import { SemanticSearch } from './semantic-search.js';
import type { DocumentationPage, JavaClass, CodeExample, SearchResult } from '../types.js';

export class HybridSearch {
  private keywordSearch: KeywordSearch;
  private semanticSearch: SemanticSearch;

  constructor() {
    this.keywordSearch = new KeywordSearch();
    this.semanticSearch = new SemanticSearch();
  }

  async initialize(
    docs: DocumentationPage[],
    classes: JavaClass[],
    examples: CodeExample[],
    precomputedEmbeddings?: Map<string, number[]>
  ): Promise<void> {
    // Always initialize keyword search
    this.keywordSearch.addDocumentation(docs);
    this.keywordSearch.addClasses(classes);
    this.keywordSearch.addExamples(examples);

    // Initialize semantic search if available
    if (this.semanticSearch.isAvailable()) {
      await this.semanticSearch.initialize(docs, classes, examples, precomputedEmbeddings);
    }
  }

  async search(query: string, options: { limit?: number; mode?: 'auto' | 'semantic' | 'keyword' } = {}): Promise<SearchResult[]> {
    const { limit = 10, mode = 'auto' } = options;

    if (mode === 'keyword') {
      return this.keywordSearch.search(query, limit);
    }

    if (mode === 'semantic' && this.semanticSearch.isAvailable()) {
      return this.semanticSearch.search(query, limit);
    }

    // Auto mode: prefer semantic, fallback to keyword
    if (this.semanticSearch.isAvailable()) {
      try {
        const semanticResults = await this.semanticSearch.search(query, limit);
        if (semanticResults.length > 0) {
          return semanticResults;
        }
      } catch (error) {
        console.error('Semantic search failed, falling back to keyword:', error);
      }
    }

    return this.keywordSearch.search(query, limit);
  }

  getSearchMode(): 'semantic' | 'keyword' {
    return this.semanticSearch.isAvailable() ? 'semantic' : 'keyword';
  }
}

export { KeywordSearch } from './keyword-search.js';
export { SemanticSearch } from './semantic-search.js';
