package core;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;


public class BrowserFactory extends WebDriverFactory {

	public  WebDriver createInstance(String browserName) {

		String downloadPath="/home/cresto/Documents/agilematrixzone";
		WebDriver driver = null;
		if (browserName.toLowerCase().contains("firefox")) {
			
			FirefoxProfile profile=new FirefoxProfile();
			 // Set preferences for file type 
			profile.setPreference("browser.link.open_newwindow", 2); //0,1 gives error 
			profile.setPreference("browser.helperApps.neverAsk.openFile", "application/octet-stream");
			profile.setPreference("browser.download.folderList", 2);
			profile.setPreference("browser.download.manager.showWhenStarting", false);
			profile.setPreference("browser.download.dir", downloadPath);
			profile.setPreference("browser.helperApps.neverAsk.openFile",
					"text/csv,application/x-msexcel,application/excel,application/x-excel,application/pdf,application/vnd.ms-excel,image/png,image/jpeg,text/html,text/plain,application/msword,application/xml");
			profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
	"text/csv,application/x-msexcel,application/excel,application/x-excel,application/vnd.ms-excel,application/pdf,image/png,image/jpeg,text/html,text/plain,application/msword,application/xml");
			profile.setPreference("browser.helperApps.alwaysAsk.force", false);
			profile.setPreference("browser.download.manager.alertOnEXEOpen", false);
			profile.setPreference("browser.download.manager.focusWhenStarting", false);
			profile.setPreference("browser.download.manager.useWindow", false);
			profile.setPreference("browser.download.manager.showAlertOnComplete", false);
			profile.setPreference("browser.download.manager.closeWhenDone", false);
			
			
			
		    System.setProperty("webdriver.firefox.marionette","/home/cresto/Documents/agilematrixzone/geckodriver");
			DesiredCapabilities capabilities = DesiredCapabilities.firefox();
			capabilities.setCapability("marionette", true);
			capabilities.setCapability(FirefoxDriver.PROFILE, profile);
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
		
		
		  
		// Open browser with profile                   
	//	WebDriver driver=new FirefoxDriver(profile);
	//	 EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);		 
		//	EventHandler handler = new EventHandler();
		//	eventDriver.register(handler);
		
			/*eventDriver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			eventDriver.manage().window().maximize();*/
		setWebDriver(driver);
		return getDriver();
	}

}
