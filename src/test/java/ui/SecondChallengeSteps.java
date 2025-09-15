package ui;

import io.cucumber.java.en.*;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SecondChallengeSteps {

    private WebDriver driver() { return UiHooks.DRIVER; }
    private WebDriverWait getWait(long seconds){
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    private void killAds() {
        try {
            ((JavascriptExecutor) driver()).executeScript("""
        (function(){
          const sel = ['#fixedban','#adplus-anchor','iframe[id^="google_ads_iframe"]',
                       'iframe[title="3rd party ad content"]','[id*="Ad.Plus"]','.adsbygoogle'];
          sel.forEach(s => document.querySelectorAll(s).forEach(el => el.remove()));
        })();
      """);
        } catch (Exception ignored) {}
    }

    private void scrollIntoView(By locator){
        WebElement el = getWait(10).until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    private void safeClick(By locator){
        WebElement el = getWait(15).until(ExpectedConditions.elementToBeClickable(locator));
        try { el.click(); }
        catch (ElementClickInterceptedException e){
            killAds();
            scrollIntoView(locator);
            ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", el);
        }
    }

    private String absUploadPath(String resourceRelativeInsideResources) {
        File f = Paths.get("src","test","resources",resourceRelativeInsideResources).toFile();
        Assertions.assertThat(f).exists();
        return f.getAbsolutePath();
    }

    private String randomString(int len){
        String alpha = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        var rnd = ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) sb.append(alpha.charAt(rnd.nextInt(alpha.length())));
        return sb.toString();
    }

    @When("eu vou para {string}")
    public void goTo(String url){
        driver().navigate().to(url);
        killAds();
    }

    @When("preencho o formulário com dados aleatórios e anexo {string}")
    public void fillPracticeForm(String relativeUpload) {
        waitVisible(By.id("userForm"), 10);
        killAds();

        String first = "John" + randomString(3);
        String last  = "Doe" + randomString(3);
        String email = "john."+randomString(4)+"@mail.com";
        String phone = String.valueOf(1000000000L + ThreadLocalRandom.current().nextLong(899999999L)); // 10 dígitos
        String address = "Rua " + randomString(5) + ", 123";

        driver().findElement(By.id("firstName")).sendKeys(first);
        driver().findElement(By.id("lastName")).sendKeys(last);
        driver().findElement(By.id("userEmail")).sendKeys(email);
        safeClick(By.xpath("//label[text()='Male']"));
        driver().findElement(By.id("userNumber")).sendKeys(phone.substring(0,10));

        safeClick(By.id("dateOfBirthInput"));
        driver().findElement(By.cssSelector("body")).click();

        WebElement subjects = driver().findElement(By.id("subjectsInput"));
        subjects.sendKeys("Maths");
        subjects.sendKeys(Keys.ENTER);
        safeClick(By.xpath("//label[text()='Reading']"));

        String abs = absUploadPath(relativeUpload);
        driver().findElement(By.id("uploadPicture")).sendKeys(abs);

        driver().findElement(By.id("currentAddress")).sendKeys(address);

        By stateCombo    = By.id("state");
        By stateListbox  = By.cssSelector("div[id^='react-select-3-listbox']");
        By stateOptionNCR= By.xpath("//div[contains(@id,'react-select-3-option') and normalize-space()='NCR']");
        openAndSelectReactOption(stateCombo, stateListbox, stateOptionNCR, "react-select-3-input", "NCR");

        By cityCombo     = By.id("city");
        By cityListbox   = By.cssSelector("div[id^='react-select-4-listbox']");
        By cityOptionDel = By.xpath("//div[contains(@id,'react-select-4-option') and normalize-space()='Delhi']");
        openAndSelectReactOption(cityCombo, cityListbox, cityOptionDel, "react-select-4-input", "Delhi");// ================================================
    }

    @When("submeto o formulário")
    public void submitPracticeForm() {
        scrollIntoView(By.id("submit"));
        safeClick(By.id("submit"));
    }

    @Then("devo ver o popup de confirmação e fechá-lo")
    public void validateAndCloseModal() {
        WebElement modal = getWait(10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-content")));
        Assertions.assertThat(modal.isDisplayed()).isTrue();

        safeClick(By.id("closeLargeModal"));
        getWait(10).until(ExpectedConditions.invisibilityOf(modal));
    }

    private String originalWindow;

    @When("eu clico no botão {string}")
    public void clickWindowsButton(String label) {
        originalWindow = driver().getWindowHandle();
        By locator = switch (label) {
            case "New Tab" -> By.id("tabButton");
            case "New Window" -> By.id("windowButton");
            case "New Window Message" -> By.id("messageWindowButton");
            default -> By.xpath("//button[normalize-space()='"+label+"']");
        };
        scrollIntoView(locator);
        safeClick(locator);
    }

    @Then("devo ver uma nova janela com o texto {string}")
    public void validateNewWindowText(String expected) {
        getWait(10).until(d -> driver().getWindowHandles().size() > 1);
        for (String h : driver().getWindowHandles())
            if (!h.equals(originalWindow)) driver().switchTo().window(h);

        WebElement h1 = getWait(10).until(ExpectedConditions.presenceOfElementLocated(By.id("sampleHeading")));
        Assertions.assertThat(h1.getText()).isEqualTo(expected);
    }

    @Then("fecho a nova janela e volto para a janela original")
    public void closeNewWindowAndBack() {
        driver().close();
        driver().switchTo().window(originalWindow);
    }


    private String createdEmail; // para localizar o registro criado
    private final List<String> createdEmails = new ArrayList<>(); // para o @bonus

    @When("eu crio um registro aleatório")
    public void createOneRecord() {
        killAds();
        safeClick(By.id("addNewRecordButton"));
        String first = "John"+randomString(3);
        String last = "Doe"+randomString(3);
        createdEmail = "john."+randomString(4)+"@mail.com";
        String age = "2" + ThreadLocalRandom.current().nextInt(0,9);
        String salary = String.valueOf(ThreadLocalRandom.current().nextInt(3000,12000));
        String dept = "QA";

        driver().findElement(By.id("firstName")).sendKeys(first);
        driver().findElement(By.id("lastName")).sendKeys(last);
        driver().findElement(By.id("userEmail")).sendKeys(createdEmail);
        driver().findElement(By.id("age")).sendKeys(age);
        driver().findElement(By.id("salary")).sendKeys(salary);
        driver().findElement(By.id("department")).sendKeys(dept);
        safeClick(By.id("submit"));

        By emailCell = By.xpath("//div[@class='rt-td' and text()='"+createdEmail+"']");
        getWait(10).until(ExpectedConditions.presenceOfElementLocated(emailCell));
    }

    @When("eu edito o registro criado")
    public void editCreatedRecord() {
        By row = By.xpath("//div[@class='rt-td' and text()='"+createdEmail+"']/ancestor::div[@class='rt-tr-group']");
        WebElement r = getWait(10).until(ExpectedConditions.presenceOfElementLocated(row));
        WebElement editBtn = r.findElement(By.cssSelector("span[title='Edit'], svg[title='Edit'], [id^='edit-record-']"));
        editBtn.click();

        WebElement dept = getWait(5).until(ExpectedConditions.visibilityOfElementLocated(By.id("department")));
        dept.clear(); dept.sendKeys("Automation");
        safeClick(By.id("submit"));

        By emailCell = By.xpath("//div[@class='rt-td' and text()='"+createdEmail+"']");
        getWait(10).until(ExpectedConditions.presenceOfElementLocated(emailCell));
    }

    @Then("eu deleto o registro criado")
    public void deleteCreatedRecord() {
        By row = By.xpath("//div[@class='rt-td' and text()='"+createdEmail+"']/ancestor::div[@class='rt-tr-group']");
        WebElement r = getWait(10).until(ExpectedConditions.presenceOfElementLocated(row));
        WebElement delBtn = r.findElement(By.cssSelector("span[title='Delete'], svg[title='Delete'], [id^='delete-record-']"));
        delBtn.click();

        By emailCell = By.xpath("//div[@class='rt-td' and text()='"+createdEmail+"']");
        getWait(10).until(ExpectedConditions.invisibilityOfElementLocated(emailCell));
    }

    @When("eu crio {string} registros aleatórios")
    public void createNRecords(String nStr){
        int n = Integer.parseInt(nStr);
        for (int i = 0; i < n; i++) {
            safeClick(By.id("addNewRecordButton"));
            String first = "U"+randomString(4);
            String last  = "T"+randomString(3);
            String email = "u"+randomString(5)+"@mail.com";
            String age   = String.valueOf(ThreadLocalRandom.current().nextInt(20,60));
            String salary= String.valueOf(ThreadLocalRandom.current().nextInt(3000,20000));
            String dept  = "Dept"+randomString(3);

            driver().findElement(By.id("firstName")).sendKeys(first);
            driver().findElement(By.id("lastName")).sendKeys(last);
            driver().findElement(By.id("userEmail")).sendKeys(email);
            driver().findElement(By.id("age")).sendKeys(age);
            driver().findElement(By.id("salary")).sendKeys(salary);
            driver().findElement(By.id("department")).sendKeys(dept);
            safeClick(By.id("submit"));

            createdEmails.add(email);
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        }
    }

    @Then("eu deleto todos os registros criados")
    public void deleteAllCreated() {
        for (String email : createdEmails) {
            List<WebElement> rows = driver().findElements(By.xpath("//div[@class='rt-td' and text()='"+email+"']/ancestor::div[@class='rt-tr-group']"));
            if (rows.isEmpty()) continue;
            WebElement delBtn = rows.get(0).findElement(By.cssSelector("span[title='Delete'], svg[title='Delete'], [id^='delete-record-']"));
            delBtn.click();
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        for (String email : createdEmails) {
            List<WebElement> cells = driver().findElements(By.xpath("//div[@class='rt-td' and text()='"+email+"']"));
            Assertions.assertThat(cells).isEmpty();
        }
        createdEmails.clear();
    }

    private int readProgress() {
        WebElement bar = driver().findElement(By.cssSelector("#progressBar .progress-bar"));
        String text = bar.getText().trim();
        if (text.endsWith("%")) text = text.substring(0, text.length()-1);
        try { return Integer.parseInt(text); } catch (Exception e){ return -1; }
    }

    @When("eu inicio a progress bar")
    public void startProgress() { safeClick(By.id("startStopButton")); }

    @When("paro antes de 25 por cento")
    public void stopBefore25() {
        long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
            int p = readProgress();
            if (p >= 20 && p < 25) {
                safeClick(By.id("startStopButton"));
                break;
            }
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
    }

    @Then("valido que a barra está em no máximo 25 por cento")
    public void validateAtMost25() {
        int p = readProgress();
        System.out.println(">>> progress: " + p + "%");
        Assertions.assertThat(p).isBetween(0, 25);
    }

    @When("inicio novamente até 100 por cento")
    public void startUntil100() {
        safeClick(By.id("startStopButton"));
        getWait(15).until(d -> readProgress() >= 100);
    }

    @Then("eu reseto a progress bar")
    public void resetProgressBar() {
        List<WebElement> resetCandidates = driver().findElements(By.id("resetButton"));
        if (!resetCandidates.isEmpty()) {
            resetCandidates.get(0).click();
        } else {
            WebElement btn = driver().findElement(By.id("startStopButton"));
            if (btn.getText().toLowerCase().contains("reset")) btn.click();
        }
        Assertions.assertThat(readProgress()).isBetween(0, 1);
    }

    @When("eu organizo a lista na ordem crescente")
    public void sortAscending() {
        By itemsSel = By.cssSelector("#demo-tabpane-list .list-group-item");
        getWait(10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(itemsSel));
        List<WebElement> items = driver().findElements(itemsSel);

        List<String> desired = List.of("One","Two","Three","Four","Five","Six");

        Actions actions = new Actions(driver());
        for (int i = 0; i < desired.size(); i++) {
            items = driver().findElements(itemsSel);
            String want = desired.get(i);
            if (items.get(i).getText().trim().equals(want)) continue;

            WebElement source = items.stream().filter(e -> e.getText().trim().equals(want)).findFirst().orElse(null);
            WebElement target = items.get(i);
            if (source == null) continue;

            try {
                actions.clickAndHold(source).moveToElement(target).pause(Duration.ofMillis(200)).release().perform();
            } catch (Exception e) {
                ((JavascriptExecutor) driver()).executeScript("""
          function dnd(from,to){
            const dt = new DataTransfer();
            const dragStart = new DragEvent('dragstart',{bubbles:true,dataTransfer:dt});
            const drop = new DragEvent('drop',{bubbles:true,dataTransfer:dt});
            const dragEnd = new DragEvent('dragend',{bubbles:true,dataTransfer:dt});
            from.dispatchEvent(dragStart); to.dispatchEvent(drop); from.dispatchEvent(dragEnd);
          }
          dnd(arguments[0], arguments[1]);
        """, source, target);
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
    }

    @Then("a lista deve estar na ordem {string}")
    public void validateOrder(String csv) {
        List<String> expected = Arrays.stream(csv.split(",")).map(String::trim).collect(Collectors.toList());
        List<WebElement> items = driver().findElements(By.cssSelector("#demo-tabpane-list .list-group-item"));
        List<String> got = items.stream().map(e -> e.getText().trim()).collect(Collectors.toList());
        Assertions.assertThat(got).isEqualTo(expected);
    }

    private WebElement waitVisible(By locator, long sec) {
        return new WebDriverWait(driver(), java.time.Duration.ofSeconds(sec))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void openReactComboWithRetry(By comboLocator, By listboxLocator, String inputId) {
        for (int i = 0; i < 4; i++) {
            killAds();
            scrollIntoView(comboLocator);
            safeClick(comboLocator);
            try { Thread.sleep(140); } catch (InterruptedException ignored) {}

            if (!driver().findElements(listboxLocator).isEmpty()) {
                WebElement lb = driver().findElement(listboxLocator);
                if (lb.isDisplayed()) return;
            }
            if (!driver().findElements(By.id(inputId)).isEmpty()) return;
        }

        WebElement combo = driver().findElement(comboLocator);
        ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", combo);
        long end = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < end) {
            boolean lbOk = !driver().findElements(listboxLocator).isEmpty()
                    && driver().findElement(listboxLocator).isDisplayed();
            boolean inOk = !driver().findElements(By.id(inputId)).isEmpty();
            if (lbOk || inOk) return;
            try { Thread.sleep(120); } catch (InterruptedException ignored) {}
        }
        throw new TimeoutException("React Select não abriu: " + comboLocator.toString());
    }


    private void selectReactOptionOrType(By optionLocator, String inputId, String text) {
        try {
            WebElement opt = new WebDriverWait(driver(), java.time.Duration.ofSeconds(5))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(optionLocator));
            opt.click();
            return;
        } catch (org.openqa.selenium.TimeoutException | org.openqa.selenium.ElementClickInterceptedException ignored) {}

        WebElement inp = driver().findElement(By.id(inputId));
        ((org.openqa.selenium.JavascriptExecutor) driver())
                .executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].focus();", inp);
        inp.sendKeys(text);
        inp.sendKeys(org.openqa.selenium.Keys.ENTER);
    }

    private void openAndSelectReactOption(By comboLocator,
                                          By listboxLocator,
                                          By optionLocator,
                                          String inputId,
                                          String textToType) {
        for (int i = 0; i < 5; i++) {
            killAds();
            scrollIntoView(comboLocator);
            try { safeClick(comboLocator); } catch (Exception ignored) {}
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}

            boolean hasListbox = !driver().findElements(listboxLocator).isEmpty();
            boolean hasInput   = !driver().findElements(By.id(inputId)).isEmpty();
            boolean hasOption  = !driver().findElements(optionLocator).isEmpty();

            if (hasListbox || hasInput || hasOption) break;

            try {
                WebElement combo = driver().findElement(comboLocator);
                ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", combo);
            } catch (Exception ignored) {}
        }

        for (int i = 0; i < 3; i++) {
            List<WebElement> opts = driver().findElements(optionLocator);
            if (!opts.isEmpty()) {
                WebElement opt = opts.get(0);
                ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView({block:'center'});", opt);
                try {
                    new WebDriverWait(driver(), Duration.ofSeconds(1))
                            .until(ExpectedConditions.elementToBeClickable(opt));
                    opt.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", opt);
                }
                return;
            }
            try { Thread.sleep(120); } catch (InterruptedException ignored) {}
        }

        List<WebElement> inputs = driver().findElements(By.id(inputId));
        if (!inputs.isEmpty()) {
            WebElement inp = inputs.get(0);
            ((JavascriptExecutor) driver()).executeScript(
                    "arguments[0].scrollIntoView({block:'center'}); arguments[0].focus();", inp);
            try {
                inp.clear();
            } catch (Exception ignored) {}
            inp.sendKeys(textToType);
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            inp.sendKeys(Keys.ENTER);
            return;
        }

        WebElement opt = new WebDriverWait(driver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.presenceOfElementLocated(optionLocator));
        ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", opt);
    }
}
