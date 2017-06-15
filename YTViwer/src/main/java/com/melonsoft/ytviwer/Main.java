/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.melonsoft.ytviwer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author admin
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

        // TODO code application logic here
        System.out.println("It works!");
        SupportFactory mySupportFactory = new SupportFactory();

        String browser = "chrome";
        String drv = "chromedriver-2.29";
        WebDriver driver = null;

        //clean our ass
        mySupportFactory.kProc("openvpn.exe");
        mySupportFactory.kProc("chromedriver.exe");

        String PROXY = "45.76.144.113:8080";

        org.openqa.selenium.Proxy sProxy = new org.openqa.selenium.Proxy();
        sProxy.setHttpProxy(PROXY)
                .setFtpProxy(PROXY)
                .setSslProxy(PROXY);
        DesiredCapabilities capa = new DesiredCapabilities();
        capa.setJavascriptEnabled(true);

        capa.setCapability(CapabilityType.PROXY, sProxy);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);
        capa.setJavascriptEnabled(true);
        String userAgent = "Mozilla/5.0 (Windows NT 6.0) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.41 Safari/535.1";
        capa.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent);

        System.out.println("browser : " + browser);

        if (browser.equals("chrome")) {
            mySupportFactory.setDriver(browser, drv);
            driver = new ChromeDriver(capa);
        } else if (browser.equals("phantom")) {
            driver = new PhantomJSDriver(capa);
        }

        WebDriverWait wait = new WebDriverWait(driver, 5);

        try {
            driver.manage().deleteAllCookies();
            driver.manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);
            driver.manage().window().maximize();
        } catch (Exception e) {
            System.err.println("error manage driver : " + e.getLocalizedMessage());
        }

        driver.get("https://www.youtube.com/watch?v=9MXqKV6eR7o");
        WebElement playButton = driver.findElement(By.cssSelector("button[class*='ytp-play-button']"));
        System.out.println("playButton.getAttribute : " + playButton.getAttribute("aria-label"));
        WebElement duration = driver.findElement(By.className("ytp-time-duration"));
        System.out.println("duration : " + duration.getText());
        String[] seconds = duration.getText().split(":");
        System.out.println("" + seconds.length);
        Integer movieSecondsLenght = 0;

        //clip duration 
        switch (seconds.length) {
            case 3:
                movieSecondsLenght = (Integer.valueOf(seconds[0]) * 60 * 60) + (Integer.valueOf(seconds[1]) * 60) + (Integer.valueOf(seconds[1]));
                break;
            case 2:
                movieSecondsLenght = (Integer.valueOf(seconds[0]) * 60) + (Integer.valueOf(seconds[1]));
                break;
            case 1:
                movieSecondsLenght = (Integer.valueOf(seconds[1]));
                break;
        }
        System.out.println("movieSecondsLenght : " + movieSecondsLenght);

        //autoplay or not ?
        //<span class="ytp-time-current">2:19</span>
        String startClipTime = "0:00";
        String currentClipTime = "0:00";
        startClipTime = driver.findElement(By.className("ytp-time-current")).getText();
        System.out.println("startClipTime : " + startClipTime);
        Thread.sleep(10000);
        currentClipTime = driver.findElement(By.className("ytp-time-current")).getText();
        System.out.println("currentClipTime : " + currentClipTime);

        //if not - click
        if (currentClipTime.equals(startClipTime)) {
            playButton.click();
        }

        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(movieSecondsLenght);
        System.out.println("we will see " + randomInt + " seconds");

        Thread.sleep(randomInt * 1000);

        System.out.println("OK. Exit");

        driver.close();
        driver.quit();

    }

}
