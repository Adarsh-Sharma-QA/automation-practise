# WebDriver Methods Reference

Every `driver.*` call used in [SeleniumSyntaxPlayground.java](../src/main/java/com/practise/automation/SeleniumSyntaxPlayground.java), grouped by category, with the follow-up methods each one unlocks.

---

## 1. Navigation

| Method | Returns | Notes |
|---|---|---|
| `driver.get(url)` | `void` | Loads a URL, blocks until the page finishes loading. |
| `driver.navigate()` | `WebDriver.Navigation` | Entry point for History-API-style navigation. |
| `driver.navigate().to(url)` | `void` | Same effect as `get()`, but part of the `Navigation` interface. |
| `driver.navigate().back()` | `void` | Like clicking the browser Back button. |
| `driver.navigate().forward()` | `void` | Like clicking the browser Forward button. |
| `driver.navigate().refresh()` | `void` | Reloads the current page. |

---

## 2. Finding Elements

| Method | Returns | Notes |
|---|---|---|
| `driver.findElement(By...)` | `WebElement` | Returns the first match; throws `NoSuchElementException` if none found. |
| `driver.findElements(By...)` | `List<WebElement>` | Returns all matches; returns an **empty list** (never throws) if none found. |

### `By` locator strategies used
| Locator | Example |
|---|---|
| `By.id(...)` | `By.id("username")` — fastest/most reliable when available |
| `By.tagName(...)` | `By.tagName("a")` |
| `By.className(...)` | `By.className("figure")` — single CSS class only |
| `By.cssSelector(...)` | `By.cssSelector("#checkboxes input[type='checkbox']")` |
| `By.linkText(...)` | `By.linkText("Click Here")` — exact visible link text |

### Once you have a `WebElement`
| Method | Returns | Notes |
|---|---|---|
| `.getText()` | `String` | Visible rendered text. |
| `.click()` | `void` | Clicks / toggles the element. |
| `.clear()` | `void` | Empties an `<input>`'s current value. |
| `.sendKeys(...)` | `void` | Types text or sends `Keys` (e.g. `Keys.TAB`, `Keys.ENTER`, `Keys.chord(...)`). |
| `.isSelected()` | `boolean` | Checked state (checkboxes/radios/options). |
| `.isEnabled()` | `boolean` | Not disabled in the DOM. |
| `.isDisplayed()` | `boolean` | Actually visible/rendered (not just present). |
| `.getTagName()` | `String` | e.g. `"input"`. |
| `.findElement(By...)` | `WebElement` | Searches **within** this element's subtree, not the whole page. |

---

## 3. Page Info

| Method | Returns | Notes |
|---|---|---|
| `driver.getCurrentUrl()` | `String` | Address bar URL. |
| `driver.getTitle()` | `String` | `<title>` tag content. |

---

## 4. `manage()` — timeouts, cookies, window

`driver.manage()` returns `WebDriver.Options`, the entry point for the methods below.

### Timeouts
| Method | Notes |
|---|---|
| `driver.manage().timeouts().implicitlyWait(Duration)` | Global poll time applied to every `findElement`/`findElements` call before throwing `NoSuchElementException`. Set to `Duration.ofSeconds(0)` to disable. |

### Cookies
| Method | Returns | Notes |
|---|---|---|
| `.addCookie(Cookie)` | `void` | Adds a cookie to the current domain. |
| `.getCookies()` | `Set<Cookie>` | All cookies visible to the current page. |
| `.getCookieNamed(String)` | `Cookie` (nullable) | Look up a single cookie by name. |

### Window
`driver.manage().window()` returns `WebDriver.Window`.

| Method | Returns | Notes |
|---|---|---|
| `.maximize()` | `void` | Fills the screen. |
| `.getSize()` | `Dimension` | Has `.getWidth()` / `.getHeight()`. |
| `.setSize(Dimension)` | `void` | Explicit width/height in pixels — some ChromeDriver versions reject this while maximized. |
| `.setPosition(Point)` | `void` | Moves the window; `new Point(0, 0)` = top-left. |

---

## 5. Window / Tab Handles

| Method | Returns | Notes |
|---|---|---|
| `driver.getWindowHandle()` | `String` | Handle of the **current** tab. |
| `driver.getWindowHandles()` | `Set<String>` | Handles of **all** open tabs. |
| `driver.switchTo()` | `TargetLocator` | Entry point for switching context (see below). |
| `driver.close()` | `void` | Closes only the **current** tab/window (session stays alive if others remain). |
| `driver.quit()` | `void` | Closes **every** window/tab AND ends the whole WebDriver session. |

---

## 6. `switchTo()` — frames, windows, alerts

`driver.switchTo()` returns `WebDriver.TargetLocator`.

