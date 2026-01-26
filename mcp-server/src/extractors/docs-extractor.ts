import * as fs from 'fs';
import * as path from 'path';
import matter from 'gray-matter';
import type { DocumentationPage, CodeExample } from '../types.js';

/**
 * Extracts documentation from markdown files and code examples from the demo.
 */
export class DocsExtractor {
  private docsRoot: string;
  private demoRoot: string;

  constructor(docsRoot: string, demoRoot: string) {
    this.docsRoot = docsRoot;
    this.demoRoot = demoRoot;
  }

  async extractDocumentation(): Promise<DocumentationPage[]> {
    const pages: DocumentationPage[] = [];
    const mdFiles = this.findMarkdownFiles(this.docsRoot);

    for (const file of mdFiles) {
      try {
        const page = await this.parseMarkdownFile(file);
        if (page) {
          pages.push(page);
        }
      } catch (error) {
        console.error(`Error parsing ${file}:`, error);
      }
    }

    return pages;
  }

  async extractCodeExamples(): Promise<CodeExample[]> {
    const examples: CodeExample[] = [];

    // Extract from documentation (code blocks in Samples.md)
    const samplesPath = path.join(this.docsRoot, 'Samples.md');
    if (fs.existsSync(samplesPath)) {
      const samplesExamples = this.extractCodeBlocksFromMarkdown(samplesPath);
      examples.push(...samplesExamples);
    }

    // Extract from demo source files
    const demoExamples = await this.extractFromDemoSources();
    examples.push(...demoExamples);

    return examples;
  }

