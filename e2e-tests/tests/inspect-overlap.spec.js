import { test } from '@playwright/test';

test('inspect constraint overlap events', async ({ page }) => {
  console.log('Navigating to constraint-overlap...');
  await page.goto('http://localhost:8082/test/constraint-overlap');
  
  console.log('Waiting 12 seconds for full load...');
  await page.waitForTimeout(12000);
  
  console.log('Taking screenshot...');
  await page.screenshot({ path: './.claude/screenshots/constraint-overlap.png', fullPage: true });
  
  console.log('Inspecting event visibility...');
  const result = await page.evaluate(() => {
    // First, get visibility/display/opacity for each event
    const events = Array.from(document.querySelectorAll('.vfc-event')).map(e => ({
      title: e.textContent.trim().substring(0, 20),
      visibility: window.getComputedStyle(e).visibility,
      display: window.getComputedStyle(e).display,
      opacity: window.getComputedStyle(e).opacity,
      element: e.className
    }));

    // Find first event with visibility !== 'visible'
    const hiddenEvent = events.find(e => e.visibility !== 'visible');
    
    let domPath = [];
    if (hiddenEvent) {
      const eventElement = document.querySelector('.vfc-event');
      let current = eventElement;
      
      while (current && current !== document.body) {
        const style = window.getComputedStyle(current);
        const visibility = style.visibility;
        const display = style.display;
        
        domPath.push({
          tag: current.tagName,
          class: current.className,
          id: current.id,
          visibility: visibility,
          display: display,
          hasStyleAttr: current.hasAttribute('style'),
          styleContent: current.getAttribute('style') ? current.getAttribute('style').substring(0, 150) : ''
        });
        
        if (visibility === 'hidden') {
          break;
        }
        
        current = current.parentElement;
      }
    }

    return {
      allEvents: events,
      firstHiddenEvent: hiddenEvent,
      domPathUpFromFirstEvent: domPath,
      totalEventCount: events.length
    };
  });
  
  console.log('\n=== VISIBILITY ANALYSIS RESULT ===');
  console.log(JSON.stringify(result, null, 2));
});