| Method | Returns | Notes |
|---|---|---|
| `.window(handle)` | `WebDriver` | Moves focus to the tab with that handle. |
| `.frame(WebElement)` | `WebDriver` | Enters an `<iframe>`. |
| `.defaultContent()` | `WebDriver` | Exits back to the main page/top-level document. |
| `.activeElement()` | `WebElement` | Currently focused element (useful after `Keys.TAB`). |
| `.alert()` | `Alert` | Handle to a native JS `alert`/`confirm`/`prompt` dialog. |

### `Alert` methods
| Method | Notes |
|---|---|
| `.getText()` | Reads the dialog's message. |
| `.accept()` | Clicks "OK". |
| `.dismiss()` | Clicks "Cancel". |
| `.sendKeys(String)` | Types into a `prompt()` dialog's text field. |

---

## 7. Waits (built on top of the driver)

| Class | Notes |
|---|---|
| `new WebDriverWait(driver, Duration)` | Explicit wait for one specific condition; use with `ExpectedConditions.*`. |
| `wait.until(ExpectedConditions.visibilityOfElementLocated(By))` | Polls until element is visible, then returns it. |
| `wait.until(ExpectedConditions.elementToBeClickable(By))` | Polls until clickable. |
| `wait.until(ExpectedConditions.presenceOfElementLocated(By))` | Polls until present in the DOM (not necessarily visible). |
| `wait.until(ExpectedConditions.alertIsPresent())` | Polls until a native dialog appears. |
| `wait.until(d -> ...)` | Custom lambda condition — `d` is the `WebDriver`. |
| `new FluentWait<>(driver).withTimeout(...).pollingEvery(...).ignoring(...)` | Like `WebDriverWait` but with configurable polling interval and ignored exceptions while polling. |

---

## 8. `Select` — dropdowns

Wraps a `<select>` `WebElement`: `new Select(driver.findElement(By.id("dropdown")))`.

| Method | Returns | Notes |
|---|---|---|
| `.selectByVisibleText(String)` | `void` | Matches the text shown to the user. |
| `.selectByValue(String)` | `void` | Matches the `value=""` attribute. |
| `.selectByIndex(int)` | `void` | Matches by position (0-based). |
| `.getFirstSelectedOption()` | `WebElement` | Currently selected `<option>`. |
| `.getOptions()` | `List<WebElement>` | Every `<option>` in the `<select>`. |
| `.isMultiple()` | `boolean` | True only if the `<select>` has the `multiple` attribute. |

---

## 9. `Actions` — mouse/keyboard gestures

`new Actions(driver)` builds a queue of gestures; nothing runs until `.perform()`.

| Method | Notes |
|---|---|
| `.moveToElement(WebElement)` | Queues a mouse hover. |
| `.contextClick(WebElement)` | Queues a right-click. |
| `.perform()` | Executes everything queued so far. |

---

## 10. `JavascriptExecutor` — running raw JS

Obtained by casting: `(JavascriptExecutor) driver`.

| Method | Returns | Notes |
|---|---|---|
| `.executeScript(String, Object...)` | `Object` | Runs arbitrary JS in the page context. A `return` in the script becomes the Java return value; extra args are accessible in JS as `arguments[0]`, `arguments[1]`, etc. |

---

## 11. `TakesScreenshot`

Obtained by casting: `(TakesScreenshot) driver`.

| Method | Returns | Notes |
|---|---|---|
| `.getScreenshotAs(OutputType.FILE)` | `File` | Captures the screenshot to a temp file, which you then copy elsewhere with `Files.copy(...)`. |

---

## 12. Setup helpers (not on `driver`, but required before creating one)

| Call | Notes |
|---|---|
| `WebDriverManager.chromedriver().setup()` | Auto-downloads the matching `chromedriver` binary for the installed Chrome version. |
| `new ChromeOptions()` | Container for launch flags; configure with `.addArguments("--flag")`. |
| `new ChromeDriver(options)` | Actually launches the Chrome process — this is what produces your `WebDriver` object. |

---

## Quick call-chain cheat sheet

```
driver
 ├─ .get(url) / .getCurrentUrl() / .getTitle() / .close() / .quit()
 ├─ .findElement(By) / .findElements(By)  -> WebElement / List<WebElement>
 ├─ .navigate()        -> .to(url) / .back() / .forward() / .refresh()
 ├─ .manage()          -> .timeouts().implicitlyWait(Duration)
 │                     -> .addCookie() / .getCookies() / .getCookieNamed()
 │                     -> .window() -> .maximize() / .getSize() / .setSize() / .setPosition()
 ├─ .switchTo()        -> .window(handle) / .frame(el) / .defaultContent()
 │                     -> .activeElement() / .alert() -> .accept()/.dismiss()/.getText()/.sendKeys()
 ├─ .getWindowHandle() / .getWindowHandles()
 └─ (cast) JavascriptExecutor -> .executeScript(...)
    (cast) TakesScreenshot    -> .getScreenshotAs(OutputType.FILE)
```
