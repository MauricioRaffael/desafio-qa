package ui;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

public class UiHooks {
    public static WebDriver DRIVER;

    @Before("@ui")
    public void before() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        if (Boolean.getBoolean("headless")) {
            opts.addArguments("--headless=new");   // se você já usa, mantenha
            opts.addArguments("--window-size=1920,1080"); // <<< ADICIONE
            opts.addArguments("--force-device-scale-factor=1");
        }
        DRIVER = new ChromeDriver(opts);
    }

    @After("@ui")
    public void after() {
        if (DRIVER != null) DRIVER.quit();
    }
}
