package com.scrapspringboot.selenium.conf;

import jakarta.annotation.PostConstruct;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfiguration {

    @PostConstruct
    void postConstruct() {
        System.setProperty("webdriver.chrome.driver", "chromedriver-win32/chromedriver.exe");
    }

    @Bean
    public ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        return options;
    }
}
