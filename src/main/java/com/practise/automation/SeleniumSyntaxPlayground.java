package com.practise.automation; // folder path <-> package name must match

import io.github.bonigarcia.wdm.WebDriverManager; // auto-downloads/manages the chromedriver binary
import org.openqa.selenium.*; // core Selenium types: WebDriver, WebElement, By, Keys, Cookie, Alert, etc.
import org.openqa.selenium.NoSuchElementException; // imported explicitly to disambiguate from java.util.NoSuchElementException
import org.openqa.selenium.chrome.ChromeDriver; // the Chrome-specific WebDriver implementation
import org.openqa.selenium.chrome.ChromeOptions; // lets us configure how Chrome launches (flags, headless, etc.)
import org.openqa.selenium.interactions.Actions; // builder for mouse/keyboard gestures (hover, right-click, drag)
import org.openqa.selenium.support.ui.*; // WebDriverWait, FluentWait, Select, ExpectedConditions

import java.io.File; // represents the screenshot file returned by Selenium
import java.nio.file.Files; // used to copy the screenshot to a target location
import java.nio.file.Path; // typed file-system path (modern replacement for java.io.File paths)
import java.nio.file.Paths; // factory for building Path objects
import java.nio.file.StandardCopyOption; // flag to allow overwriting an existing screenshot file
import java.time.Duration; // modern way to express wait timeouts (replaces raw seconds/millis ints)
import java.util.*; // List, ArrayList, Map, HashMap, Set, Arrays

/**
 * One-file playground covering core Java syntax + the most commonly used
 * Selenium WebDriver APIs. Run with: mvn exec:java
 *
 * Site under test: https://the-internet.herokuapp.com (a public sandbox
 * built specifically for practising automation - safe to hammer).
 */
public class SeleniumSyntaxPlayground { // public so Maven's exec plugin / IDE can find and run it  


    private static final String BASE_URL = "https://the-internet.herokuapp.com"; // shared constant reused by every demo method

    public static void main(String[] args) { // JVM entry point - args are unused command-line arguments
        javaCoreSyntaxDemo(); // runs first: plain Java syntax warm-up, no browser needed yet

        WebDriver driver = null; // declared outside try so it is visible in the finally block below
        try {
            driver = createDriver(); // launches a real Chrome window and returns the WebDriver handle to it

            demoNavigationAndLocators(driver); // driver.get(), findElements, By.tagName, List<->array, split/charAt
            demoExplicitAndFluentWaits(driver); // implicit wait, WebDriverWait, FluentWait
            demoLoginAndKeyboardActions(driver); // sendKeys, clear, Keys.TAB/ENTER, switchTo().activeElement()
            demoDropdownSelect(driver); // the Select class for <select> dropdowns
            demoCheckboxesLoopsAndArrays(driver); // findElements + indexed for loop + isSelected()
            demoTabsAndWindowSwitching(driver); // getWindowHandle(s), switchTo().window()
            demoIframeSwitching(driver); // switchTo().frame() / defaultContent()
            demoJavascriptAlerts(driver); // switchTo().alert(): accept/dismiss/sendKeys
            demoMouseActionsAndContextClick(driver); // Actions class: hover + right-click
            demoJavascriptExecutor(driver); // running raw JavaScript via JavascriptExecutor
            demoCookiesAndWindowManagement(driver); // cookies, window size/position, back/forward/refresh
            demoScreenshot(driver); // TakesScreenshot -> save a PNG to disk
            demoExceptionHandling(driver); // catching a Selenium-specific exception on purpose

        } finally {
            // finally always runs -> guarantees the browser process is closed
            // even if one of the demo methods above throws.
            if (driver != null) { // guard against createDriver() itself having failed
                driver.quit(); // closes every window/tab AND ends the WebDriver session (unlike close())
            }
        }
    }

