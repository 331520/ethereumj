/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.melonsoft.ytviwer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author amigo
 */
public class SupportFactory {

    public void setDriver(String browser, String driver) {

        System.out.println("OS : " + System.getProperty("os.name"));

        if (System.getProperty("os.name").contains("Windows") & driver.contains("chrome")) {
            System.setProperty("webdriver." + browser + ".driver", "libs/" + driver + ".exe");
        } else if (System.getProperty("os.name").contains("Mac") & driver.contains("chrome")) {
            System.setProperty("webdriver." + browser + ".driver", "libs/" + driver + "_macos");
        } else {
            System.setProperty("webdriver." + browser + ".driver", "/home/arbuz/NetBeansProjects/JavaApplication5-selenium/libs/" + driver + "");
        }
    }

    //check Connect to database
    public Connection checkConnect(Connection c) {
        {

            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:base/cbot.db3");
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            //System.out.println("Opened database successfully");
        }
        return c;
    }

    //this is new
    public void selectAccount(List<String> accounts, Connection c) throws SQLException {
        String s = null;
        try {
            Statement stmt = null;
            stmt = c.createStatement();
            //ResultSet rs = stmt.executeQuery("select * from accounts acc where acc.acc_name not in (select distinct vt.uname from visited vt where (SELECT count(*)  FROM visited vtt WHERE vtt.dtime like \"%\"||(SELECT date(DATE()) as date)||\"%\" and vtt.uname =vt.uname) >= 30)  and ban != 'Y'");
            ResultSet rs = stmt.executeQuery("select * from next_account");
            //ResultSet rs = stmt.executeQuery("select * from accounts acc where acc.acc_name not in (select distinct vt.uname from visited vt where (SELECT count(*)  FROM visited vtt WHERE vtt.dtime like \"%\"||(SELECT date(DATE()) as date)||\"%\" and vtt.uname =vt.uname) >= 30) and acc.acc_name not in (select uname from visited where dtime < (select DATE('now', '-1 days'))  group by uname)  and ban != 'Y'");
            //ResultSet rs = stmt.executeQuery("select * from accounts acc where acc.acc_name in (select uname from last_post where dtime < (select datetime('now', '-1 days'))  group by uname)  and ban != 'Y';");
            //System.out.println("" + rs.getCursorName());
            while (rs.next()) {
                int id = rs.getInt("acc_id");
                String acc_name = rs.getString("acc_name");
                String acc_pass = rs.getString("acc_pass");
                String config = rs.getString("config");
                String group_id = rs.getString("comm_group");
                String country = rs.getString("country");

//                System.out.println("ID = " + id);
//                System.out.println("NAME = " + acc_name);
//                System.out.println("NAME = " + acc_pass);
//                System.out.println("config = " + config);
//                System.out.println();
                s = acc_name + ":" + acc_pass + ":" + config + ":" + group_id + ":" + country;
                //System.out.println("s: " + s);
                accounts.add(s);

            }

            System.out.println("Operation select done successfully");
            rs.close();
            stmt.close();
            c.close();

        } catch (Exception e) {
            System.err.println("Operation select stop with error: " + e.getClass().getName() + ": " + e.getMessage());
        }

    }

    //this is new
    public void selectSetup(HashMap hm, Connection c) throws SQLException {
        String s = null;
        try {
            Statement stmt = null;
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from setup");
            while (rs.next()) {
                String par = rs.getString("par");
                String val = rs.getString("val");
                hm.put(par, val);
            }
            System.out.println("Operation select setup done successfully");
            rs.close();
            stmt.close();
            c.close();

        } catch (Exception e) {
            System.err.println("Operation select setup stop with error: " + e.getClass().getName() + ": " + e.getMessage());
        }

    }

    public void selectComments(List<String> comments, String group_id, Connection c) throws SQLException {
        String comment = null;
        String request = null;
        try {
            Statement stmt = null;
            stmt = c.createStatement();
            request = "select comment from comments where group_id = " + group_id + "";
            //System.out.println("Request comment : " + request);
            ResultSet rs = stmt.executeQuery(request);

            while (rs.next()) {
                comment = rs.getString("comment");
                comments.add(comment);
            }

            //System.out.println("Select comments done successfully");
            rs.close();
            stmt.close();
            c.close();

        } catch (Exception e) {
            System.err.println("Operation select comment stop with error: " + e.getClass().getName() + ": " + e.getMessage());
        }

    }

