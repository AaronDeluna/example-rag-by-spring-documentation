package com.example.pages;

import static com.codeborne.selenide.Selenide.$;
import com.codeborne.selenide.SelenideElement;

public class DuckDuckGoHomePage {
    private final SelenideElement searchInput = $("#search_form_input_homepage");

    public void search(String query) {
        searchInput.setValue(query).pressEnter();
    }
}
