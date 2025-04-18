package databaseTesting;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class App1HtmlTest {

	private WebDriver driver;

    @BeforeClass
    public void setUp() {
        // Set path to ChromeDriver (replace with your actual path) 
    	String os = System.getProperty("os.name").toLowerCase();
    	if (os.contains("win")) {
    	    System.setProperty("webdriver.chrome.driver", "C:\\Users\\Drivers\\chromedriver.exe");
    	} else {
    	    // On Linux (like GitHub Actions), chromedriver is in PATH after install
    	    System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
    	}

    	ChromeOptions options = new ChromeOptions();
    	options.addArguments("--headless=new"); // new headless mode
    	options.addArguments("--window-size=1920,1080"); // Makes sure elements are visible
    	options.addArguments("--no-sandbox");
    	options.addArguments("--disable-dev-shm-usage");
    	options.addArguments("--disable-gpu");
    	options.addArguments("--remote-allow-origins=*"); // sometimes helps with CI
    	
    	try {
    	    String tempProfileDir = Files.createTempDirectory("chrome-profile").toString();
    	    options.addArguments("--user-data-dir=" + tempProfileDir);
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	
        // Initialize WebDriver
        driver = new ChromeDriver(options);

        // Open the HTML file in the browser
        //driver.get("file:///C:/Users/janal/Workspace/eclipse-workspace/TMP/SeleniumTests/src/main/resources/App1.html");
        
        //Load HTML file from classpath resources to also work in GitHub Actions
        URL resource = getClass().getClassLoader().getResource("index.html");
        if (resource == null) {
            throw new RuntimeException("index.html not found in resources!");
        }
        driver.get(resource.toString());
        
        //  the test is hitting the deployed HTML page on Tomcat, not a local file. That’s how real-world deployment 
        //testing works
        //driver.get("https://4f5c-161-35-140-158.ngrok-free.app/App1.html"); // Later

        
        waitForPageToLoad();
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Title: " + driver.getTitle());
        
        takeScreenshot("App1 page.png");
        System.out.println("App1HtmlTest starts now");
    }
    
    @AfterClass
    public void tearDown() {
        // Close the browser
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1)
    public void testAddTask() {
    	try {
    		// Locate the input box and add button
    		WebElement taskInput = driver.findElement(By.id("taskInput"));
    		WebElement addButton = driver.findElement(By.xpath("//button[text()='Add Task']"));

    		// Add a new task
    		String taskName = "Test Task 1";
    		taskInput.sendKeys(taskName);
    		addButton.click();

    		// Verify that the task is added to the list
    		WebElement taskList = driver.findElement(By.id("taskList"));
    		WebElement addedTask = taskList.findElement(By.xpath("//li[contains(text(), '" + taskName + "')]"));
    		Assert.assertNotNull(addedTask, "Task was not added!");
    		
    	} catch (Exception e) {
            takeScreenshot("add_task_failed.png");
            throw e;  // re-throw so test still fails
        }	
    }

    @Test(priority = 2, dependsOnMethods = "testAddTask")
    public void testDeleteTask() {
        // Locate the task and delete button
        WebElement taskList = driver.findElement(By.id("taskList"));
        WebElement addedTask = taskList.findElement(By.xpath("//li[contains(text(), 'Test Task 1')]"));
        WebElement deleteButton = addedTask.findElement(By.xpath(".//button[text()='Delete']"));

        // Delete the task
        deleteButton.click();

        // Verify that the task is removed from the list
        boolean isTaskPresent = taskList.findElements(By.xpath("//li[contains(text(), 'Test Task 1')]")).isEmpty();
        Assert.assertTrue(isTaskPresent, "Task was not deleted!");
    }
    
    public void waitForPageToLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    }
    
    public void takeScreenshot(String filename) {
        if (driver instanceof TakesScreenshot) {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                Files.createDirectories(Paths.get("target/screenshots"));
                Files.copy(screenshot.toPath(), Paths.get("target/screenshots", filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void takeScreenshot2(String filename) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            Files.createDirectories(new File("screenshots").toPath());
            Files.copy(screenshot.toPath(), new File("screenshots/" + filename + ".png").toPath());
            System.out.println("Screenshot saved: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
