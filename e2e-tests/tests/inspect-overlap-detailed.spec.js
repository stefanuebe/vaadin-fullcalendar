import { test } from '@playwright/test';

test('detailed constraint overlap visibility inspection', async ({ page }) => {
  console.log('Navigating to constraint-overlap...');
  await page.goto('http://localhost:8082/test/constraint-overlap');
  
  console.log('Waiting 12 seconds for full load...');
  await page.waitForTimeout(12000);
  
  console.log('Detailed DOM inspection...');
  const result = await page.evaluate(() => {
    // Get the first vfc-event
    const firstEvent = document.querySelector('.vfc-event');
    if (!firstEvent) {
      return { error: 'No .vfc-event found' };
    }

    let current = firstEvent;
    let depth = 0;
    let visibilityHiddenParent = null;
    const pathDetails = [];

    // Walk up the tree to find visibility:hidden
    while (current && current !== document.body && depth < 20) {
      const style = window.getComputedStyle(current);
      const visibility = style.visibility;
      const display = style.display;
      const inlineStyle = current.getAttribute('style') || '';
      
      pathDetails.push({
        depth: depth,
        tag: current.tagName,
        class: current.className,
        id: current.id,
        dataAttributes: Array.from(current.attributes)
          .filter(attr => attr.name.startsWith('data-'))
          .map(attr => `${attr.name}=${attr.value}`)
          .join('; '),
        computedVisibility: visibility,
        computedDisplay: display,
        inlineStyleAttr: inlineStyle.substring(0, 200),
        hasVisibilityInline: inlineStyle.includes('visibility'),
        hasDisplayInline: inlineStyle.includes('display')
      });

      if (visibility === 'hidden' && !visibilityHiddenParent) {
        visibilityHiddenParent = {
          tag: current.tagName,
          class: current.className,
          id: current.id,
          inlineStyle: inlineStyle
        };
      }

      current = current.parentElement;
      depth++;
    }

    // Also check computed styles to see if any parent explicitly sets visibility:hidden
    current = firstEvent;
    let visibilityHiddenReason = null;
    while (current && current !== document.body) {
      const style = window.getComputedStyle(current);
      if (style.visibility === 'hidden') {
        visibilityHiddenReason = {
          element: current.tagName + '.' + current.className,
          id: current.id,
          computedVisibility: style.visibility,
          // Try to get the original style property
          styleAttr: current.getAttribute('style')
        };
        break;
      }
      current = current.parentElement;
    }

    return {
      firstEventDomPath: pathDetails,
      visibilityHiddenParent: visibilityHiddenParent,
      visibilityHiddenReason: visibilityHiddenReason,
      allEvents: Array.from(document.querySelectorAll('.vfc-event')).length
    };
  });
  
  console.log('\n=== DETAILED DOM PATH ===');
  console.log(JSON.stringify(result, null, 2));
});