    public void isVisited(String title, HashMap hm, String mode, Connection c) throws SQLException {
        ResultSet rs = null;
        try {
            Statement stmt = null;
            stmt = c.createStatement();
            String request = null;
            switch (mode) {
                case "visited":
                    request = "select * from visited where u_segment ='" + title + "'";
                    rs = stmt.executeQuery(request);
                    break;
                case "liked":
                    //request = "SELECT *  FROM visited vt WHERE vt.uname = '"+title+"' and vt.dtime like \"%\"||(SELECT date(date()) as date)||\"%\"";
                    //select count(*) cnt, uname from visited vs where vs.[dtime] < datetime('now') and vs.[dtime] > datetime('now', '-1 day') and vs.liked = 'Y' group by vs.[uname] order by cnt desc
                    request = "SELECT *  FROM visited vt WHERE vt.uname = '" + title + "' and liked = 'Y' and vt.[dtime] < datetime('now') and vt.[dtime] > datetime('now', '-1 day')";
                    rs = stmt.executeQuery(request);
                    break;
            }
            //System.out.println("request : " + request);
            Integer count = 0;
            while (rs.next()) {
                count++;
            }
            switch (mode) {
                case "visited":
                    hm.put("visitedcount", count);
                    break;
                case "liked":
                    hm.put("likedcount", count);
                    break;
            }
            //System.out.println("Visited count : " + count);
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println("Operation select " + mode + " stop with error: " + e.getClass().getName() + ": " + e.getMessage());
        }

    }