  private findMarkdownFiles(dir: string): string[] {
    const files: string[] = [];

    if (!fs.existsSync(dir)) {
      return files;
    }

    const entries = fs.readdirSync(dir, { withFileTypes: true });

    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        files.push(...this.findMarkdownFiles(fullPath));
      } else if (entry.name.endsWith('.md')) {
        files.push(fullPath);
      }
    }

    return files;
  }

  private async parseMarkdownFile(filePath: string): Promise<DocumentationPage | null> {
    const content = fs.readFileSync(filePath, 'utf-8');
    const { data: frontmatter, content: body } = matter(content);

    const relativePath = path.relative(this.docsRoot, filePath);
    const id = relativePath.replace(/\.md$/, '').replace(/\//g, '-').toLowerCase();

    // Extract title from first heading or filename
    const titleMatch = body.match(/^#\s+(.+)$/m);
    const title = frontmatter.title || (titleMatch ? titleMatch[1] : path.basename(filePath, '.md'));

    // Extract tags from content
    const tags = this.extractTags(body, filePath);

    return {
      id,
      title,
      path: relativePath,
      content: body,
      category: 'docs',
      tags,
    };
  }

  private extractTags(content: string, filePath: string): string[] {
    const tags: string[] = [];
    const filename = path.basename(filePath, '.md').toLowerCase();

    // Add filename as tag
    tags.push(filename);

    // Extract mentioned class names
    const classMatches = content.match(/`([A-Z][a-zA-Z]+)`/g);
    if (classMatches) {
      for (const match of classMatches.slice(0, 20)) {
        tags.push(match.replace(/`/g, '').toLowerCase());
      }
    }

    // Add category tags based on content
    if (content.includes('migration') || filename.includes('migration')) {
      tags.push('migration', 'upgrade');
    }
    if (content.includes('scheduler') || content.includes('Scheduler')) {
      tags.push('scheduler', 'resource');
    }
    if (content.includes('entry') || content.includes('Entry')) {
      tags.push('entry', 'event');
    }

    return [...new Set(tags)];
  }

  private extractCodeBlocksFromMarkdown(filePath: string): CodeExample[] {
    const content = fs.readFileSync(filePath, 'utf-8');
    const examples: CodeExample[] = [];

    // Match code blocks with optional language and preceding heading
    const codeBlockRegex = /(?:#{2,3}\s+([^\n]+)\n)?(?:[^\n]*\n)*?```(java|typescript|ts)?\n([\s\S]*?)```/g;

    let match;
    let index = 0;

    while ((match = codeBlockRegex.exec(content)) !== null) {
      const [, heading, language, code] = match;

      if (!code.trim()) continue;

      // Find the nearest heading before this code block
      const beforeContent = content.slice(0, match.index);
      const headingMatch = beforeContent.match(/#{2,4}\s+([^\n]+)\n[^#]*$/);
      const title = heading || headingMatch?.[1] || `Example ${index + 1}`;

      examples.push({
        id: `samples-${index}`,
        title: title.trim(),
        description: this.extractDescription(beforeContent, match.index),
        code: code.trim(),
        language: (language === 'ts' ? 'typescript' : language || 'java') as 'java' | 'typescript',
        category: this.categorizeExample(title, code),
        tags: this.extractExampleTags(title, code),
      });

      index++;
    }

    return examples;
  }

  private extractDescription(beforeContent: string, position: number): string {
    // Get the text between the last heading and the code block
    const lines = beforeContent.split('\n').slice(-10);
    const description = lines
      .filter(line => !line.startsWith('#') && line.trim())
      .join(' ')
      .trim();

    return description.slice(0, 300);
  }

  private categorizeExample(title: string, code: string): string {
    const titleLower = title.toLowerCase();
    const codeLower = code.toLowerCase();

    if (titleLower.includes('provider') || codeLower.includes('entryprovider')) return 'data-provider';
    if (titleLower.includes('event') || codeLower.includes('listener')) return 'events';
    if (titleLower.includes('resource') || codeLower.includes('resource')) return 'scheduler';
    if (titleLower.includes('style') || codeLower.includes('addclassname')) return 'styling';
    if (titleLower.includes('timezone') || codeLower.includes('timezone')) return 'timezone';
    if (titleLower.includes('recurring') || codeLower.includes('recurring')) return 'recurring';
    if (titleLower.includes('view') || codeLower.includes('calendarview')) return 'views';

    return 'general';
  }

  private extractExampleTags(title: string, code: string): string[] {
    const tags: string[] = [];
    const combined = (title + ' ' + code).toLowerCase();

    const keywords = [
      'entry', 'provider', 'callback', 'inmemory', 'resource', 'scheduler',
      'timezone', 'recurring', 'event', 'listener', 'click', 'drop', 'resize',
      'style', 'theme', 'view', 'builder', 'crud', 'create', 'update', 'delete',
    ];

    for (const keyword of keywords) {
      if (combined.includes(keyword)) {
        tags.push(keyword);
      }
    }

    return tags;
  }

  private async extractFromDemoSources(): Promise<CodeExample[]> {
    const examples: CodeExample[] = [];
    const demoDir = path.join(this.demoRoot, 'src/main/java/org/vaadin/stefan/ui/view/demos');

    if (!fs.existsSync(demoDir)) {
      return examples;
    }

    const javaFiles = this.findJavaFiles(demoDir);

    for (const file of javaFiles) {
      try {
        const content = fs.readFileSync(file, 'utf-8');
        const className = path.basename(file, '.java');

        // Extract class-level Javadoc
        const javadocMatch = content.match(/\/\*\*\s*([\s\S]*?)\s*\*\/\s*(?:@\w+[^\n]*\n\s*)*public\s+class/);
        const description = javadocMatch
          ? javadocMatch[1].replace(/^\s*\*\s?/gm, '').trim()
          : `Demo: ${className}`;

        examples.push({
          id: `demo-${className.toLowerCase()}`,
          title: this.humanizeClassName(className),
          description,
          code: content,
          language: 'java',
          category: this.categorizeExample(className, content),
          tags: this.extractExampleTags(className, content),
        });
      } catch (error) {
        console.error(`Error extracting demo ${file}:`, error);
      }
    }

    return examples;
  }

  private findJavaFiles(dir: string): string[] {
    const files: string[] = [];
    const entries = fs.readdirSync(dir, { withFileTypes: true });

    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        files.push(...this.findJavaFiles(fullPath));
      } else if (entry.name.endsWith('.java') && entry.name.includes('Demo')) {
        files.push(fullPath);
      }
    }

    return files;
  }

  private humanizeClassName(className: string): string {
    return className
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }
}
