import OpenAI from 'openai';
import type { DocumentationPage, JavaClass, CodeExample, SearchResult } from '../types.js';

interface EmbeddedItem {
  id: string;
  title: string;
  content: string;
  type: 'docs' | 'api' | 'example';
  path?: string;
  embedding: number[];
}

export class SemanticSearch {
  private openai: OpenAI | null = null;
  private items: EmbeddedItem[] = [];
  private isInitialized = false;

  constructor() {
    const apiKey = process.env.OPENAI_API_KEY;
    if (apiKey) {
      this.openai = new OpenAI({ apiKey });
    }
  }

  isAvailable(): boolean {
    return this.openai !== null;
  }

  async initialize(
    docs: DocumentationPage[],
    classes: JavaClass[],
    examples: CodeExample[],
    precomputedEmbeddings?: Map<string, number[]>
  ): Promise<void> {
    if (!this.openai) {
      console.log('Semantic search not available: OPENAI_API_KEY not set');
      return;
    }

    console.log('Initializing semantic search...');

    // Prepare all items
    const allItems: { id: string; title: string; content: string; type: 'docs' | 'api' | 'example'; path?: string }[] = [];

    for (const doc of docs) {
      allItems.push({
        id: `doc:${doc.id}`,
        title: doc.title,
        content: `${doc.title}\n\n${doc.content}`.slice(0, 8000),
        type: 'docs',
        path: doc.path,
      });
    }

    for (const cls of classes) {
      const methodSummary = cls.methods.slice(0, 20).map(m => `${m.name}(${m.parameters.map(p => p.type).join(', ')}): ${m.returnType}`).join('\n');
      allItems.push({
        id: `api:${cls.fullName}`,
        title: cls.name,
        content: `${cls.name} - ${cls.description}\n\nMethods:\n${methodSummary}`.slice(0, 8000),
        type: 'api',
        path: cls.fullName,
      });
    }

    for (const example of examples) {
      allItems.push({
        id: `example:${example.id}`,
        title: example.title,
        content: `${example.title}\n${example.description}\n\n${example.code}`.slice(0, 8000),
        type: 'example',
        path: example.id,
      });
    }

    // Get embeddings (use precomputed if available)
    for (const item of allItems) {
      let embedding: number[];

      if (precomputedEmbeddings?.has(item.id)) {
        embedding = precomputedEmbeddings.get(item.id)!;
      } else {
        embedding = await this.getEmbedding(item.content);
      }

      this.items.push({ ...item, embedding });
    }

    this.isInitialized = true;
    console.log(`Semantic search initialized with ${this.items.length} items`);
  }

  private async getEmbedding(text: string): Promise<number[]> {
    if (!this.openai) {
      throw new Error('OpenAI client not initialized');
    }

    const response = await this.openai.embeddings.create({
      model: 'text-embedding-3-small',
      input: text,
    });

    return response.data[0].embedding;
  }

  private cosineSimilarity(a: number[], b: number[]): number {
    let dotProduct = 0;
    let normA = 0;
    let normB = 0;

    for (let i = 0; i < a.length; i++) {
      dotProduct += a[i] * b[i];
      normA += a[i] * a[i];
      normB += b[i] * b[i];
    }

    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
  }

  async search(query: string, limit: number = 10): Promise<SearchResult[]> {
    if (!this.isInitialized || !this.openai) {
      return [];
    }

    const queryEmbedding = await this.getEmbedding(query);

    const scored = this.items.map(item => ({
      item,
      score: this.cosineSimilarity(queryEmbedding, item.embedding),
    }));

    scored.sort((a, b) => b.score - a.score);

    return scored.slice(0, limit).map(({ item, score }) => ({
      id: item.id,
      title: item.title,
      snippet: item.content.slice(0, 200) + '...',
      score,
      type: item.type,
      path: item.path,
    }));
  }
}
