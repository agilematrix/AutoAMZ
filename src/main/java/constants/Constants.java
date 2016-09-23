package constants;

import core.Utilities;

public  class Constants {
	
	private static final String ctmHomeDir = Utilities.getOSDirectory() + "/CTM_Automation";
	public static final int ELEMENT_WAIT_TIME = 20;
	public static final String passText = "PASS: ";
	public static final String failText = "FAIL: ";
	public static final String verificationHighlightColor = "magenta";
	//public static final String gridHub = "http://SAx7nLkcZ0JCWZj6VHa8RTKX9Umh1SIK:i6HwHSzxJ8ZvY8sJFtInciab3niV5Rve@OD.gridlastic.com:80/wd/hub";
	//public static final String gridHub = "http://localhost:8007/wd/hub";
	//public static final String gridHub = System.getProperty("gridURL", "http://localhost:8007/wd/hub");
	public static final String gridHub = System.getProperty("gridURL", "http://SAx7nLkcZ0JCWZj6VHa8RTKX9Umh1SIK:i6HwHSzxJ8ZvY8sJFtInciab3niV5Rve@OD.gridlastic.com:80/wd/hub");
	public static final String imagesDirectory = ctmHomeDir + "/mediacontent/screenshots";
	public static final String chromeDriverPath = "/Automation/tools/chromedriver_win32/chromedriver.exe";
	public static final String ieDriverPath = "/Automation/tools/IEDriverServer_Win32_2.45.0/IEDriverServer.exe";
	public static final String firefoxVersion = "33";
	public static final String versionMismatchAction = "warn";
	public static final String screenshotFilePath = ctmHomeDir + "/mediacontent/screenshots/";
	public static final String configFileName = ctmHomeDir + "/config/config.properties";
	public static final String reportDirectory = ctmHomeDir + "/localreport/";
	public static final String archieveDirectory = ctmHomeDir + "/localreport/archive/";
	public static final String reportFileName = "CTM_LocalReport.html";
	public static final String cssFile = "./resources/table.css";
	public static final String mobileAppPath = ctmHomeDir + "/tools/";
	

}
