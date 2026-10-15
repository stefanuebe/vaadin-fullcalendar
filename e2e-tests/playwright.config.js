// @ts-check
const { defineConfig, devices } = require('@playwright/test');

/**
 * Playwright configuration for FullCalendar Vaadin Flow integration tests
 * @see https://playwright.dev/docs/test-configuration
 */
module.exports = defineConfig({
  testDir: './tests',

  /* Run tests in files in parallel */
  fullyParallel: true,

  /* Fail the build on CI if you accidentally left test.only in the source code */
  forbidOnly: !!process.env.CI,

  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,

  /* Half the CPU cores, also on CI. Each test gets its own browser context and so its own Vaadin session. */
  workers: '50%',

  /* Reporter to use */
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['junit', { outputFile: 'test-results/junit-results.xml' }],
    ['list']
  ],

  /* Shared settings for all the projects below */
  use: {
    /* Base URL to use in actions like `await page.goto('/')` */
    baseURL: process.env.BASE_URL || 'http://localhost:8082',

    /* Collect trace when retrying the failed test */
    trace: 'on-first-retry',

    /* Screenshot on failure */
    screenshot: 'only-on-failure',

    /* Video on failure */
    video: 'on-first-retry',

    /* A click or fill on a missing element fails after this, not after the whole test timeout */
    actionTimeout: 5000,

    /* The calendar shows the browser's time zone by default, so the tests do not depend on the machine's zone. A
       test about time zones sets its own. */
    timezoneId: 'UTC',
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    // Uncomment to add more browsers
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },
  ],

  /* Timeout settings. The slowest passing test takes about 7s, so a broken test fails fast
     instead of hanging for a minute per retry. */
  timeout: 20000,
  expect: {
    timeout: 5000
  },

  /* Output directory for test artifacts */
  outputDir: 'test-results/',
});