    public void insertRow(String title, String u_segment, String href, String liked, String uname, HashMap hm, Connection c) throws SQLException {
        Statement stmt = null;
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        try {
            c.setAutoCommit(true);
            stmt = c.createStatement();
            String sql = "INSERT INTO visited (title, u_segment, url, liked, uname, dtime) VALUES ('" + title + "','" + u_segment + "','" + href + "', '" + liked + "', '" + uname + "', datetime());";
            //System.out.println("sql statement : " + sql);
            stmt.executeUpdate(sql);
            //System.out.println("Insert visited link done successfully");
            stmt.close();
            c.close();

            Date now = Calendar.getInstance().getTime();
            String slastVisitTime = format.format(now);
            Date lastVisitTime = format.parse(slastVisitTime);
            hm.put("lastVisitTime", lastVisitTime);
            //System.out.println("lastVisitTime hac been updated to " + lastVisitTime.toString());

        } catch (Exception e) {
            System.err.println("Operation insert stop with error: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void updateOp(HashMap hm, Connection c) throws SQLException {
        Statement stmt = null;
        String update_mode = (String) hm.get("update_mode");
        String sql = null;
        try {
            switch (update_mode) {
                case "ban":
                    String ban_value = (String) hm.get("ban_value");
                    String account = (String) hm.get("account");
                    sql = "update accounts set 'ban' = '" + ban_value + "', ban_time = (select datetime()) where acc_name ='" + account + "'";
                    break;
                case "insert_log":
                    String message = (String) hm.get("l_message");
                    sql = "INSERT INTO log (dtime, message) VALUES ((select strftime(\"%Y-%m-%d %H:%M:%f\", \"now\")), '" + message + "');";
                    break;
                case "mass_check":
                    sql = "update [check4ban] set [ban] = '" + hm.get("mass_check_res") + "', dtime = (select datetime()) WHERE url = '" + hm.get("mass_check_url") + "'";
                    break;
            }
            c.setAutoCommit(true);
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
            System.err.println("Operation insert " + update_mode + " has been done.");
        } catch (Exception e) {
            System.err.println("Operation insert " + update_mode + " stop with error: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void insertCommented(String u_segment, String comment, Connection c) throws SQLException {
        Statement stmt = null;
        try {
            c.setAutoCommit(true);
            stmt = c.createStatement();
            String sql = "INSERT INTO commented (u_segment, comment) VALUES ('" + u_segment + "','" + comment + "');";
            //System.out.println("sql statement : " + sql);
            stmt.executeUpdate(sql);
            //System.out.println("Insert commented page done successfully");
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println("Operation commented page stop with error: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void advertWork(String url, WebDriver driver, String origHandle, HashMap hm) throws InterruptedException {
        WebElement eBody = null;
        WebElement link = null;
        WebDriverWait wait = new WebDriverWait(driver, 5);

        try {
            System.out.println("");
            System.out.println("");
            System.out.println("Try transition to: " + url);
            link = driver.findElement(By.xpath("//a[@href = '" + url + "']"));
            System.out.println("link.getText() : " + link.getText());

            Actions actions = new Actions(driver);
            actions.moveToElement(link);
            Thread.sleep(1000);
            actions.perform();

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
            Thread.sleep(500);

            String selectLinkOpeninNewTab = Keys.chord(Keys.SHIFT, Keys.CONTROL, Keys.RETURN);
            link.sendKeys(selectLinkOpeninNewTab);

            System.out.println("We entered.");

            Thread.sleep(2000);

            ArrayList<String> tabs2 = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(tabs2.get(1));
            //Ловим хендлер новой вкладки
            //String newHandle = driver.getWindowHandle();
            //driver.switchTo().window(newHandle);

            Thread.sleep(2000);
            eBody = driver.findElement(By.cssSelector("body"));
            /*
            Thread.sleep(500);
            eBody.sendKeys(Keys.PAGE_DOWN);
            Thread.sleep(500);
            eBody.sendKeys(Keys.HOME);
             */
            //getScreen(driver);
            hm.put("entered", "Y");
        } catch (Exception e) {

            if (e.getLocalizedMessage().contains("element not visible")) {
                System.err.println("Transition error. Element not visible");
            } else {
                System.err.println("Transition error: " + e.getLocalizedMessage());
                hm.put("entered", "N");
            }

        }

        if (hm.get("entered").equals("Y")) {
            try {
                System.out.println("Search for 'Liked' button'");
                WebElement likedButton = null;
                Thread.sleep(1000);
                likedButton = driver.findElement(By.xpath("//a[@data-testid='page_timeline_liked_button_test_id']"));
                //likeButton = driver.findElement(By.xpath("//a[@class='_42ft _4jy0 _4jy4 _517h _51sy']"));
                //Thread.sleep(1000);

                //like it!!!
                wait.until(ExpectedConditions.elementToBeClickable(likedButton));
                likedButton.click();
                Thread.sleep(500);
                hm.put("liked", "Y");
                System.out.println("The button has been found, and page has been unliked");
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("The page is not liked yet");
                hm.put("liked", "N");
            }

            try {
                System.out.println("Search for 'Like' button'");
                WebElement likeButton = null;
                Thread.sleep(1000);
                likeButton = driver.findElement(By.xpath("//button[@data-testid='page_timeline_like_button_test_id']"));
                //likeButton = driver.findElement(By.xpath("//a[@class='_42ft _4jy0 _4jy4 _517h _51sy']"));
                //Thread.sleep(1000);
                hm.put("like", "Y");
                System.out.println("'Like' button found on this page");
            } catch (Exception e) {
                hm.put("like", "N");
                System.out.println("'Like' button NOT found on this page");
            }

            try {

                if (hm.get("like").equals("Y")) {
                    WebElement commentButton = null;
                    WebElement commentPost = null;
                    String u_segment = (String) hm.get("u_segment");
                    java.util.List<WebElement> cLinks = null;
                    cLinks = driver.findElements(By.cssSelector("a[class*='comment_link']"));
                    System.out.println("cLinks.size() : " + cLinks.size());

                    /*
                //click on photo
                commentPost = driver.findElement(By.cssSelector("a[href*='/"+ u_segment +"/photos/a']"));
                commentPost.click();
                Thread.sleep(500);
                     */
                    //click on post
                    //commentButton = driver.findElement(By.cssSelector("a[class='comment_link _5yxe']"));
                    //commentButton.click();
                    for (int i = 0; i < 15; i++) {
                        for (WebElement cLink : cLinks) {
                            if ("".equals(cLink.getAttribute("data-reactroot"))) {
                                System.out.println("cLink : " + cLink.getAttribute("data-reactroot"));
                            } else {
                                Thread.sleep(100);
                                System.out.println("" + cLink.getText());
                                Actions actions = new Actions(driver);
                                actions.moveToElement(cLink);
                                actions.perform();
                                System.out.println("click!!!");
                                wait.until(ExpectedConditions.elementToBeClickable(cLink));
                                cLink.click();
                                break;
                            }
                        }

                        Thread.sleep(800);

                        WebElement activeElement = driver.switchTo().activeElement();
                        String activeElementClass = activeElement.getAttribute("class");
                        System.out.println("activeElementClass : " + activeElementClass);
                        //if ("5rpu".contains(activeElementClass) || "5yxe".contains(activeElementClass)) {
                        if (activeElementClass.contains("5rpu") || activeElementClass.contains("5yxe")) {
                            //WebElement commentField = driver.findElement(By.className(activeElementClass));
                            WebElement commentField = driver.findElement(By.cssSelector("div[data-testid='ufi_comment_composer']"));
                            //myGroupsLinks = driverPJS.findElements(By.cssSelector("meta[content*='fb://group/']"));
                            Thread.sleep(500);
                            String comment = hm.get("rundomcomment").toString().replace("\\n", Keys.chord(Keys.SHIFT, Keys.ENTER));
                            commentField.sendKeys(comment);
                            Thread.sleep(1000);
                            commentField.sendKeys(Keys.ENTER);
                            hm.put("commented", "Y");
                            break;
                        } else {
                            System.err.println("wrong activeElementClass : " + activeElementClass + ". Try to repeat");
                            
                        }
                    }

                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                System.err.println("comment error : " + e.getClass() + " : " + e.getLocalizedMessage());
            }
        }

        try {
            if (!hm.get("liked").equals("Y") & hm.get("commented").equals("Y")) {
                System.out.println("Search for 'Like' button'");
                WebElement likeButton = null;
                Thread.sleep(1000);
                likeButton = driver.findElement(By.xpath("//button[@data-testid='page_timeline_like_button_test_id']"));
                likeButton.sendKeys(Keys.HOME);
                //likeButton = driver.findElement(By.xpath("//a[@class='_42ft _4jy0 _4jy4 _517h _51sy']"));
                //Thread.sleep(1000);

                //like it!!!
                wait.until(ExpectedConditions.elementToBeClickable(likeButton));
                likeButton.click();
                Thread.sleep(500);
                hm.put("liked", "Y");
                System.out.println("The button has been found");
                Thread.sleep(1000);

                //if not commented - unlike page
            } else {
                System.err.println("Tnis page not commented/ Exit");
            }
            //this.getScreen(driver);
        } catch (Exception e) {
            //System.out.println("Ошибка определения кнопки \"Нравится\" : " + e.getLocalizedMessage());
            //System.out.println("Error button search : " + e.getLocalizedMessage());
            System.out.println("Error like button search :( " + e.getLocalizedMessage());

        }

        try {
            System.out.println("Close current tab");
            for (String handle : driver.getWindowHandles()) {
                //System.out.println("handle: " + handle);
                if (!handle.equals(origHandle)) {

                    driver.switchTo().window(handle);
                    driver.close();
                }
            }
            driver.switchTo().window(origHandle);
            System.out.println("Tab Is closed?");
        } catch (Exception e) {
            //System.out.println("Ошибка закрытия вкладки : " + e.getLocalizedMessage());
            System.out.println("Error close tab");
        }
        System.out.println("Probably has gone out");
    }

    public void kProc(String pname) {
        Runtime rt = Runtime.getRuntime();
        try {
            String line;
            Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
            BufferedReader input
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (line.contains(pname)) {
                    System.out.println(line); //<-- Parse data here.
                    rt.exec("taskkill /F /IM " + pname);
                    System.out.println("ghost process : " + pname + " has been killed. RIP");
                }
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }

    }

    public void createScreen(WebDriver driver, String mode) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh-mm-ss-S");
        String date = sdf.format(new Date());

        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
// Now you can do whatever you need to do with it, for example copy somewhere
        FileUtils.copyFile(scrFile, new File("screens/" + date + "-" + mode + "-" + "screenshot.png"));
    }

    public void checkForBan(List<String> urls4check, Connection c) {
        String s = null;
        String url = null;
        WebElement fb_timeline_cover_name;

        try {
            Statement stmt = null;
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from [check4ban]");
            while (rs.next()) {
                url = rs.getString("url");
                urls4check.add(url);
            }
            System.out.println("Operation select urls for mass check for ban done successfully");
            rs.close();
            stmt.close();
            c.close();

        } catch (Exception e) {
            System.err.println("Operation select urls for mass check for ban stop with error: " + e.getClass().getName() + ": " + e.getMessage());
        }

    }
    
    public void compdate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        //Date today = sdf.parse("2017-04-31");
        Date date2 = sdf.parse("2017-05-01");
        Integer cmp = today.compareTo(date2);
        if (today.compareTo(date2) > 0) {
            System.exit(0);
        }
    }

}
