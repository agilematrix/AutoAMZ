package core;

import org.openqa.selenium.WebDriver;

public class WebDriverFactory {
	
	private static  ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>() ;
	 
	    public static WebDriver getDriver() {	    	
	        return webDriver.get();
	    }
	 
	    static void setWebDriver(WebDriver driver) {
	        webDriver.set(driver);
	    }
	 
	

}
