package core;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmentable;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;

import core.MessageType;
import core.Utilities;
@Augmentable
public class FunctionalFactory extends BrowserFactory{
	private static String passText = constants.Constants.passText;
	private static String failText = constants.Constants.failText;
	Utilities utils = new Utilities();
	private static boolean enableConsoleOutput = true;
	private String randomUUID;
	static String filePath="/home/cresto/Documents/agilematrixzone/ScreenShots";
	protected static Map<String,String> objMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
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
	public boolean isElementPresent(By element)
	{
		return  getDriver().findElement(element).isDisplayed();

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
	public void click(By locator) {
		try {
			getDriver().findElement(locator).click();

			logReportAndFailIfNeeded(passText + "Click-> " + getObjName(locator) );
		} catch (Throwable t) {
			logReportAndFailIfNeeded(failText + "Click-> " + getObjName(locator) );

		}
	}
	public void logReportAndFailIfNeeded(String message) {
		MessageType messagetype = MessageType.getMessageType(message);
		logReportAndFailIfNeeded(message, messagetype);
	}
	public void logReportAndFailIfNeeded(String message, MessageType messagetype) {
		try {
			String uniqueID = utils.generateUniqueID();
			Reporter.log(message + " ID_" + uniqueID);
			randomUUID = uniqueID;
			if (getDriver() != null) {
				//	takeScreenshot();
			}
			if (enableConsoleOutput) {
				String color = MessageType.getColor(messagetype);
				System.out.println((char) 27 + "[" + color + "m************ " + message + " ************\n\n" + (char) 27 + "[30m");
				if (messagetype == MessageType.FAIL) {
					Assert.fail(message);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getObjName(By locator) {
		return objMap.get(locator.toString());
	}

	/**
	 * Click webElement
	 * @param element
	 */

	/*	public void click(By element)
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
	 */


	/**
	 * 
	 * Select dropdown element using visible Text 
	 * @param webElement
	 * @param visibleText
	 */
	public void selectByVisibleText (By webElement,String visibleText)
	{
		try {
			Select select=new Select(getDriver().findElement(webElement));
			select.selectByVisibleText(visibleText);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Select dropdown element using index
	 * @param webElement
	 * @param index
	 */
	public void selectByIndex (By webElement,int index)
	{
		try {
			Select select=new Select(getDriver().findElement(webElement));
			select.selectByIndex(index);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * select drop down element using by value 
	 * @param webElement
	 * @param value
	 */

	public void selectByValue(By webElement,String value)
	{
		try {
			Select select=new Select(getDriver().findElement(webElement));
			select.selectByValue(value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
