package core;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;


public class BrowserFactory extends WebDriverFactory {

	public  WebDriver createInstance(String browserName) {

		WebDriver driver = null;
		if (browserName.toLowerCase().contains("firefox")) {
			System.setProperty("webdriver.firefox.marionette","/home/cresto/Documents/agilematrixzone/geckodriver");
			DesiredCapabilities capabilities = DesiredCapabilities.firefox();
			capabilities.setCapability("marionette", true);
			driver = new FirefoxDriver(capabilities);

		}
		if (browserName.toLowerCase().contains("internet")) {
			driver = new InternetExplorerDriver();

		}
		if (browserName.toLowerCase().contains("chrome")) {
			System.setProperty("webdriver.chrome.driver","/home/cresto/Documents/agilematrixzone/chromedriver");
			DesiredCapabilities dc=DesiredCapabilities.chrome();
			dc.setPlatform(org.openqa.selenium.Platform.ANY);

			driver = new ChromeDriver(dc);

		}
		 EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);		 
			EventHandler handler = new EventHandler();
			eventDriver.register(handler);
		
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		driver.manage().window().maximize();

		setWebDriver(eventDriver);
		return getDriver();
	}

}
