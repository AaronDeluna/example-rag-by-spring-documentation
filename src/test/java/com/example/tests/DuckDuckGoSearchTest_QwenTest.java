package com.example.tests;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.Condition;
import com.example.pages.DuckDuckGoHomePage;

import static com.codeborne.selenide.Selenide.*;

public class DuckDuckGoSearchTest_QwenTest {

    @Test
    public void testDuckDuckGoSearch_QwenTest() {
        // Setup
        Configuration.startMaximized = true;
        Configuration.timeout = 10000;
        open("https://duckduckgo.com");

        // Test steps
        DuckDuckGoHomePage home = new DuckDuckGoHomePage();
        home.search("Qwen");

        // Verification: at least one result with class 'result__a'
        $$("a.result__a").shouldBe(Condition.sizeGreaterThan(0));
    }
}
