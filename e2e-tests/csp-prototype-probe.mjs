import { createRequire } from 'module'; const { chromium } = createRequire(import.meta.url)('@playwright/test');
// PROTOTYPE #479: node e2e-tests/csp-prototype-probe.mjs "http://localhost:8082/test/csp-prototype?noAutoId"
const url = process.argv[2];
const browser = await chromium.launch();
const page = await browser.newPage();
const logs = [];
page.on('console', m => logs.push(`[${m.type()}] ${m.text()}`));
page.on('pageerror', e => logs.push(`[pageerror] ${e.message}`));
const resp = await page.goto(url);
console.log('CSP header:', resp.headers()['content-security-policy'] ?? '(none)');
await page.waitForTimeout(4000);
const r = await page.evaluate(() => ({
  proto: window.__cspProto ?? null,
  events: document.querySelectorAll('.fc-event').length,
  appClass: [...document.querySelectorAll('.fc-event')].map(e => [...e.classList].filter(c => c.startsWith('csp-'))),
  sundayCells: document.querySelectorAll('.csp-addon-sunday').length,
  eventIds: [...document.querySelectorAll('.fc-event')].map(e => e.id),
  calendarRendered: !!document.querySelector('.fc'),
}));
console.log(JSON.stringify(r, null, 1));
console.log(logs.join('\n'));
await browser.close();
