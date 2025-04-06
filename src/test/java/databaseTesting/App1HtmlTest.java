package databaseTesting;

import java.io.IOException;
import java.nio.file.Files;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        driver = new ChromeDriver();

        // Open the HTML file in the browser
        driver.get("file:///C:/Users/janal/Workspace/eclipse-workspace/TMP/SeleniumTests/src/main/resources/App1.html");
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
}
