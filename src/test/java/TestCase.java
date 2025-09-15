import io.percy.selenium.Percy;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class TestCase {
    @Test
    public void test() throws Exception {
        String username =  System.getenv("BROWSERSTACK_USERNAME");
        String accesskey =  System.getenv("BROWSERSTACK_ACCESS_KEY");
        ArrayList<Integer> widths = new ArrayList();

        MutableCapabilities capabilities = new MutableCapabilities();
        HashMap<String, Object> bstackOptions = new HashMap<String, Object>();
        capabilities.setCapability("browserName", "Chrome");
        bstackOptions.put("os", "Windows");
        bstackOptions.put("osVersion", "10");
        bstackOptions.put("browserVersion", "120.0");
        bstackOptions.put("consoleLogs", "info");
        bstackOptions.put("projectName", "Elsevier");
        bstackOptions.put("buildName", "Percy Status Sync");
        bstackOptions.put("sessionName", "sampleTest"); 
        capabilities.setCapability("bstack:options", bstackOptions);

        WebDriver driver = new RemoteWebDriver(new URL("https://"+username+":"+accesskey+"@hub.browserstack.com/wd/hub"),capabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();

        try{
            Percy percySnapshots = new Percy(driver);
            SessionId sessionId = ((RemoteWebDriver)driver).getSessionId();
            storeSessionID(sessionId.toString());

            driver.get("https://bstackdemo.com/");
            percySnapshots.snapshot("Homepage", widths);
            driver.findElement(By.id("signin")).click();
            driver.findElement(By.cssSelector("#username input")).sendKeys("image_not_loading_user", Keys.ENTER);
            driver.findElement(By.cssSelector("#password input")).sendKeys("testingisfun99", Keys.ENTER);
            driver.findElement(By.id("login-btn")).click();
            String productName = "iPhone 12";
            List<WebElement> allProductsList = driver.findElements(By.cssSelector(".shelf-item"));
            for(int i=0;i<allProductsList.size();i++){
                WebElement selectProduct = allProductsList.get(i);
                if(selectProduct.findElement(By.cssSelector(".shelf-item__title")).getText().equals(productName)){
                    selectProduct.findElement(By.cssSelector(".shelf-item__buy-btn")).click();
                    System.out.println(productName + " added to bag!");
                    break;
                }else{
                    continue;
                }
            }
            //driver.findElement(By.cssSelector(".float-cart__close-btn")).click();
            driver.findElement(By.cssSelector(".buy-btn")).click();
            driver.findElement(By.id("firstNameInput")).sendKeys("Nithya");
            driver.findElement(By.id("lastNameInput")).sendKeys("Mani");
            driver.findElement(By.id("addressLine1Input")).sendKeys("Mumbai");
            driver.findElement(By.id("provinceInput")).sendKeys("Maharashtra");
            driver.findElement(By.id("postCodeInput")).sendKeys("400080");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            String confirmationMessage = driver.findElement(By.id("confirmation-message")).getText();
            String orderId = driver.findElement(By.cssSelector(".checkout-form div:nth-child(2) strong")).getText();
            Assert.assertTrue(confirmationMessage.contains("successfully"));
            percySnapshots.snapshot("Checkout page", widths);
        }catch (ClassCastException e){
            System.out.printf(e.getMessage());
        }

        driver.quit();
    }

    public void storeSessionID(String sessionID) throws IOException {
        try (OutputStream output = new FileOutputStream("src/test/resources/session-data/currentSessionID.properties")) {
            Properties prop = new Properties();
            prop.setProperty("sessionId", sessionID);
            prop.store(output, null);
            System.out.println(prop);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
