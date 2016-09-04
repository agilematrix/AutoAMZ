package core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FunctionalFactory extends BrowserFactory{

	static String filePath="/home/cresto/Documents/agilematrixzone/ScreenShots";
	public static void takeScreenShot(String methodName)
	{    	
		File scrFile = ((TakesScreenshot)getDriver()).getScreenshotAs(OutputType.FILE);
		//The below method will save the screen shot in d drive with test method name 
		try {
			FileUtils.copyFile(scrFile, new File(filePath+methodName+".png"));
			System.out.println("***Placed screen shot in "+filePath+" ***");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void startBrowser(String url)
	{
		try {
			createInstance("firefox");
			getDriver().get(url);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 * @param element
	 * @return
	 */
	public String getText(By element)
	{

		return getDriver().findElement(element).getText();
	}
	public void startBrowser()
	{
		try {
			createInstance(Config.BROWSER_TYPE);
			getDriver().get(Config.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	 public static void highlightElement(By by)
	 {
		
		 WebElement element= getDriver().findElement(by);  
		  
		 
		  ((JavascriptExecutor) getDriver()).executeScript("arguments[0].style.border='3px dotted green'", element);
		  
		 }
	public void closeBrowser()
	{
		try {
			getDriver().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Click webElement
	 * @param element
	 */

	public void click(By element)
	{
		//WebElement waited=new WebDriverWait(getDriver(),10).until(ExpectedConditions.elementToBeClickable(element));
		try {
			System.out.println("Trying to Click " + element.toString());
			wait(element);
			getDriver().findElement(element).click();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public String  getTitle()
	{
		String title="";
		try {
			title=getDriver().getTitle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return title;
	}

	/*
	 * clear and send text into the field 
	 * 
	 */

	public void sendKeys(By field,String text)
	{
		try {
			wait(field);
	
			//	getDriver().findElement(field).clear();
			getDriver().findElement(field).sendKeys(text);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}



	public void waitForElement(By element)
	{
		try {
			new WebDriverWait(getDriver(),10).until(ExpectedConditions.elementToBeClickable(element));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void wait(By element)
	{
		try {
			new WebDriverWait(getDriver(),10).until(ExpectedConditions.presenceOfElementLocated(element));
			highlightElement(element);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
