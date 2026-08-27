# Selenium + Java Automation Practise

A single-file learning project that spins up a real Chrome browser and walks
through the Java syntax and Selenium WebDriver APIs you'll use in day-to-day
automation work. Everything runs against
[the-internet.herokuapp.com](https://the-internet.herokuapp.com) — a public
sandbox built specifically for practising automation, so it's safe to click
around and re-run as often as you like.

## Prerequisites

| Tool   | Version used here | Check with     |
|--------|--------------------|-----------------|
| JDK    | 21                 | `java -version` |
| Maven  | 3.9.x              | `mvn -version`  |
| Chrome | any recent version | already installed at `C:\Program Files\Google\Chrome\Application\chrome.exe` |

You do **not** need to download `chromedriver` yourself — `WebDriverManager`
detects your installed Chrome version and fetches the matching driver
automatically the first time you run the project.

## Project structure

```
AUtomation_Practise/
├── pom.xml                                  # Maven build file (dependencies + plugins)
├── docs/
│   └── WebDriver-Methods-Reference.md       # every driver.* call used here, by category
├── src/main/java/com/practise/
│   ├── automation/
│   │   ├── SeleniumSyntaxPlayground.java    # the main script - see walkthrough below
│   │   └── DriverSetup.java                 # shared createDriver() + a small smoke script
│   └── algorithms/
│       └── MaxWordsInSentence.java          # plain-Java string exercise, no browser
├── src/test/java/com/practise/automation/
│   └── SauceDemoCheckoutTest.java           # TestNG end-to-end checkout scenario
├── .vscode/
│   └── launch.json                          # F5 debug config for VS Code
└── target/                                  # build output (created by Maven)
    └── screenshots/final-state.png          # written by the script on each run
```

## Dependencies (`pom.xml`)

| Dependency | Purpose |
|---|---|
| `org.seleniumhq.selenium:selenium-java` | The core Selenium WebDriver API — everything under `org.openqa.selenium.*` |
| `io.github.bonigarcia:webdrivermanager` | Detects your local Chrome version and downloads the matching `chromedriver.exe` automatically, so you never manage driver binaries by hand |
| `org.testng:testng` | `@Test` methods, lifecycle annotations, and `Assert.*` — used by `SauceDemoCheckoutTest` under `src/test/java`, and run via `mvn test`. The playground script itself deliberately stays a plain `main()` for simplicity. |

Build plugins:
- **maven-compiler-plugin** — compiles against Java 21.
- **exec-maven-plugin** — lets you run the script with `mvn exec:java` instead of wiring up a classpath by hand.
- **maven-surefire-plugin** — will pick up TestNG `@Test` classes if you add any later.

## How to run

From the project root:

```powershell
mvn exec:java
```

This compiles the project (if needed) and runs
`com.practise.automation.SeleniumSyntaxPlayground#main`. A visible Chrome
window will open and drive itself through every demo; console output is
grouped into `====` banners, one per topic. It takes roughly 30–40 seconds
end to end and finishes with exit code `0`.

To run headless (no visible window — useful in CI or when you just want the
console output), open the file and uncomment this line inside `createDriver()`:

```java
options.addArguments("--headless=new");
```

### Running from an IDE

- **VS Code**: open `SeleniumSyntaxPlayground.java`, then either click the
  `Run` code-lens above `main`, or press **F5** to use the
  `Debug SeleniumSyntaxPlayground` launch config in `.vscode/launch.json`
  (requires the "Extension Pack for Java").
- **IntelliJ IDEA**: right-click `SeleniumSyntaxPlayground.java` → *Run
  'SeleniumSyntaxPlayground.main()'*. IntelliJ resolves the Maven classpath
  automatically.

## The other classes in this repo

`mvn exec:java` only runs the playground. These three classes are separate,
independently runnable entry points:

### `DriverSetup` (`src/main/java/.../automation/DriverSetup.java`)

Holds the shared `public static WebDriver createDriver()` that
`SauceDemoCheckoutTest` reuses, so driver setup lives in exactly one place.
Its own `main()` is a smoke test against
[datatables.net](https://datatables.net/examples/index): it opens the *Zero
configuration* example, parses the "Showing 1 to N of X entries" label out of
`#example_info` to assert there are at least 25 records, then clicks the first
numeric column header twice to sort it descending and asserts the top row's
Age is 65 or more. Run it to confirm your JDK/Maven/Chrome setup works before
touching anything else:

```powershell
mvn exec:java "-Dexec.mainClass=com.practise.automation.DriverSetup"
```

Its `WebDriverWait` is only 2 seconds — on a slow connection, raise
`Duration.ofSeconds(2)` rather than assuming the locators are broken.

### `SauceDemoCheckoutTest` (`src/test/java/.../automation/SauceDemoCheckoutTest.java`)

A ~10-step TestNG scenario on [saucedemo.com](https://www.saucedemo.com/):
log in as `standard_user`, add the Sauce Labs Backpack, assert the cart badge
reads `1`, fill in checkout details, finish the order, and assert the
"Thank you for your order!" header. `@BeforeClass` builds the driver via
`DriverSetup.createDriver()`; `@AfterClass` always quits it. Run it with:

```powershell
mvn test
```

One gotcha it already works around: saucedemo's checkout form is a SPA, and
the postal-code field's value can lag behind the keystrokes, so `Continue`
silently no-ops. The test waits on
`d -> "00000".equals(...getAttribute("value"))` before clicking — a useful
pattern for any React/Vue form that re-renders while you type.

### `MaxWordsInSentence` (`src/main/java/.../algorithms/MaxWordsInSentence.java`)

No browser involved. `maxWords(String)` splits on `[.!?]`, trims each
fragment, skips empty ones, and returns the largest whitespace-delimited word
count. Run it with:

```powershell
mvn exec:java "-Dexec.mainClass=com.practise.algorithms.MaxWordsInSentence"
```

## How to debug

### Setting breakpoints

Click in the gutter next to any line — good first breakpoints to try:
- `SeleniumSyntaxPlayground.java:35` (`driver = createDriver();`) — step
  into `createDriver()` to see how `WebDriverManager` and `ChromeOptions`
  are wired up.
- Line 370 in `demoTabsAndWindowSwitching` — pause right after the new tab
  opens and inspect `driver.getWindowHandles()` in the Variables/Watch pane
  to see the handle strings.
- Any `wait.until(...)` call — step over it and inspect the returned
  `WebElement` (`.getText()`, `.getAttribute("...")`, `.isDisplayed()`) in
  the debugger's Evaluate Expression panel.

While paused, the Chrome window stays open exactly as it was at that
instant — this is the fastest way to understand what a wait or a frame
switch actually did to the page.

### VS Code

1. Set a breakpoint (click the gutter).
2. Press **F5** (uses `.vscode/launch.json`).
3. Use the Debug toolbar to Step Over (F10) / Step Into (F11) / Continue (F5).
4. Hover any variable, or use the **Debug Console** to evaluate expressions
   like `driver.getTitle()` or `checkboxes.size()` while paused.

### IntelliJ IDEA

1. Set a breakpoint.
2. Right-click the file → *Debug 'SeleniumSyntaxPlayground.main()'*.
3. Same Step Over/Into/Continue controls; the Variables and Watches panes
   work the same way.

### Command-line debugging (no IDE)

Maven ships with `mvnDebug`, which starts the JVM with the debug agent
already enabled and suspended, waiting for a debugger to attach:

```powershell
mvnDebug exec:java
```

It prints `Listening for transport dt_socket at address: 8000` and pauses.
In VS Code / IntelliJ, create a **Remote JVM Attach** configuration pointed
at `localhost:8000`, then start it — execution resumes once the debugger
attaches, and your breakpoints will hit normally.

### Turning on Selenium's own logging

If a locator can't be found or a wait times out, extra WebDriver logging
often explains why (element covered by another element, page not fully
loaded, etc.):

```powershell
mvn exec:java "-Dwebdriver.chrome.verboseLogging=true"
```

### Inspecting failures

Every method prints a `====` banner before it runs, so a stack trace tells
you exactly which demo failed from the last banner printed above it. Common
exceptions you'll see while experimenting, and what they mean:

| Exception | Typical cause |
|---|---|
| `NoSuchElementException` | Locator didn't match anything — page hadn't loaded yet, or the selector is wrong. Add/extend a `WebDriverWait`. |
| `StaleElementReferenceException` | You navigated or the DOM re-rendered after finding the element but before using it. Re-find it. |
| `ElementClickInterceptedException` | Something (an overlay, alert, animation) is covering the element. Wait for `elementToBeClickable`, or dismiss the blocker first. |
| `TimeoutException` from `WebDriverWait` | The condition never became true within the timeout — verify the locator/condition in the browser DevTools first. |

## Code walkthrough

The whole script lives in one class, `SeleniumSyntaxPlayground`, split into
small `private static` methods so each Selenium concept is isolated and easy
to jump to. `main()` just calls them in order, wrapped in a
`try { ... } finally { driver.quit(); }` so the browser process is always
cleaned up even if a demo throws.

| Method | What it teaches |
|---|---|
| `javaCoreSyntaxDemo()` | Plain Java, no browser: `if`/`else if`/`else`, ternary operator, classic `for`, enhanced `for`-each, `while`, `do-while`, classic `switch` + modern arrow `switch` expression, `ArrayList`/`List`, `HashMap`, `String.split/charAt/substring/trim/replace/toUpperCase/toLowerCase`, `StringBuilder`, `Arrays.sort`, `try/catch/finally`. |
| `countVowels(String)` | A small custom method (loop + `if` + `charAt` + `return`) called from the warm-up. |
| `createDriver()` | `WebDriverManager.chromedriver().setup()` (auto driver management) + `ChromeOptions` + constructing a `ChromeDriver`. |
| `demoNavigationAndLocators()` | `driver.get(url)`, `By.tagName`, `findElements` → `List<WebElement>`, converting a `List` to an array, `getCurrentUrl()` + `String.split("/")`, `getTitle()` + `charAt(0)`. |
| `demoExplicitAndFluentWaits()` | Implicit wait (`manage().timeouts().implicitlyWait`), explicit wait (`WebDriverWait` + `ExpectedConditions`), and `FluentWait` with a custom polling interval and an ignored-exceptions list. |
| `demoLoginAndKeyboardActions()` | `By.id`, `sendKeys`/`clear`, `Keys.TAB` to move focus, `switchTo().activeElement()`, `Keys.ENTER` to submit without clicking a button. |
| `demoDropdownSelect()` | The `Select` class: `selectByVisibleText`, `selectByValue`, `selectByIndex`, `getFirstSelectedOption()`, `getOptions()`, `isMultiple()`. |
| `demoCheckboxesLoopsAndArrays()` | `findElements` + `List`→array conversion, an indexed `for` loop, `isSelected()`/`isEnabled()`/`isDisplayed()`/`getTagName()`. |
| `demoTabsAndWindowSwitching()` | `getWindowHandle()` vs `getWindowHandles()` (a `Set<String>`), waiting for a second tab to open, `switchTo().window(handle)`, `driver.close()` vs `driver.quit()`. |
| `demoIframeSwitching()` | `switchTo().frame(WebElement)`, interacting with a `contenteditable` element (why `.clear()` doesn't work there and `Keys.chord(CONTROL, "a")` + `DELETE` does), `switchTo().defaultContent()`. |
| `demoJavascriptAlerts()` | Triggering native `alert()`/`confirm()`/`prompt()` dialogs and handling them with `switchTo().alert()`: `getText()`, `accept()`, `dismiss()`, `sendKeys()`. |
| `demoMouseActionsAndContextClick()` | The `Actions` class: `moveToElement()` for hover, `contextClick()` for a right-click that triggers a JS `confirm`. |
| `demoJavascriptExecutor()` | Casting `WebDriver` to `JavascriptExecutor`, running arbitrary JS (`window.scrollBy`, reading `document.title`, styling an element via `arguments[0]`). |
| `demoCookiesAndWindowManagement()` | `Cookie`, `manage().addCookie/getCookies/getCookieNamed`, `manage().window().getSize/setSize/setPosition`, `navigate().back/forward/refresh`. |
| `demoScreenshot()` | `TakesScreenshot.getScreenshotAs(OutputType.FILE)` and saving it under `target/screenshots/` with `java.nio.file`. |
| `demoExceptionHandling()` | Catching Selenium's `NoSuchElementException` explicitly, with a `finally` block for guaranteed cleanup logic. |
| `section(String)` | Small helper that prints the `====` banners you see between each topic in the console output. |

### Known environment quirk already handled in the code

Some ChromeDriver builds throw `WebDriverException: failed to change window
state to 'normal', current state is 'maximized'` when you call `setSize()`
on a maximized window. `demoCookiesAndWindowManagement()` calls
`setPosition()` first and wraps the resize in a `try/catch` so the script
keeps going and just logs it as non-fatal — a good example of defensive
handling around browser/driver version differences you'll hit in real
projects.

## Next steps to extend this project

- Convert the `demo*` methods into `@Test` methods under `src/test/java`,
  the way `SauceDemoCheckoutTest` already does, so they run under `mvn test`
  with real assertions instead of console output you have to eyeball.
- Add a Page Object class per page (`LoginPage`, `DropdownPage`, ...) once
  you're comfortable with the raw API calls shown here.
- Swap `ChromeOptions` for `FirefoxOptions`/`EdgeOptions` and a different
  `WebDriverManager.*driver()` call to practise cross-browser runs.
