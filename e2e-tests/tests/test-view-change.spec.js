import { test, expect } from '@playwright/test';

test('evaluate view change from dayGridMonth to timeGridWeek', async ({ page }) => {
  // Navigate to localhost:8082
  await page.goto('http://localhost:8082', { waitUntil: 'domcontentloaded', timeout: 30000 });
  
  // Wait for .vfc-view to appear
  await page.waitForSelector('.vfc-view', { timeout: 30000 });
  
  // Evaluate: check if we're in dayGridMonth
  const initialViewClass = await page.evaluate(() => {
    return document.querySelector('.vfc-view-dayGridMonth')?.className || 'NOT_FOUND';
  });
  console.log('Initial view class (dayGridMonth):', initialViewClass);
  
  // Get all button text
  const buttonTexts = await page.evaluate(() => {
    const allButtons = Array.from(document.querySelectorAll('button'));
    return allButtons.map(b => b.textContent?.trim());
  });
  console.log('All button texts:', buttonTexts);
  
  // Try to find and click "Time Grid Week" button
  const timeGridWeekButtons = await page.locator('button:has-text("Time Grid Week")').all();
  if (timeGridWeekButtons.length > 0) {
    console.log('Found Time Grid Week button, clicking...');
    await timeGridWeekButtons[0].click();
    await page.waitForTimeout(3000);
  } else {
    console.log('Time Grid Week button not found');
  }
  
  // Evaluate 1: new view class
  const newViewClass = await page.evaluate(() => {
    return document.querySelector('.vfc-view')?.className || 'NOT_FOUND';
  });
  console.log('1. New view class:', newViewClass);
  
  // Evaluate 2: count of events
  const eventCount = await page.evaluate(() => {
    return document.querySelectorAll('.vfc-event').length;
  });
  console.log('2. Event count:', eventCount);
  
  // Evaluate 3: visibility of events
  const eventVisibility = await page.evaluate(() => {
    return Array.from(document.querySelectorAll('.vfc-event')).map(e => window.getComputedStyle(e).visibility).slice(0, 3);
  });
  console.log('3. Event visibility (first 3):', eventVisibility);
});
