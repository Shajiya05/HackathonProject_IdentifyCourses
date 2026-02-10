package com.project.pages;

import com.project.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CourseraHomePage extends BasePage {

    private final By searchBox = By.xpath("//input[@placeholder='What do you want to learn?']");
    private final By enterpriseFooterLink = By.xpath("//a[contains(@href,'/business')]");

    public CourseraHomePage(WebDriver driver) {
        super(driver);
    }

    /** Search a course and land on results */
    public SearchResultsPage searchCourse(String query) {
        type(searchBox, query);
        pressEnter(searchBox);
        return new SearchResultsPage(driver);
    }

    /** Scroll to footer → open Enterprise page */
    public EnterprisePage openEnterprise() {
        scrollToBottom();
        click(enterpriseFooterLink);
        return new EnterprisePage(driver);
    }
}