    // =====================================================================
    // 0. DRIVER SETUP
    // =====================================================================
    private static WebDriver createDriver() { // builds and returns a ready-to-use Chrome WebDriver
        WebDriverManager.chromedriver().setup(); // auto-downloads matching chromedriver

        ChromeOptions options = new ChromeOptions(); // container for browser launch flags/capabilities
        options.addArguments("--remote-allow-origins=*"); // works around a CDP origin check some Chrome versions enforce
        // options.addArguments("--headless=new"); // uncomment for no visible window

        WebDriver driver = new ChromeDriver(options); // actually starts the chromedriver process + browser
        driver.manage().window().maximize(); // resize the browser window to fill the screen
        return driver; // hand the live session back to the caller
    }

    // =====================================================================
    // 1. PLAIN JAVA SYNTAX WARM-UP (no browser involved)
    // =====================================================================
    private static void javaCoreSyntaxDemo() { // pure Java syntax refresher, runs before any browser opens
        section("JAVA CORE SYNTAX WARM-UP"); // prints a banner so console output is easy to scan

        // variables & data types
        int number = 7; // 32-bit whole number
        double price = 19.99; // 64-bit floating point number
        boolean isAvailable = true; // true/false flag
        char grade = 'A'; // single 16-bit Unicode character (single quotes, not double)
        String text = "Selenium Automation"; // immutable sequence of characters (double quotes)

        // if / else if / else
        if (number > 10) { // condition checked first
            System.out.println("number is big");
        } else if (number == 7) { // only checked if the first condition was false
            System.out.println("number is exactly seven"); // this branch runs since number == 7
        } else { // fallback if none of the above matched
            System.out.println("number is small");
        }

        // ternary operator
        String parity = (number % 2 == 0) ? "even" : "odd"; // shorthand for if/else that returns a value: condition ? whenTrue : whenFalse
        System.out.println(number + " is " + parity); // string concatenation with +

        // classic for loop
        for (int i = 1; i <= 5; i++) { // init; condition; increment - runs while condition is true
            System.out.print(i + " "); // print() (no newline) so numbers stay on one line
        }
        System.out.println(); // print a trailing newline to close out the line above

        // enhanced for-each loop over an array
        String[] browsers = {"Chrome", "Firefox", "Edge", "Safari"}; // fixed-size array literal
        for (String browser : browsers) { // "for each browser in browsers" - no manual index needed
            System.out.println("Browser: " + browser);
        }

        // while loop
        int counter = 0; // loop variable declared outside the loop
        while (counter < 3) { // condition checked before every iteration
            System.out.println("while counter = " + counter);
            counter++; // must manually advance the counter or this loops forever
        }

        // do-while loop (body always runs at least once)
        int retries = 0;
        do { // body executes first...
            System.out.println("retry attempt " + retries);
            retries++;
        } while (retries < 2); // ...then the condition is checked to decide whether to repeat

        // classic switch statement
        int day = 3; // value being matched against each case
        switch (day) {
            case 1: // matches when day == 1
                System.out.println("Monday");
                break; // stops execution from "falling through" into case 2
            case 2:
                System.out.println("Tuesday");
                break;
            case 3: // this is the branch that actually runs, since day == 3
                System.out.println("Wednesday");
                break;
            default: // runs if no case matched
                System.out.println("Some other day");
        }

        // modern switch expression
        String dayName = switch (day) { // switch used as an expression that produces a value
            case 1 -> "Monday"; // arrow form: no break needed, no fall-through
            case 2 -> "Tuesday";
            case 3 -> "Wednesday"; // matches, so dayName becomes "Wednesday"
            default -> "Unknown";
        };
        System.out.println("dayName = " + dayName);

        // List (ArrayList)
        List<String> testData = new ArrayList<>(); // resizable list, declared via the List interface
        testData.add("QA"); // appends to the end
        testData.add("Automation");
        testData.add("Selenium");
        testData.remove("QA"); // removes the first element equal to "QA"
        System.out.println("List after remove: " + testData); // List.toString() prints [Automation, Selenium]
        System.out.println("List contains 'Selenium'? " + testData.contains("Selenium")); // membership check

        // Map
        Map<String, String> credentials = new HashMap<>(); // key -> value store, no guaranteed order
        credentials.put("username", "tomsmith"); // insert/overwrite the value for a key
        credentials.put("password", "SuperSecretPassword!");
        for (Map.Entry<String, String> entry : credentials.entrySet()) { // iterate key/value pairs together
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        // String methods: split, charAt, substring, trim, replace, case conversion
        String sentence = "  Selenium,WebDriver,Java,TestNG  "; // has leading/trailing spaces on purpose
        String trimmed = sentence.trim(); // removes leading/trailing whitespace
        String[] tokens = trimmed.split(","); // breaks the string into an array wherever "," appears
        System.out.println("Token count: " + tokens.length); // arrays expose length as a field, not a method
        for (String token : tokens) {
            System.out.println("token = " + token + ", firstChar = " + token.charAt(0)); // charAt(0) = first character
        }
        System.out.println("Substring(0,4): " + trimmed.substring(0, 4)); // characters from index 0 up to (not including) 4
        System.out.println("Replace: " + trimmed.replace("Java", "Core Java")); // replaces every occurrence
        System.out.println("Upper/Lower: " + trimmed.toUpperCase() + " / " + trimmed.toLowerCase()); // case conversion
        System.out.println("Vowel count in '" + text + "': " + countVowels(text)); // calls our custom method below

        // StringBuilder
        StringBuilder sb = new StringBuilder(); // mutable string buffer - avoids creating a new String on every concatenation
        sb.append("Selenium").append(" ").append("Automation").append(" ").append("Practise"); // chained appends
        sb.insert(0, "[LEARNING] "); // inserts text at a specific index (0 = the very start)
        System.out.println(sb); // println calls sb.toString() implicitly

        // Arrays utility + sorting
        int[] numbers = {5, 3, 8, 1, 9}; // primitive int array literal
        Arrays.sort(numbers); // sorts in place, ascending order
        System.out.println("Sorted array: " + Arrays.toString(numbers)); // Arrays.toString formats arrays nicely (arrays don't override toString themselves)

        // try-catch-finally with a plain Java exception
        try {
            int result = 10 / (number - 7); // number == 7 -> divide by zero
            System.out.println("Result: " + result); // never reached because the line above throws
        } catch (ArithmeticException e) { // catches only this specific exception type
            System.out.println("Caught: " + e.getMessage()); // e.getMessage() -> "/ by zero"
        } finally { // runs whether or not an exception was thrown/caught
            System.out.println("Java core warm-up finished");
        }
        System.out.println("isAvailable=" + isAvailable + ", price=" + price + ", grade=" + grade); // shows the earlier variables are still in scope
    }

    /** Custom method demo: loop + if + charAt + return value. */
    private static int countVowels(String input) { // private helper, only callable from within this class
        int count = 0; // running total, starts at zero
        String vowels = "aeiouAEIOU"; // reference string of characters we're counting
        for (int i = 0; i < input.length(); i++) { // walk through every character by index
            char c = input.charAt(i); // grab the character at position i
            if (vowels.indexOf(c) != -1) { // indexOf returns -1 when the character is not found
                count++; // found a vowel, increment the tally
            }
        }
        return count; // send the final tally back to the caller
    }

    // =====================================================================
    // 2. NAVIGATION + LOCATORS (By.id/name/css/xpath/tagName/className/linkText)
    // =====================================================================
    private static void demoNavigationAndLocators(WebDriver driver) { // takes the shared driver instance as a parameter
        section("NAVIGATION + LOCATORS");

        driver.get(BASE_URL); // initial navigation - blocks until the page finishes loading

        List<WebElement> allLinks = driver.findElements(By.tagName("a")); // findElements -> List<WebElement>
        System.out.println("Total links on homepage: " + allLinks.size()); // List.size() = number of matches, never throws if zero

        for (int i = 0; i < allLinks.size(); i++) { // indexed for loop
            if (i < 5) { // if inside for loop
                System.out.println((i + 1) + ". " + allLinks.get(i).getText()); // get(i) fetches by position; getText() returns visible text
            }
        }

        // List -> array conversion
        WebElement[] linksArray = allLinks.toArray(new WebElement[0]); // toArray needs a typed array to know what kind to build
        System.out.println("Array length after conversion: " + linksArray.length); // arrays use .length (field), not .length() or .size()

        String currentUrl = driver.getCurrentUrl(); // reads the browser's current address bar URL
        String[] urlParts = currentUrl.split("/"); // split()
        for (String part : urlParts) {
            System.out.println("urlPart = " + part);
        }

        String pageTitle = driver.getTitle(); // reads the <title> tag's content
        System.out.println("Title first char: " + pageTitle.charAt(0)); // charAt()
        System.out.println("Title contains 'Internet'? " + pageTitle.contains("Internet")); // simple substring check
    }

    // =====================================================================
    // 3. WAITS: implicit / explicit (WebDriverWait) / fluent (FluentWait)
    // =====================================================================
    private static void demoExplicitAndFluentWaits(WebDriver driver) {
        section("WAITS: implicit / explicit / fluent");

        // Implicit wait: WebDriver polls the DOM for up to this long on every
        // findElement call before throwing NoSuchElementException.
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2)); // applies globally to this driver session

