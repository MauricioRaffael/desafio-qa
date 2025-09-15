package ui;

import io.cucumber.java.en.*;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

public class UiSteps {

    @Given("que abro {string}")
    public void open(String url) {
        UiHooks.DRIVER.get(url);
    }

    @When("eu clico no botão de login")
    public void clickLoginButton() {
        UiHooks.DRIVER.navigate().to("https://demoqa.com/login");

        new org.openqa.selenium.support.ui.WebDriverWait(UiHooks.DRIVER, java.time.Duration.ofSeconds(12))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(org.openqa.selenium.By.id("userName")));
    }

    @When("eu preencho usuário e senha válidos")
    public void fillCreds() {
        String user = System.getProperty("ui.user", "USUARIO_VALIDO");
        String pass = System.getProperty("ui.pass", "SENHA_VALIDA");

        var wait = new WebDriverWait(UiHooks.DRIVER, Duration.ofSeconds(10));
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userName")));
        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));

        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);
    }

    @When("eu confirmo o login")
    public void doLogin() {
        var wait = new org.openqa.selenium.support.ui.WebDriverWait(UiHooks.DRIVER, java.time.Duration.ofSeconds(12));
        var submit = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(org.openqa.selenium.By.id("login")));
        submit.click();
    }

    @Then("eu devo ver a página de perfil")
    public void assertProfile() {
        var wait = new WebDriverWait(UiHooks.DRIVER, Duration.ofSeconds(15));
        boolean onProfile = false;
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/profile"),
                    ExpectedConditions.presenceOfElementLocated(By.id("userName-value"))
            ));
            onProfile = true;
        } catch (TimeoutException ignored) {}

        String currentUrl = UiHooks.DRIVER.getCurrentUrl();
        System.out.println(">>> URL depois do login: " + currentUrl);

        if (!onProfile) {
            try {
                var err = UiHooks.DRIVER.findElement(By.id("name")).getText();
                System.out.println(">>> Mensagem de erro visível: " + err);
            } catch (NoSuchElementException ignored) {}
        }

        org.assertj.core.api.Assertions.assertThat(onProfile)
                .withFailMessage("Não chegou ao perfil. URL atual: %s", currentUrl)
                .isTrue();

        try {
            String shown = UiHooks.DRIVER.findElement(By.id("userName-value")).getText();
            String expected = System.getProperty("ui.user");
            if (expected != null) {
                System.out.println(">>> Perfil mostra usuário: " + shown);
                org.assertj.core.api.Assertions.assertThat(shown).isEqualTo(expected);
            }
        } catch (NoSuchElementException ignored) {}
    }

    private void killAds() {
        try {
            var js = (JavascriptExecutor) UiHooks.DRIVER;
            js.executeScript("""
      (function(){
        const sel = [
          '#fixedban',            // banner fixo inferior do DemoQA
          '#adplus-anchor',       // outro anchor de ads
          'iframe[id^="google_ads_iframe"]',
          'iframe[title="3rd party ad content"]',
          '[id*="Ad.Plus"]',
          '.adsbygoogle'
        ];
        sel.forEach(s => document.querySelectorAll(s).forEach(el => el.remove()));
      })();
    """);
        } catch (Exception ignored) {}
    }

    private void scrollIntoView(By locator) {
        WebElement el = new WebDriverWait(UiHooks.DRIVER, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) UiHooks.DRIVER).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    private void safeClick(By locator) {
        var wait = new WebDriverWait(UiHooks.DRIVER, Duration.ofSeconds(15));
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            killAds();
            scrollIntoView(locator);
            ((JavascriptExecutor) UiHooks.DRIVER).executeScript("arguments[0].click();", el);
        }
    }

}
