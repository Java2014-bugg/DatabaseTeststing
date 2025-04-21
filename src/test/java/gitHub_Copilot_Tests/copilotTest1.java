package gitHub_Copilot_Tests;

import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class copilotTest1 {
	
	WebDriver driver = new ChromeDriver();

	@BeforeMethod
	public void setUp() {
		// Set up the WebDriver (e.g., ChromeDriver)
		System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
		
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.manage().window().maximize();
	}
	
	@AfterMethod
	public void tearDown() {
		// Close the browser
		driver.quit();
	}
	
	@Test
	public void testExample1() {
		// Navigate to a website
		driver.get("https://www.example.com");
		
		
		
		
		
	}
	
	//Method to take and save a screenshot
	public void takeScreenshot(String filePath) {
		File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			File destinationFile = new File(filePath);
			com.google.common.io.Files.copy(screenshot, destinationFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//
	
}
