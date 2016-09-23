package core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;



/**
 * This class contains all utility methods
 * @author praveen-bhasker
 *
 */
public class Utilities {
	
	private String propertyComments = null;

	/**
	 * Ignores the case while comparing s1 and s2
	 * @param actual
	 * @param expected
	 * @return returns true if found and false if not found
	 */
	public boolean containsIgnoreCase(String actual, String expected) {
		final int length = expected.length();
		if (length == 0)
			return true;
		final char firstLo = Character.toLowerCase(expected.charAt(0));
		final char firstUp = Character.toUpperCase(expected.charAt(0));
		for (int i = actual.length() - length; i >= 0; i--) {
			final char ch = actual.charAt(i);
			if (ch != firstLo && ch != firstUp)
				continue;

			if (actual.regionMatches(true, i, expected, 0, length))
				return true;
		}
		return false;
	}

	/**
	 * Generates Unique ID using GUID algorithm
	 * 
	 * @return Strings Returns a string containing the unique ID 
	 */
	public String generateUniqueID() {
		String randomUUID = "";
		try {
			UUID uuid = UUID.randomUUID();
			randomUUID = uuid.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return randomUUID;
	}

	/**
	 * Generates a string containing Date and Time information. This can be used
	 * when a unique data is required for input to the application. The format
	 * of the string returned is:
	 * <p>
	 * ddMMMyyyyhhmmssaa
	 * <p>
	 * <B>Example for January 01, 2014 at 12:55:01 PM:</B><br>
	 * 01Jan2014125501PM
	 *
	 * @return String containing the current Date and Time
	 */
	public String generateDate() {

		String dateString = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("ddMMMyyyyhhmmssaa");
			Date date = new Date();
			dateString = dateFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateString;
	}


	/**
	 * Executes the tasklist shell command to query for running processes that
	 * match the given process name and returns a list of running process that
	 * match processName The resulting list can be parsed for the process ID in
	 * order to kill the process.
	 * 
	 * @param processName
	 *            The name of the image file for the process
	 * @return String containing the list of processes with that image name that
	 *         are active on the host, or null if the process is not active
	 */

	public String checkRunningProcess(String processName) {
		InputStream in = null;
		InputStream bufferStream = null;
		Reader reader = null;
		try {
			Process process = Runtime.getRuntime().exec(
					"tasklist /FI \"IMAGENAME eq " + processName + "\"");
			in = process.getInputStream();
			bufferStream = new BufferedInputStream(in);
			reader = new InputStreamReader(bufferStream);
			char[] charr = new char[1024];
			StringBuffer stringBuffer = new StringBuffer();
			while (true) {
				int readerNo = reader.read(charr);
				if (readerNo <= 0) {
					break;
				}
				stringBuffer.append(charr, 0, readerNo);
			}
			String taskList = stringBuffer.toString();
			if (taskList.contains("INFO: No tasks are running which match the specified criteria.")) {
				return null;
			} else {
				return taskList;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			if (bufferStream != null) {
				try {
					bufferStream.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}


	/**
	 * Generates a unique string of alpha characters.
	 * @param length - integer  The length of the created string.
	 * @return Returns a alpha character string of specified length.  
	 */
	public String generateUniqueAlphaString(int length){
		String uniqueString = RandomStringUtils.randomAlphabetic(length);
		return uniqueString;
	}

	/**
	 * Generates random number within specified digit limit
	 * @param maxDigits - maximum number of digits to generate
	 */

	public String generateRandomNumber(int maxDigits)
	{
		final String digits = "0123456789";
		final char[] digitsArray=digits.toCharArray();
		Random r = new Random();
		StringBuilder number = new StringBuilder();
		number.append(digitsArray[r.nextInt(digitsArray.length)]);
		for (int i = 1; i < maxDigits; i++) {
			number.append(digitsArray[r.nextInt(digitsArray.length)]);
		}
		return number.toString();
	}
	
	/**
	 * Utility method to check if the specified file exists
	 * @param fileName
	 * @return returns true if file is found else false
	 */
	public boolean checkIfFileExists(String fileName) {
		boolean found = false;
		try {
			File file = new File(fileName);
			if(file.exists() && file.isFile()) {
				found = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return found;
	}

	/**
	 * Get's the property value of the supplied property
	 * @param propertyName
	 * @param fileName
	 * @return returns the property name
	 */
	public String getProperty(String fileName, String propertyName) {
		Properties property = new Properties();
		InputStream input = null;
		String propertyValue = "";
		try {
			input = new FileInputStream(fileName);
			property.load(input);
			propertyValue = property.getProperty(propertyName);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return propertyValue;
	}

	/**
	 * Sets the property that is supplied
	 * @param fileName Path and name for property file
	 * @param property The name of the property
	 * @param value	The default value for the property if the property is not referenced in the file.
	 */
	@SuppressWarnings("unused")
	private void setProperty(String fileName, String property, String value) {
		Properties prop = new Properties();
		FileOutputStream fileOut = null;
		FileInputStream fileIn = null;
		try {
			File file = new File(fileName);
			file.getParentFile().mkdirs();
			if(checkIfFileExists(fileName)) {
				fileIn = new FileInputStream(file);
				prop.load(fileIn);
			}
			prop.setProperty(property, value);
			fileOut = new FileOutputStream(fileName);
			prop.store(fileOut, propertyComments);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Generates random number within specified digit limit
	 * @param min
	 * @param max
	 * @return
	 */
	public String generateRandomNumber(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return String.valueOf(randomNum);
	}
	
	
	/**
	 * Get user directory based on the operating system
	 * @return user directory
	 */
	public static String getOSDirectory() {
		String osName = System.getProperty("os.name");
		if(!osName.toLowerCase().contains("windows")) {
			return System.getProperty("user.home");
		}
		else {
			return "";
		}
	}

}
