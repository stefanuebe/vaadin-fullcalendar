#!/usr/bin/env tsx
/**
 * Extraction script - runs at build time to extract all data from source files.
 * Output is stored as JSON for the MCP server to load at runtime.
 */

import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';
import { JavaExtractor } from './java-extractor.js';
import { DocsExtractor } from './docs-extractor.js';
import { ModelExtractor } from './model-extractor.js';
import type { ExtractedData } from '../types.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Paths relative to project root (parent of mcp-server)
const PROJECT_ROOT = process.env.PROJECT_ROOT || path.resolve(__dirname, '../../..');
const OUTPUT_DIR = path.resolve(__dirname, '../../data');

async function extractVersion(): Promise<string> {
  const pomPath = path.join(PROJECT_ROOT, 'addon/pom.xml');

  if (fs.existsSync(pomPath)) {
    const content = fs.readFileSync(pomPath, 'utf-8');
    const versionMatch = content.match(/<version>([^<]+)<\/version>/);
    if (versionMatch) {
      return versionMatch[1];
    }
  }

  return 'unknown';
}

async function main() {
  console.log('Starting extraction...');
  console.log(`Project root: ${PROJECT_ROOT}`);
  console.log(`Output directory: ${OUTPUT_DIR}`);

  // Ensure output directory exists
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });

  // Initialize extractors
  const javaExtractor = new JavaExtractor([
    path.join(PROJECT_ROOT, 'addon/src/main/java'),
    path.join(PROJECT_ROOT, 'addon-scheduler/src/main/java'),
  ]);

  const docsExtractor = new DocsExtractor(
    path.join(PROJECT_ROOT, 'docs'),
    path.join(PROJECT_ROOT, 'demo')
  );

  const modelExtractor = new ModelExtractor(
    path.join(PROJECT_ROOT, 'addon'),
    path.join(PROJECT_ROOT, 'addon-scheduler')
  );

  // Run extractions
  console.log('Extracting Java API...');
  const classes = await javaExtractor.extractAll();
  console.log(`  Found ${classes.length} classes`);

  console.log('Extracting documentation...');
  const documentation = await docsExtractor.extractDocumentation();
  console.log(`  Found ${documentation.length} documentation pages`);

  console.log('Extracting code examples...');
  const examples = await docsExtractor.extractCodeExamples();
  console.log(`  Found ${examples.length} code examples`);

  console.log('Extracting Entry properties...');
  const entryProperties = await modelExtractor.extractEntryProperties();
  console.log(`  Found ${entryProperties.length} properties`);

  console.log('Extracting calendar views...');
  const calendarViews = await modelExtractor.extractCalendarViews();
  console.log(`  Found ${calendarViews.length} views`);

  console.log('Extracting event types...');
  const eventTypes = await modelExtractor.extractEventTypes();
  console.log(`  Found ${eventTypes.length} event types`);

  const version = await extractVersion();
  console.log(`Version: ${version}`);

  // Compile extracted data
  const extractedData: ExtractedData = {
    documentation,
    classes,
    examples,
    entryProperties,
    calendarViews,
    eventTypes,
    version,
    extractedAt: new Date().toISOString(),
  };

  // Write to output file
  const outputPath = path.join(OUTPUT_DIR, 'extracted.json');
  fs.writeFileSync(outputPath, JSON.stringify(extractedData, null, 2));
  console.log(`\nExtracted data written to: ${outputPath}`);

  // Write summary
  const summary = {
    version,
    extractedAt: extractedData.extractedAt,
    counts: {
      documentation: documentation.length,
      classes: classes.length,
      examples: examples.length,
      entryProperties: entryProperties.length,
      calendarViews: calendarViews.length,
      eventTypes: eventTypes.length,
    },
  };

  const summaryPath = path.join(OUTPUT_DIR, 'summary.json');
  fs.writeFileSync(summaryPath, JSON.stringify(summary, null, 2));
  console.log(`Summary written to: ${summaryPath}`);

  console.log('\nExtraction complete!');
}

main().catch(error => {
  console.error('Extraction failed:', error);
  process.exit(1);
});