        // Explicit wait: wait for one specific condition.
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // reusable wait object, max 10s per call
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1"))); // polls until the <h1> is visible, then returns it
        System.out.println("Heading via explicit wait: " + heading.getText());

        // Fluent wait: like explicit wait but with custom polling interval
        // and a list of exceptions to ignore while polling.
        Wait<WebDriver> fluentWait = new FluentWait<>(driver) // builder-style wait configuration
                .withTimeout(Duration.ofSeconds(15)) // overall time budget
                .pollingEvery(Duration.ofMillis(500)) // how often to re-check the condition
                .ignoring(NoSuchElementException.class); // don't fail immediately if the element isn't there yet

        WebElement heading2 = fluentWait.until(d -> d.findElement(By.tagName("h1"))); // lambda: d is the WebDriver, condition = "element is found"
        System.out.println("Heading via fluent wait: " + heading2.getText());

        // Reset implicit wait to 0 - best practice is to not mix implicit
        // and explicit waits in real projects; shown together here only for learning.
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // back to no implicit wait
    }

    // =====================================================================
    // 4. LOGIN FORM: sendKeys / clear / click + KEYBOARD ACTIONS (Keys class)
    // =====================================================================
    private static void demoLoginAndKeyboardActions(WebDriver driver) {
        section("LOGIN FORM + KEYBOARD ACTIONS");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // fresh wait scoped to this method
        driver.navigate().to(BASE_URL + "/login"); // navigate().to() is equivalent to get() but part of the History API

        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))); // By.id -> fastest/most reliable locator when available
        username.clear(); // empties any pre-filled text in the input
        username.sendKeys("tomsmith"); // types the given text
        username.sendKeys(Keys.TAB); // move focus to next field via keyboard

        WebElement active = driver.switchTo().activeElement(); // currently focused element
        active.sendKeys("SuperSecretPassword!"); // types into whichever field now has focus (the password field)
        active.sendKeys(Keys.ENTER); // submit the form via keyboard instead of clicking the button

        WebElement flash = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("flash"))); // waits for the post-submit banner to appear
        // getText() includes the trailing "x" close-button glyph, so keep only the first line
        String flashText = flash.getText().split("\n")[0]; // split on newline, keep the first segment only
        System.out.println("Flash message: " + flashText);

        if (flashText.contains("You logged into a secure area")) { // simple pass/fail check on the message text
            System.out.println("Login succeeded");
        } else {
            System.out.println("Login failed");
        }
    }

    // =====================================================================
    // 5. DROPDOWNS: Select class
    // =====================================================================
    private static void demoDropdownSelect(WebDriver driver) {
        section("DROPDOWN (Select class)");

        driver.navigate().to(BASE_URL + "/dropdown");

        Select dropdown = new Select(driver.findElement(By.id("dropdown"))); // wraps a <select> WebElement with dropdown-specific helpers
        dropdown.selectByVisibleText("Option 1"); // matches the text shown to the user
        System.out.println("Selected by text: " + dropdown.getFirstSelectedOption().getText()); // reads back the currently selected <option>

        dropdown.selectByValue("2"); // matches the option's value="" attribute
        System.out.println("Selected by value: " + dropdown.getFirstSelectedOption().getText());

        dropdown.selectByIndex(1); // index 0 is the disabled "Please select an option" placeholder
        System.out.println("Selected by index: " + dropdown.getFirstSelectedOption().getText());

        List<WebElement> options = dropdown.getOptions(); // every <option> element inside the <select>
        System.out.println("Total dropdown options: " + options.size());
        System.out.println("Dropdown is multiple-select? " + dropdown.isMultiple()); // true only if the <select> has the "multiple" attribute
    }

    // =====================================================================
    // 6. CHECKBOXES: findElements + isSelected + indexed for + array
    // =====================================================================
    private static void demoCheckboxesLoopsAndArrays(WebDriver driver) {
        section("CHECKBOXES + LOOPS + ARRAYS");

        driver.navigate().to(BASE_URL + "/checkboxes");

        List<WebElement> checkboxes = driver.findElements(By.cssSelector("#checkboxes input[type='checkbox']")); // CSS selector: inside #checkboxes, any <input type="checkbox">
        WebElement[] checkboxArray = checkboxes.toArray(new WebElement[0]); // convert List to array again, same trick as before
        System.out.println("Checkbox array length: " + checkboxArray.length);

        for (int i = 0; i < checkboxArray.length; i++) { // classic indexed loop over the array
            WebElement box = checkboxArray[i]; // array access by index using []
            if (!box.isSelected()) { // isSelected() reflects the checkbox's checked state
                box.click(); // clicking toggles a checkbox
                System.out.println("Checkbox " + i + " was unchecked -> clicked to check it");
            } else {
                System.out.println("Checkbox " + i + " already checked, unchecking it");
                box.click(); // click again to uncheck it
            }
            System.out.println("Checkbox " + i + " enabled? " + box.isEnabled() // isEnabled() -> not disabled in the DOM
                    + " displayed? " + box.isDisplayed() // isDisplayed() -> actually visible/rendered, not just present
                    + " tag=" + box.getTagName()); // getTagName() -> "input" for this element
        }
    }

    // =====================================================================
    // 7. TABS / WINDOWS: getWindowHandle(s) + switchTo().window()
    // =====================================================================
    private static void demoTabsAndWindowSwitching(WebDriver driver) {
        section("TAB / WINDOW SWITCHING");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.navigate().to(BASE_URL + "/windows");

        String mainWindow = driver.getWindowHandle(); // handle of the current tab
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Click Here"))); // By.linkText matches an <a> by its exact visible text
        link.click(); // this link opens a new tab (target="_blank")

        wait.until(d -> d.getWindowHandles().size() > 1); // poll until a second tab actually exists

        Set<String> allWindows = driver.getWindowHandles(); // Set<String> of all open tabs
        System.out.println("Total open tabs: " + allWindows.size());

        for (String handle : allWindows) { // for-each over a Set
            if (!handle.equals(mainWindow)) { // find the one handle that isn't the original tab
                driver.switchTo().window(handle); // move WebDriver's focus to that tab
                break; // stop looping once we've switched
            }
        }

        System.out.println("New tab title: " + driver.getTitle()); // now reads the new tab's title, since focus switched
        driver.close(); // close only the new tab

        driver.switchTo().window(mainWindow); // return focus to the original tab
        System.out.println("Back on: " + driver.getTitle());
    }

    // =====================================================================
    // 8. IFRAMES: switchTo().frame() / defaultContent()
    // =====================================================================
    private static void demoIframeSwitching(WebDriver driver) {
        section("IFRAME SWITCHING");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.navigate().to(BASE_URL + "/iframe");

        WebElement iframeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe"))); // find the <iframe> tag itself
        driver.switchTo().frame(iframeElement); // enter the iframe

        WebElement editorBody = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tinymce"))); // this id only exists once we're "inside" the frame
        // tinymce's body is contenteditable, not a real <input>, so .clear() is not
        // supported here - select-all + delete via a Keys.chord() does the same job.
        editorBody.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE); // Keys.chord presses CONTROL+A together, then DELETE removes the selection
        editorBody.sendKeys("Learning switchTo().frame() with Selenium"); // types fresh text into the now-empty editor
        System.out.println("Editor text now: " + editorBody.getText());

        driver.switchTo().defaultContent(); // exit back to the main page
        System.out.println("Back to main document, title: " + driver.getTitle());
    }

    // =====================================================================
    // 9. JAVASCRIPT ALERTS / CONFIRM / PROMPT: switchTo().alert()
    // =====================================================================
    private static void demoJavascriptAlerts(WebDriver driver) {
        section("JS ALERTS / CONFIRM / PROMPT");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.navigate().to(BASE_URL + "/javascript_alerts");

        driver.findElement(By.cssSelector("button[onclick='jsAlert()']")).click(); // CSS attribute selector matching a specific onclick handler
        Alert alert = wait.until(ExpectedConditions.alertIsPresent()); // waits for the native browser dialog to appear
        System.out.println("Alert text: " + alert.getText()); // reads the dialog's message
        alert.accept(); // clicks "OK"

        driver.findElement(By.cssSelector("button[onclick='jsConfirm()']")).click();
        alert = wait.until(ExpectedConditions.alertIsPresent()); // reassigning the same alert variable for the confirm dialog
        System.out.println("Confirm text: " + alert.getText());
        alert.dismiss(); // clicks "Cancel" instead of "OK"

        driver.findElement(By.cssSelector("button[onclick='jsPrompt()']")).click();
        alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.sendKeys("Selenium rocks"); // types into the prompt's text field
        alert.accept(); // confirms the prompt with the typed value

        String result = driver.findElement(By.id("result")).getText(); // page echoes back what accept()/dismiss() produced
        System.out.println("Result after alerts: " + result);
    }

    // =====================================================================
    // 10. MOUSE ACTIONS: hover, double click, right click (Actions class)
    // =====================================================================
    private static void demoMouseActionsAndContextClick(WebDriver driver) {
        section("MOUSE ACTIONS (hover + right click)");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Actions actions = new Actions(driver); // builder object for composing mouse/keyboard gestures

        driver.navigate().to(BASE_URL + "/hovers");
        List<WebElement> figures = driver.findElements(By.className("figure")); // By.className matches a single CSS class name
        for (WebElement figure : figures) {
            actions.moveToElement(figure).perform(); // mouse hover; perform() actually executes the queued action
            WebElement caption = figure.findElement(By.tagName("h5")); // find within figure's subtree, not the whole page
            System.out.println("Hover caption: " + caption.getText());
        }

        driver.navigate().to(BASE_URL + "/context_menu");
        WebElement hotSpot = driver.findElement(By.id("hot-spot"));
        actions.contextClick(hotSpot).perform(); // right click -> triggers a JS alert

        Alert contextAlert = wait.until(ExpectedConditions.alertIsPresent());
        System.out.println("Context menu alert: " + contextAlert.getText());
        contextAlert.accept();
    }

    // =====================================================================
    // 11. JAVASCRIPT EXECUTOR
    // =====================================================================
    private static void demoJavascriptExecutor(WebDriver driver) {
        section("JAVASCRIPT EXECUTOR");

        driver.navigate().to(BASE_URL);
        JavascriptExecutor js = (JavascriptExecutor) driver; // ChromeDriver implements this interface, so we can cast to it

        js.executeScript("window.scrollBy(0, 300);"); // runs arbitrary JS in the page context; no return value used here
        String domTitle = (String) js.executeScript("return document.title;"); // "return" in the script maps to this method's return value
        System.out.println("Title fetched via JS: " + domTitle);

        WebElement heading = driver.findElement(By.tagName("h1"));
        js.executeScript("arguments[0].style.border='3px solid red';", heading); // highlight element (arguments[0] refers to the WebElement passed in)
    }

    // =====================================================================
    // 12. COOKIES + WINDOW MANAGEMENT + BACK/FORWARD/REFRESH
    // =====================================================================
    private static void demoCookiesAndWindowManagement(WebDriver driver) {
        section("COOKIES + WINDOW MANAGEMENT");

        Cookie cookie = new Cookie("practiseCookie", "selenium-learning"); // name/value pair to store in the browser
        driver.manage().addCookie(cookie); // adds it to the current domain's cookie jar

        Set<Cookie> allCookies = driver.manage().getCookies(); // every cookie visible to the current page
        System.out.println("Cookie count: " + allCookies.size());

        Cookie fetched = driver.manage().getCookieNamed("practiseCookie"); // look up a single cookie by name
        System.out.println("Fetched cookie value: " + (fetched != null ? fetched.getValue() : "null")); // ternary guards against a null lookup

        Dimension size = driver.manage().window().getSize(); // current browser window width/height
        System.out.println("Window size: " + size.getWidth() + "x" + size.getHeight());
        try {
            // some ChromeDriver versions reject setSize() while the window is
            // maximized ("failed to change window state to 'normal'") - restore first.
            driver.manage().window().setPosition(new Point(0, 0)); // move the window to the top-left corner
            driver.manage().window().setSize(new Dimension(1200, 800)); // explicit width/height in pixels
            System.out.println("Resized window to: " + driver.manage().window().getSize());
        } catch (WebDriverException e) { // broad Selenium exception type, covers driver/browser-level failures
            System.out.println("Could not resize window (non-fatal): " + e.getMessage());
        }

        driver.navigate().to(BASE_URL + "/login");
        driver.navigate().back(); // like clicking the browser's Back button
        driver.navigate().forward(); // like clicking Forward
        driver.navigate().refresh(); // reloads the current page
        System.out.println("After back/forward/refresh, title: " + driver.getTitle());
    }

    // =====================================================================
    // 13. SCREENSHOTS
    // =====================================================================
    private static void demoScreenshot(WebDriver driver) {
        section("SCREENSHOT");

        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE); // cast driver to the screenshot interface, capture as a temp file
            Path target = Paths.get("target", "screenshots", "final-state.png"); // where we want the permanent copy to live
            Files.createDirectories(target.getParent()); // ensures target/screenshots/ exists before copying into it
            Files.copy(screenshot.toPath(), target, StandardCopyOption.REPLACE_EXISTING); // copy temp file -> target, overwrite if it already exists
            System.out.println("Screenshot saved at: " + target.toAbsolutePath()); // full path for easy access
        } catch (Exception e) { // IO operations can throw checked exceptions, so we catch broadly here
            System.out.println("Could not save screenshot: " + e.getMessage());
        }
    }

    // =====================================================================
    // 14. EXCEPTION HANDLING with Selenium exceptions
    // =====================================================================
    private static void demoExceptionHandling(WebDriver driver) {
        section("EXCEPTION HANDLING");

        try {
            driver.findElement(By.id("this-id-does-not-exist")); // guaranteed to fail - no element has this id
        } catch (NoSuchElementException e) { // catches only this specific Selenium exception type
            System.out.println("Caught expected exception: element not found");
        } finally { // runs regardless of whether the catch block ran
            System.out.println("Finally block always runs - good place for cleanup");
        }
    }

    private static void section(String title) { // small formatting helper reused by every demo method above
        System.out.println();
        System.out.println("=================================================");
        System.out.println("  " + title);
        System.out.println("=================================================");
    }
}
