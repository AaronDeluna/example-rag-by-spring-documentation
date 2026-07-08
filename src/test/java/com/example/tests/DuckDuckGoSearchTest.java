package com.example.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.Test;
import com.example.pages.DuckDuckGoHomePage;

import static com.codeborne.selenide.Selenide.*;

public class DuckDuckGoSearchTest {

    @Test
    public void testDuckDuckGoSearch() {
        // Setup
        Configuration.startMaximized = true;
        Configuration.timeout = 10000;
        open("https://duckduckgo.com");

        // Test steps
        DuckDuckGoHomePage home = new DuckDuckGoHomePage();
        home.search("Selenium WebDriver");

        // Verification: at least one result with class 'result__a'
        $$("a.result__a").shouldBe(Condition.sizeGreaterThan(0));
    }
}
