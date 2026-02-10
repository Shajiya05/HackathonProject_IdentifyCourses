package com.project.pages;

import com.project.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchResultsPage extends BasePage {

    // --- Required driver utilities (kept local for robustness) ---
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    // --- Filters & controls ---
    private final By languageFilterBtn = By.xpath("//button[@data-testid='filter-dropdown-language']");
    private final By englishOption     = By.xpath("//label[contains(.,'English')]");
    private final By levelFilterBtn    = By.xpath("//button[@data-testid='filter-dropdown-productDifficultyLevel']");
    private final By beginnerOption    = By.xpath("//label[contains(.,'Beginner')]");
    private final By viewButton        = By.xpath("//button[@data-testid='filter-view-button']");
    private final By clearAllBtn       = By.xpath("//span[contains(@class,'cds-button-label') and normalize-space()='Clear all']");

    // --- Course results scaffolding ---
    private final By searchResultsFrame = By.id("search-results-frame");
    private final By searchResultsRoot  = By.id("searchResults");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.js = (JavascriptExecutor) driver;
    }

    // ---------------- Public actions ----------------

    /** Expands Language filter, selects English, applies via View, and waits for refreshed results. */
    public SearchResultsPage applyEnglishFilter() {
        WebElement langBtn = wait.until(ExpectedConditions.elementToBeClickable(languageFilterBtn));
        js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", langBtn);
        langBtn.click();

        WebElement english = wait.until(ExpectedConditions.elementToBeClickable(englishOption));
        english.click();

        WebElement view = wait.until(ExpectedConditions.elementToBeClickable(viewButton));
        view.click();

        waitForResultsScaffold();
        return this;
    }

    /** Expands Level filter, selects Beginner, applies via View, and waits for refreshed results. */
    public SearchResultsPage applyBeginnerFilter() {
        WebElement levelBtn = wait.until(ExpectedConditions.elementToBeClickable(levelFilterBtn));
        js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", levelBtn);
        levelBtn.click();

        WebElement beginner = wait.until(ExpectedConditions.elementToBeClickable(beginnerOption));
        beginner.click();

        WebElement view = wait.until(ExpectedConditions.elementToBeClickable(viewButton));
        view.click();

        waitForResultsScaffold();
        return this;
    }

    /** Clears all filters if the button is interactable (best-effort). */
    public SearchResultsPage clearFiltersIfVisible() {
        try {
            WebElement clearBtn = wait.until(ExpectedConditions.elementToBeClickable(clearAllBtn));
            clearBtn.click();
            sleepSilently(300);
        } catch (Exception ignored) {}
        return this;
    }

    /** Returns the first two courses (title, rating, duration, level) from the product card footer. */
    public List<CourseInfo> getFirstTwoCourses() {
        return getFirstNFromFooter(2);
    }

    /** Convenience: true if a filter chip containing the given text is visible. */
    public boolean isFilterChipActive(String chipText) {
        List<WebElement> chips = driver.findElements(By.xpath(
            "//*[contains(@class,'filter') or contains(@class,'Chip') or contains(@class,'pill') or contains(@class,'token')]//*[contains(normalize-space(.),'" + chipText + "')]"
        ));
        for (WebElement chip : chips) {
            try {
                if (chip.isDisplayed()) return true;
            } catch (StaleElementReferenceException ignored) {}
        }
        return false;
    }

    /** Convenience: true if any active filter chips are visible. */
    public boolean areAnyActiveFilters() {
        List<WebElement> chips = driver.findElements(By.xpath(
            "//*[contains(@class,'filter') or contains(@class,'Chip') or contains(@class,'pill') or contains(@class,'token')]"
        ));
        for (WebElement chip : chips) {
            try {
                if (chip.isDisplayed()) return true;
            } catch (StaleElementReferenceException ignored) {}
        }
        return false;
    }

    // ---------------- Core extraction ----------------

    /** Returns DTOs for first N course cards (title, rating, duration, level) using the ProductCard footer. */
    public List<CourseInfo> getFirstNFromFooter(int n) {
        List<WebElement> cards = waitForCardsAtLeast(Math.max(1, n));
        int limit = Math.min(n, cards.size());
        List<CourseInfo> out = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            WebElement card = cards.get(i);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", card);

            String title = getTitleFromCard(card);

            WebElement footer = null;
            try {
                footer = card.findElement(By.cssSelector("div.cds-ProductCard-footer"));
            } catch (NoSuchElementException ignored) {}

            String rating = "";
            String level  = "";
            String duration = "";

            if (footer != null) {
                String ratingRaw   = getRatingFromFooter(footer);
                String metaText    = getMetaFromFooter(footer);
                String levelRaw    = extractLevel(metaText);
                String durationRaw = extractDuration(metaText);
                rating   = ratingRaw   == null ? "" : ratingRaw.trim();
                level    = levelRaw    == null ? "" : levelRaw.trim();
                duration = durationRaw == null ? "" : durationRaw.trim();
            }

            out.add(new CourseInfo(title, rating, duration, level));
        }
        return out;
    }

    // ---------------- Internal waits & helpers ----------------

    private void waitForResultsScaffold() {
        wait.until(ExpectedConditions.presenceOfElementLocated(searchResultsFrame));
        wait.until(ExpectedConditions.presenceOfElementLocated(searchResultsRoot));
        wait.until(d -> !d.findElements(By.xpath("//div[@id='searchResults']//li")).isEmpty());
        sleepSilently(250); // small buffer for lazy content rendering
    }

    private List<WebElement> waitForCardsAtLeast(int min) {
        return wait.until(d -> {
            // Primary: cards under searchResults with a ProductCard footer
            List<WebElement> cards = d.findElements(By.xpath("//div[@id='searchResults']//li[.//div[contains(@class,'cds-ProductCard-footer')]]"));
            cards = visible(cards);
            if (cards.size() >= min) return cards;

            // Fallback: cards under the frame wrapper
            cards = d.findElements(By.xpath("//*[@id='search-results-frame']//li[.//div[contains(@class,'cds-ProductCard-footer')]]"));
            cards = visible(cards);
            if (cards.size() >= min) return cards;

            // Broad fallback: any li under searchResults that actually has a footer
            cards = d.findElements(By.xpath("//div[@id='searchResults']//ul[contains(@class,'cds-10') or contains(@class,'css-')]//li"));
            cards = filterThatHaveFooter(cards);
            if (cards.size() >= min) return cards;

            // Trigger lazy-load and retry next poll
            try { ((JavascriptExecutor) d).executeScript("window.scrollBy(0, 700);"); } catch (Exception ignored) {}
            return null;
        });
    }

    private List<WebElement> filterThatHaveFooter(List<WebElement> candidates) {
        List<WebElement> out = new ArrayList<>();
        for (WebElement c : candidates) {
            try {
                if (c.isDisplayed() && !c.findElements(By.cssSelector("div.cds-ProductCard-footer")).isEmpty()) {
                    out.add(c);
                }
            } catch (StaleElementReferenceException ignored) {}
        }
        return out;
    }

    private List<WebElement> visible(List<WebElement> list) {
        List<WebElement> out = new ArrayList<>();
        for (WebElement el : list) {
            try { if (el.isDisplayed()) out.add(el); } catch (StaleElementReferenceException ignored) {}
        }
        return out;
    }

    private ExpectedCondition<Boolean> urlContainsIgnoreCase(String token) {
        return d -> {
            try {
                String u = d.getCurrentUrl();
                return u != null && u.toLowerCase().contains(token.toLowerCase());
            } catch (Exception e) {
                return false;
            }
        };
    }

    private void dismissConsentIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            WebElement accept = shortWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Accept') or contains(.,'Agree') or contains(.,'Got it')]")));
            accept.click();
            sleepSilently(150);
        } catch (Exception ignored) {}
    }

    private String getTitleFromCard(WebElement card) {
        try {
            WebElement h = card.findElement(By.xpath(".//a[contains(@class,'cds-CommonCard-titleLink')]//*[self::h2 or self::h3]"));
            String t = h.getText().trim();
            if (!t.isEmpty()) return t;
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement h = card.findElement(By.xpath(".//*[self::h2 or self::h3][contains(@class,'cds-CommonCard-title') or contains(@class,'css-6ecy9b')]"));
            String t = h.getText().trim();
            if (!t.isEmpty()) return t;
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement h = card.findElement(By.xpath(".//div[contains(@class,'cds-ProductCard-content')]//*[self::h2 or self::h3]"));
            String t = h.getText().trim();
            if (!t.isEmpty()) return t;
        } catch (NoSuchElementException ignored) {}

        return "";
    }

    private String getRatingFromFooter(WebElement footer) {
        try {
            WebElement r = footer.findElement(By.xpath(".//div[@role='meter' and @aria-valuenow]"));
            String val = r.getAttribute("aria-valuenow");
            if (!isBlank(val)) return val.trim();
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement r = footer.findElement(By.cssSelector("span[class*='ratings-text']"));
            String t = r.getText().trim();
            if (!t.isEmpty()) return t;
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement aria = footer.findElement(By.xpath(".//*[@aria-label[contains(.,'out of 5')]]"));
            String label = aria.getAttribute("aria-label");
            if (!isBlank(label)) return label.trim();
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement t = footer.findElement(By.xpath(".//*[contains(normalize-space(.),'out of 5')]"));
            return t.getText().trim();
        } catch (NoSuchElementException ignored) {}

        return "";
    }

    private String getMetaFromFooter(WebElement footer) {
        try {
            WebElement meta = footer.findElement(By.cssSelector("div[class*='cds-CommonCard-metadata'] p"));
            String t = meta.getText().trim();
            if (!t.isEmpty()) return t;
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement meta = footer.findElement(By.xpath(
                ".//*[self::p or self::span or self::div]" +
                "[contains(.,'Beginner') or contains(.,'Intermediate') or contains(.,'Advanced') or contains(.,'All Levels') or " +
                " contains(translate(., 'HOURSWEEKSMONTH', 'hoursweeksmonth'),'hour') or " +
                " contains(translate(., 'HOURSWEEKSMONTH', 'hoursweeksmonth'),'week') or " +
                " contains(translate(., 'HOURSWEEKSMONTH', 'hoursweeksmonth'),'month')]"
            ));
            return meta.getText().trim();
        } catch (NoSuchElementException ignored) {}

        return "";
    }

    private String extractLevel(String meta) {
        if (isBlank(meta)) return "";
        List<String> levels = Arrays.asList("Beginner", "Intermediate", "Advanced", "Mixed", "All Levels");
        for (String lvl : levels) if (meta.contains(lvl)) return lvl;

        String[] parts = meta.split("•|·");
        if (parts.length > 0) {
            String first = parts[0].trim();
            for (String lvl : levels) if (first.equalsIgnoreCase(lvl)) return lvl;
        }
        return "";
    }

    private String extractDuration(String meta) {
        if (isBlank(meta)) return "";
        Pattern p = Pattern.compile("(?i)(\\d+\\s*-\\s*\\d+\\s*(weeks?|months?)|\\d+\\s*(hours?|weeks?|months?))");
        Matcher m = p.matcher(meta);
        if (m.find()) return m.group().trim();

        String[] parts = meta.split("•|·");
        for (int i = parts.length - 1; i >= 0; i--) {
            String token = parts[i].trim().toLowerCase();
            if (token.contains("hour") || token.contains("week") || token.contains("month")) return parts[i].trim();
        }
        return "";
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void sleepSilently(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // ---------------- DTO ----------------
    public static class CourseInfo {
        public final String title;
        public final String rating;
        public final String duration;
        public final String level;

        public CourseInfo(String title, String rating, String duration, String level) {
            this.title = title;
            this.rating = rating;
            this.duration = duration;
            this.level = level;
        }

        @Override
        public String toString() {
            return "CourseInfo{title='" + title + "', rating='" + rating + "', duration='" + duration + "', level='" + level + "'}";
        }
    }
}