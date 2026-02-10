package com.project.pages;

import com.project.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

public class EnterprisePage extends BasePage {

    public EnterprisePage(WebDriver driver) {
        super(driver);
    }

    public void fillAndSubmit(
            String firstName, String lastName, String workEmail, String phone,
            String orgType, String jobTitle, String company,
            String companySize, String needs, String country, String state) {

        type(By.xpath("//input[@placeholder='First Name']"), firstName);
        type(By.xpath("//input[@placeholder='Last Name']"), lastName);
        type(By.xpath("//input[@placeholder='Work Email Address']"), workEmail);
        type(By.xpath("//input[@placeholder='Country Code + Phone Number']"), phone);

        new Select(driver.findElement(By.id("rentalField9"))).selectByVisibleText(orgType);
        type(By.id("Title"), jobTitle);
        type(By.id("Company"), company);

        new Select(driver.findElement(By.id("Employee_Range__c"))).selectByVisibleText(companySize);
        new Select(driver.findElement(By.id("Self_Reported_Needs__c"))).selectByVisibleText(needs);
        new Select(driver.findElement(By.id("Country"))).selectByVisibleText(country);
        new Select(driver.findElement(By.id("State"))).selectByVisibleText(state);

        click(By.className("mktoButton"));
    }
}