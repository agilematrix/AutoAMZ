package report;

import org.testng.Assert;
import org.testng.Reporter;

import core.MessageType;
import core.Utilities;

/**
 * Class to perform console reporting with 
 * 
 *
 */
public class ConsoleReport implements iLogReport {

	Utilities utils = new Utilities();
	private static final String PACKAGE_PREFIX = "com.ctm.";
	private static final String ID_PREFIX = " ID_";
	private static final String CLOSE_COLOR_TAG_PREFIX = "[30m";


	/**
	 * Logs report with status.
	 * Set to 'true' to pass
	 * Set to 'false' to fail
	 */
	public void logReportWithStatus(boolean status, String message) {
		String callerMethodName = null;
		try {
			StackTraceElement[] sTElements = Thread.currentThread().getStackTrace();
			for (StackTraceElement sTElement : sTElements) {
				String className = sTElement.getClassName();
				if(className.startsWith(PACKAGE_PREFIX)) {
					callerMethodName = sTElement.getMethodName();
				}
			}
			String uniqueID = utils.generateUniqueID();
			Reporter.log(callerMethodName+": "+ message + ID_PREFIX + uniqueID);
			MessageType messagetype = MessageType.getMessageTypeFromStatus(status);
			String color = MessageType.getColor(messagetype);
			System.out.println((char) 27 + "[" + color + "m************ " + callerMethodName + ": " + message + " ************\n\n"
					+ (char) 27 + CLOSE_COLOR_TAG_PREFIX);
			if (!status) {
				Assert.fail(message);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Logs Test message without setting status
	 */
	public void logTestMessage(String message) {
		try {
			System.out.println((char) 27 + "[34m************ "
					+ message + " ************\n\n" + (char) 27
					+ CLOSE_COLOR_TAG_PREFIX);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void logReportWithStatusAndException(boolean status, String message, Error e) {
		String uniqueID = utils.generateUniqueID();
		Reporter.log(message + ID_PREFIX + uniqueID);
		MessageType messagetype = MessageType.getMessageType(message);
		String color = MessageType.getColor(messagetype);
		System.out.println((char) 27 + "[" + color + "m************ " + message + " ************\n\n"
				+ (char) 27 + CLOSE_COLOR_TAG_PREFIX);
		if (!status) {
			e.printStackTrace();
			Assert.fail(message);
		}

	}


}
