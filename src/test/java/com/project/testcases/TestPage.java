//package com.project.testcases;
// 
////import com.project.pages.*;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.testng.Assert;
//import org.testng.annotations.*;
//
//import com.project.pages.CourseraHomePage;
//import com.project.pages.EnterprisePage;
//import com.project.pages.SearchResultsPage;
//
//public class TestPage {
//
//    private WebDriver driver;
//
//    @BeforeClass(alwaysRun = true)
//    public void setUp() {
//        // If chromedriver is not on PATH, set it like:
//        // System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");
//        driver = new ChromeDriver();
//        driver.manage().window().maximize();
//        driver.get("https://www.coursera.org");
//    }
//
//    @Test(priority = 1)
//    public void searchApplyFiltersAndPrint() {
//        CourseraHomePage home = new CourseraHomePage(driver);
//        SearchResultsPage results = home.searchCourse("web development");
//
//        results.applyEnglishFilter()
//               .applyBeginnerFilter()
//               .printFirstTwoCourses();
//
//        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("/search"),
//                "Expected to be on a /search results page");
//    }
//
//    @Test(priority = 2, dependsOnMethods = "searchApplyFiltersAndPrint")
//    public void clearFilters() {
//        new SearchResultsPage(driver).clearFiltersIfVisible();
//        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("coursera.org"),
//                "Expected to remain on Coursera domain");
//    }
//
//    @Test(priority = 3, dependsOnMethods = "searchApplyFiltersAndPrint")
//    public void openEnterpriseAndSubmitForm() {
//        CourseraHomePage home = new CourseraHomePage(driver);
//        EnterprisePage enterprise = home.openEnterprise();
//
//        enterprise.fillAndSubmit(
//                "Pavitra",
//                "Vaishnavi",
//                "pavitra.vaishnavi@example.com",
//                "9876543210",
//                "Business",
//                "PAT",
//                "Cognizant",
//                "15001-30000",
//                "Courses for myself",
//                "India",
//                "Telangana"
//        );
//
//        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("coursera.org"),
//                "Still on Coursera site post-submit");
//    }
//
//    @AfterClass(alwaysRun = true)
//    public void tearDown() {
//        if (driver != null) driver.quit();
//    }
//}














package com.project.testcases;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.*;

import com.project.pages.CourseraHomePage;
import com.project.pages.EnterprisePage;
import com.project.pages.SearchResultsPage;

import java.time.Duration;
import java.util.List;

public class TestPage {

    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        // If chromedriver is not on PATH, set it like:
        // System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        // It's better to avoid implicit waits if your pages use explicit waits:
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.get("https://www.coursera.org");
    }

    @Test(priority = 1)
    public void searchApplyFiltersAndPrint() {
        CourseraHomePage home = new CourseraHomePage(driver);
        SearchResultsPage results = home.searchCourse("web development");

        // CHANGED: use the new extraction method and print manually
        results.applyEnglishFilter()
               .applyBeginnerFilter();

        List<SearchResultsPage.CourseInfo> firstTwo = results.getFirstTwoCourses();

        // Print them (replacing the old printFirstTwoCourses())
        System.out.println("=== First 2 Courses ===");
        for (int i = 0; i < firstTwo.size(); i++) {
            SearchResultsPage.CourseInfo c = firstTwo.get(i);
            System.out.println("----- COURSE " + (i + 1) + " -----");
            System.out.println("Title   : " + c.title);
            System.out.println("Rating  : " + c.rating);
            System.out.println("Duration: " + c.duration);
            System.out.println("Level   : " + c.level);
        }

        // Ensure we actually extracted 2 courses
        Assert.assertTrue(firstTwo.size() >= 2, "Expected at least 2 courses after applying filters");

        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("/search"),
                "Expected to be on a /search results page");
    }

    @Test(priority = 2, dependsOnMethods = "searchApplyFiltersAndPrint")
    public void clearFilters() {
        new SearchResultsPage(driver).clearFiltersIfVisible();
        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("coursera.org"),
                "Expected to remain on Coursera domain");
    }

    @Test(priority = 3, dependsOnMethods = "searchApplyFiltersAndPrint")
    public void openEnterpriseAndSubmitForm() {
        CourseraHomePage home = new CourseraHomePage(driver);
        EnterprisePage enterprise = home.openEnterprise();

        enterprise.fillAndSubmit(
                "Pavitra",
                "Vaishnavi",
                "pavitra.vaishnavi@example.com",
                "9876543210",
                "Business",
                "PAT",
                "Cognizant",
                "15001-30000",
                "Courses for myself",
                "India",
                "Telangana"
        );

        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("coursera.org"),
                "Still on Coursera site post-submit");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
