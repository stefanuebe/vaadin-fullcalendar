import * as fs from 'fs';
import * as path from 'path';
import type { JavaClass, JavaMethod, JavaField, JavaConstructor, JavaParameter } from '../types.js';

/**
 * Extracts Java API documentation from source files.
 * Uses line-by-line parsing for robustness with large files.
 */
export class JavaExtractor {
  private sourceRoots: string[];

  constructor(sourceRoots: string[]) {
    this.sourceRoots = sourceRoots;
  }

  async extractAll(): Promise<JavaClass[]> {
    const classes: JavaClass[] = [];

    for (const root of this.sourceRoots) {
      const source = root.includes('scheduler') ? 'addon-scheduler' : 'addon';
      const javaFiles = this.findJavaFiles(root);

      for (const file of javaFiles) {
        try {
          const parsed = await this.parseJavaFile(file, source);
          if (parsed && parsed.isPublic) {
            classes.push(parsed);
          }
        } catch (error) {
          console.error(`Error parsing ${file}:`, error);
        }
      }
    }

    return classes;
  }

  private findJavaFiles(dir: string): string[] {
    const files: string[] = [];

    if (!fs.existsSync(dir)) {
      return files;
    }

    const entries = fs.readdirSync(dir, { withFileTypes: true });

    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        files.push(...this.findJavaFiles(fullPath));
      } else if (entry.name.endsWith('.java')) {
        files.push(fullPath);
      }
    }

    return files;
  }

  private async parseJavaFile(filePath: string, source: 'addon' | 'addon-scheduler'): Promise<JavaClass | null> {
    const content = fs.readFileSync(filePath, 'utf-8');
    const lines = content.split('\n');

    // Extract package name
    let packageName = '';
    for (const line of lines) {
      const match = line.match(/^package\s+([\w.]+);/);
      if (match) {
        packageName = match[1];
        break;
      }
    }
    if (!packageName) return null;

    // Find class declaration
    let className = '';
    let classType: 'class' | 'interface' | 'enum' = 'class';
    let extendsClause = '';
    let implementsClause: string[] = [];
    let classLineIndex = -1;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      if (line.match(/^\s*public\s+(abstract\s+)?(final\s+)?(class|interface|enum)\s+/)) {
        const typeMatch = line.match(/(class|interface|enum)\s+(\w+)/);
        if (typeMatch) {
          classType = typeMatch[1] as 'class' | 'interface' | 'enum';
          className = typeMatch[2];
          classLineIndex = i;

          // Check for extends
          const extendsMatch = line.match(/extends\s+([\w<>.,\s]+?)(?:\s+implements|\s*\{|$)/);
          if (extendsMatch) {
            extendsClause = extendsMatch[1].trim();
          }

          // Check for implements
          const implementsMatch = line.match(/implements\s+([\w<>.,\s]+?)(?:\s*\{|$)/);
          if (implementsMatch) {
            implementsClause = implementsMatch[1].split(',').map(s => s.trim());
          }
          break;
        }
      }
    }

    if (!className) return null;

    // Extract class Javadoc (look backwards from class declaration)
    const classJavadoc = this.extractJavadocBeforeLine(lines, classLineIndex);

    // Extract annotations before class
    const annotations = this.extractAnnotationsBeforeLine(lines, classLineIndex);

    const javaClass: JavaClass = {
      name: className,
      packageName,
      fullName: `${packageName}.${className}`,
      description: classJavadoc,
      type: classType,
      extends: extendsClause || undefined,
      implements: implementsClause.length > 0 ? implementsClause : undefined,
      methods: [],
      fields: [],
      constructors: [],
      annotations,
      isPublic: true,
      source,
    };

    // Extract methods (simplified approach for large files)
    javaClass.methods = this.extractMethodsSimple(lines, className);

    // Extract public fields
    javaClass.fields = this.extractFieldsSimple(lines);

    // Extract constructors
    javaClass.constructors = this.extractConstructorsSimple(lines, className);

    return javaClass;
  }

  private extractJavadocBeforeLine(lines: string[], lineIndex: number): string {
    if (lineIndex <= 0) return '';

    // Find end of Javadoc (line before class/annotations)
    let endLine = lineIndex - 1;
    while (endLine >= 0 && lines[endLine].trim().startsWith('@')) {
      endLine--;
    }

    if (endLine < 0 || !lines[endLine].trim().endsWith('*/')) return '';

    // Find start of Javadoc
    let startLine = endLine;
    while (startLine >= 0) {
      if (lines[startLine].includes('/**')) break;
      startLine--;
    }

    if (startLine < 0) return '';

    // Extract and clean Javadoc
    const javadocLines = lines.slice(startLine, endLine + 1);
    return this.cleanJavadoc(javadocLines.join('\n'));
  }

  private cleanJavadoc(javadoc: string): string {
    return javadoc
      .replace(/\/\*\*\s*/g, '')
      .replace(/\s*\*\//g, '')
      .split('\n')
      .map(line => line.replace(/^\s*\*\s?/, ''))
      .filter(line => !line.trim().startsWith('@param'))
      .filter(line => !line.trim().startsWith('@return'))
      .filter(line => !line.trim().startsWith('@throws'))
      .filter(line => !line.trim().startsWith('@see'))
      .filter(line => !line.trim().startsWith('@since'))
      .join('\n')
      .replace(/{@link\s+([^}]+)}/g, '$1')
      .replace(/{@code\s+([^}]+)}/g, '`$1`')
      .trim()
      .slice(0, 500); // Limit description length
  }

  private extractAnnotationsBeforeLine(lines: string[], lineIndex: number): string[] {
    const annotations: string[] = [];
    let i = lineIndex - 1;

    while (i >= 0 && i > lineIndex - 20) {
      const line = lines[i].trim();
      if (line.startsWith('@')) {
        const match = line.match(/@(\w+)/);
        if (match) annotations.push(match[1]);
      } else if (line.endsWith('*/') || (!line.startsWith('*') && line.length > 0 && !line.startsWith('//'))) {
        break;
      }
      i--;
    }

    return annotations;
  }

  private extractMethodsSimple(lines: string[], className: string): JavaMethod[] {
    const methods: JavaMethod[] = [];
    let inJavadoc = false;
    let javadocLines: string[] = [];
    let annotations: string[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const trimmed = line.trim();

      // Track Javadoc
      if (trimmed.startsWith('/**')) {
        inJavadoc = true;
        javadocLines = [trimmed];
        continue;
      }
      if (inJavadoc) {
        javadocLines.push(trimmed);
        if (trimmed.endsWith('*/')) {
          inJavadoc = false;
        }
        continue;
      }

      // Track annotations
      if (trimmed.startsWith('@')) {
        const match = trimmed.match(/@(\w+)/);
        if (match) annotations.push(match[1]);
        continue;
      }

      // Match public method (but not constructor)
      const methodMatch = line.match(/^\s+public\s+(static\s+)?([\w<>[\],\s.?]+)\s+(\w+)\s*\(([^)]*)\)/);
      if (methodMatch) {
        const [, staticMod, returnType, name, params] = methodMatch;

        // Skip if it looks like a constructor (return type equals class name or is missing)
        if (name !== className && returnType.trim() !== className) {
          methods.push({
            name,
            description: this.cleanJavadoc(javadocLines.join('\n')),
            returnType: returnType.trim(),
            parameters: this.parseParametersSimple(params),
            annotations: [...annotations],
            isPublic: true,
            isStatic: !!staticMod,
          });
        }

        // Reset for next method
        javadocLines = [];
        annotations = [];
      } else if (!trimmed.startsWith('//') && trimmed.length > 0) {
        // Non-annotation, non-comment, non-empty line - reset tracking
        if (!trimmed.startsWith('*') && !trimmed.startsWith('/*')) {
          javadocLines = [];
          annotations = [];
        }
      }
    }

    // Limit to first 50 methods to avoid huge outputs
    return methods.slice(0, 50);
  }

  private extractFieldsSimple(lines: string[]): JavaField[] {
    const fields: JavaField[] = [];

    for (const line of lines) {
      const match = line.match(/^\s+public\s+(static\s+)?(final\s+)?([\w<>[\],\s.?]+)\s+(\w+)\s*[=;]/);
      if (match) {
        const [, staticMod, finalMod, type, name] = match;
        fields.push({
          name,
          type: type.trim(),
          description: '',
          annotations: [],
          isPublic: true,
          isStatic: !!staticMod,
          isFinal: !!finalMod,
        });
      }
    }

    return fields;
  }

  private extractConstructorsSimple(lines: string[], className: string): JavaConstructor[] {
    const constructors: JavaConstructor[] = [];
    let inJavadoc = false;
    let javadocLines: string[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const trimmed = line.trim();

      // Track Javadoc
      if (trimmed.startsWith('/**')) {
        inJavadoc = true;
        javadocLines = [trimmed];
        continue;
      }
      if (inJavadoc) {
        javadocLines.push(trimmed);
        if (trimmed.endsWith('*/')) {
          inJavadoc = false;
        }
        continue;
      }

      // Match constructor
      const constructorPattern = new RegExp(`^\\s+public\\s+${className}\\s*\\(([^)]*)\\)`);
      const match = line.match(constructorPattern);
      if (match) {
        constructors.push({
          description: this.cleanJavadoc(javadocLines.join('\n')),
          parameters: this.parseParametersSimple(match[1]),
          annotations: [],
          isPublic: true,
        });
        javadocLines = [];
      }
    }

    return constructors;
  }

  private parseParametersSimple(paramsString: string): JavaParameter[] {
    if (!paramsString.trim()) return [];

    const params: JavaParameter[] = [];
    let current = '';
    let depth = 0;

    for (const char of paramsString) {
      if (char === '<') depth++;
      else if (char === '>') depth--;
      else if (char === ',' && depth === 0) {
        this.addParameter(current, params);
        current = '';
        continue;
      }
      current += char;
    }

    if (current.trim()) {
      this.addParameter(current, params);
    }

    return params;
  }

  private addParameter(paramStr: string, params: JavaParameter[]): void {
    const trimmed = paramStr.trim().replace(/@\w+\s*/g, ''); // Remove annotations
    const lastSpace = trimmed.lastIndexOf(' ');

    if (lastSpace > 0) {
      params.push({
        type: trimmed.slice(0, lastSpace).trim(),
        name: trimmed.slice(lastSpace + 1).trim(),
      });
    }
  }
}
