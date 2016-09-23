package report;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.testng.IResultMap;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.Utils;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.XMLReporterConfig;
import org.testng.reporters.XMLStringBuffer;
import org.testng.util.Strings;

import core.Utilities;
import com.google.common.io.Files;

import constants.Constants;


/**
 * Core reporter class for generating the test result. This class need not be a listener.
 * Reason for not being a listener is because CustomReport class has the invocation methods
 * that overrides default testng methods.
 * DO NOT MAKE THIS CLASS A LISTENER
 * @author Praveen-Bhasker
 *
 */
public class XMLWriter {
 
	private XMLReporterConfig config;
	static final String timeStampFormat = "yyyy-MM-dd HH:mm:ss";
	Map<String, List<String>> reportDataMap = new HashMap<String, List<String>>();
	StringBuilder htmlReport = new StringBuilder();
	String enableGrid = System.getProperty("enableGrid","false");

	Utilities utils = new Utilities(); 

	Set<String> uniqueClasses = new HashSet<String>();
	int classCounter = 0;
	int tearDownCounter = 0;

	public XMLWriter(XMLReporterConfig config) {
		this.config = config;
		//super(config);
	}

	public void writeSuiteResult(XMLStringBuffer xmlBuffer, ISuiteResult suiteResult) {
		if (XMLReporterConfig.FF_LEVEL_SUITE_RESULT != config.getFileFragmentationLevel()) {
			writeAllToBuffer(xmlBuffer, suiteResult);
		} else {
			String parentDir =
					config.getOutputDirectory() + File.separatorChar + suiteResult.getTestContext().getSuite().getName();
			File file = referenceSuiteResult(xmlBuffer, parentDir, suiteResult);
			XMLStringBuffer suiteXmlBuffer = new XMLStringBuffer();
			writeAllToBuffer(suiteXmlBuffer, suiteResult);
			Utils.writeUtf8File(file.getAbsoluteFile().getParent(), file.getName(), suiteXmlBuffer.toXML());
		}
	}

	private void writeAllToBuffer(XMLStringBuffer xmlBuffer, ISuiteResult suiteResult) {
		xmlBuffer.push(XMLReporterConfig.TAG_TEST, getSuiteResultAttributes(suiteResult));
		Set<ITestResult> testResults = Sets.newHashSet();
		ITestContext testContext = suiteResult.getTestContext();
		addAllTestResults(testResults, testContext.getPassedTests());
		addAllTestResults(testResults, testContext.getFailedTests());
		addAllTestResults(testResults, testContext.getSkippedTests());
		addAllTestResults(testResults, testContext.getPassedConfigurations());
		addAllTestResults(testResults, testContext.getSkippedConfigurations());
		addAllTestResults(testResults, testContext.getFailedConfigurations());
		addAllTestResults(testResults, testContext.getFailedButWithinSuccessPercentageTests());
		addTestResults(xmlBuffer, testResults);
		xmlBuffer.pop();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addAllTestResults(Set<ITestResult> testResults, IResultMap resultMap) {
		if (resultMap != null) {
			List<ITestResult> allResults = new ArrayList<ITestResult>();
			allResults.addAll(resultMap.getAllResults());

			Collections.sort(new ArrayList(allResults), new Comparator<ITestResult>() {
				public int compare(ITestResult o1, ITestResult o2) {
					return (int) (o1.getStartMillis() - o2.getStartMillis());
				}
			});

			testResults.addAll(allResults);
		}
	}

	private File referenceSuiteResult(XMLStringBuffer xmlBuffer, String parentDir, ISuiteResult suiteResult) {
		Properties attrs = new Properties();
		String suiteResultName = suiteResult.getTestContext().getName() + ".xml";
		attrs.setProperty(XMLReporterConfig.ATTR_URL, suiteResultName);
		xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_TEST, attrs);
		return new File(parentDir + File.separatorChar + suiteResultName);
	}

	private Properties getSuiteResultAttributes(ISuiteResult suiteResult) {
		Properties attributes = new Properties();
		ITestContext tc = suiteResult.getTestContext();
		attributes.setProperty(XMLReporterConfig.ATTR_NAME, tc.getName());
		XMLReporter.addDurationAttributes(config, attributes, tc.getStartDate(), tc.getEndDate());
		return attributes;
	}

	private void addTestResults(XMLStringBuffer xmlBuffer, Set<ITestResult> testResults) {
		Map<String, List<ITestResult>> testsGroupedByClass = buildTestClassGroups(testResults);
		for (Map.Entry<String, List<ITestResult>> result : testsGroupedByClass.entrySet()) {
			uniqueClasses.add(result.getKey());
		}
		for (Map.Entry<String, List<ITestResult>> result : testsGroupedByClass.entrySet()) {
			Properties attributes = new Properties();
			String className = result.getKey();

			if (config.isSplitClassAndPackageNames()) {
				int dot = className.lastIndexOf('.');
				attributes.setProperty(XMLReporterConfig.ATTR_NAME,
						dot > -1 ? className.substring(dot + 1, className.length()) : className);
				attributes.setProperty(XMLReporterConfig.ATTR_PACKAGE, dot > -1 ? className.substring(0, dot) : "[default]");
			} else {
				attributes.setProperty(XMLReporterConfig.ATTR_NAME, className);
			}

			xmlBuffer.push(XMLReporterConfig.TAG_CLASS, attributes);
			List<ITestResult> sortedResults = result.getValue();
			Collections.sort( sortedResults );
			for (ITestResult testResult : sortedResults) {
				addTestResult(xmlBuffer, testResult);
			}
			xmlBuffer.pop();
		}
	}

	private Map<String, List<ITestResult>> buildTestClassGroups(Set<ITestResult> testResults) {
		Map<String, List<ITestResult>> map = Maps.newHashMap();
		for (ITestResult result : testResults) {
			String className = result.getTestClass().getName();
			List<ITestResult> list = map.get(className);
			if (list == null) {
				list = Lists.newArrayList();
				map.put(className, list);
			}
			list.add(result);
		}
		return map;
	}

	private void addTestResult(XMLStringBuffer xmlBuffer, ITestResult testResult) {
		Properties attribs = getTestResultAttributes(testResult);
		attribs.setProperty(XMLReporterConfig.ATTR_STATUS, getStatusString(testResult.getStatus()));
		attribs.setProperty("browser",System.getProperty("browserName")!=null ? System.getProperty("browserName") : "NA");
		xmlBuffer.push(XMLReporterConfig.TAG_TEST_METHOD, attribs);
		addTestMethodParams(xmlBuffer, testResult);
		addTestResultException(xmlBuffer, testResult);
		addTestResultOutput(xmlBuffer, testResult);
		if (config.isGenerateTestResultAttributes()) {
			addTestResultAttributes(xmlBuffer, testResult);
		}
		xmlBuffer.pop();
	}

	private String getStatusString(int testResultStatus) {
		switch (testResultStatus) {
		case ITestResult.SUCCESS:
			return "PASS";
		case ITestResult.FAILURE:
			return "FAIL";
		case ITestResult.SKIP:
			return "SKIP";
		case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
			return "SUCCESS_PERCENTAGE_FAILURE";
		}
		return null;
	}

	private Properties getTestResultAttributes(ITestResult testResult) {
		Properties attributes = new Properties();
		if (!testResult.getMethod().isTest()) {
			attributes.setProperty(XMLReporterConfig.ATTR_IS_CONFIG, "true");
		}
		attributes.setProperty(XMLReporterConfig.ATTR_NAME, testResult.getMethod().getMethodName());
		//String methodName = testResult.getMethod().getMethodName();
		String testInstanceName = testResult.getTestName();
		if (null != testInstanceName) {
			attributes.setProperty(XMLReporterConfig.ATTR_TEST_INSTANCE_NAME, testInstanceName);
		}
		String description = testResult.getMethod().getDescription();
		if (!Utils.isStringEmpty(description)) {
			attributes.setProperty(XMLReporterConfig.ATTR_DESC, description);
		}

		attributes.setProperty(XMLReporterConfig.ATTR_METHOD_SIG, removeClassName(testResult.getMethod().toString()));

		SimpleDateFormat format = new SimpleDateFormat(timeStampFormat);
		String startTime = format.format(testResult.getStartMillis());
		String endTime = format.format(testResult.getEndMillis());
		attributes.setProperty(XMLReporterConfig.ATTR_STARTED_AT, startTime);
		attributes.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, endTime);
		long duration = testResult.getEndMillis() - testResult.getStartMillis();
		String strDuration = Long.toString(duration);
		attributes.setProperty(XMLReporterConfig.ATTR_DURATION_MS, strDuration);
		
		if (config.isGenerateGroupsAttribute()) {
			String groupNamesStr = Utils.arrayToString(testResult.getMethod().getGroups());
			if (!Utils.isStringEmpty(groupNamesStr)) {
				attributes.setProperty(XMLReporterConfig.ATTR_GROUPS, groupNamesStr);
			}
		}

		if (config.isGenerateDependsOnMethods()) {
			String dependsOnStr = Utils.arrayToString(testResult.getMethod().getMethodsDependedUpon());
			if (!Utils.isStringEmpty(dependsOnStr)) {
				attributes.setProperty(XMLReporterConfig.ATTR_DEPENDS_ON_METHODS, dependsOnStr);
			}
		}

		if (config.isGenerateDependsOnGroups()) {
			String dependsOnStr = Utils.arrayToString(testResult.getMethod().getGroupsDependedUpon());
			if (!Utils.isStringEmpty(dependsOnStr)) {
				attributes.setProperty(XMLReporterConfig.ATTR_DEPENDS_ON_GROUPS, dependsOnStr);
			}
		}

		ConstructorOrMethod cm = testResult.getMethod().getConstructorOrMethod();
		Test testAnnotation;
		if (cm.getMethod() != null) {
			testAnnotation = cm.getMethod().getAnnotation(Test.class);
			if (testAnnotation != null) {
				String dataProvider = testAnnotation.dataProvider();
				if (!Strings.isNullOrEmpty(dataProvider)) {
					attributes.setProperty(XMLReporterConfig.ATTR_DATA_PROVIDER, dataProvider);
					StringBuilder customMethodName = new StringBuilder();
					Object[] parameters = testResult.getParameters();
					if ((parameters != null) && (parameters.length > 0)) {
						for (int i = 0; i < parameters.length; i++) {
							customMethodName.append("_").append(parameters[i]);
						}
					}
					String processedMethodName = customMethodName.toString().replaceAll(" ", "_");
					attributes.setProperty("customMethodName", processedMethodName);
				}
			}
		}

		return attributes;
	}

	private String removeClassName(String methodSignature) {
		int firstParanthesisPos = methodSignature.indexOf("(");
		int dotAferClassPos = methodSignature.substring(0, firstParanthesisPos).lastIndexOf(".");
		return methodSignature.substring(dotAferClassPos + 1, methodSignature.length());
	}

	public void addTestMethodParams(XMLStringBuffer xmlBuffer, ITestResult testResult) {
		Object[] parameters = testResult.getParameters();
		if ((parameters != null) && (parameters.length > 0)) {
			xmlBuffer.push(XMLReporterConfig.TAG_PARAMS);
			for (int i = 0; i < parameters.length; i++) {
				addParameter(xmlBuffer, parameters[i], i);
			}
			xmlBuffer.pop();
		}
	}

	private void addParameter(XMLStringBuffer xmlBuffer, Object parameter, int i) {
		Properties attrs = new Properties();
		attrs.setProperty(XMLReporterConfig.ATTR_INDEX, String.valueOf(i));
		xmlBuffer.push(XMLReporterConfig.TAG_PARAM, attrs);
		if (parameter == null) {
			Properties valueAttrs = new Properties();
			valueAttrs.setProperty(XMLReporterConfig.ATTR_IS_NULL, "true");
			xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_PARAM_VALUE, valueAttrs);
		} else {
			xmlBuffer.push(XMLReporterConfig.TAG_PARAM_VALUE);
			xmlBuffer.addCDATA(parameter.toString());
			xmlBuffer.pop();
		}
		xmlBuffer.pop();
	}

	private void addTestResultException(XMLStringBuffer xmlBuffer, ITestResult testResult) {
		Throwable exception = testResult.getThrowable();
		if (exception != null) {
			Properties exceptionAttrs = new Properties();
			exceptionAttrs.setProperty(XMLReporterConfig.ATTR_CLASS, exception.getClass().getName());
			xmlBuffer.push(XMLReporterConfig.TAG_EXCEPTION, exceptionAttrs);

			if (!Utils.isStringEmpty(exception.getMessage())) {
				xmlBuffer.push(XMLReporterConfig.TAG_MESSAGE);
				xmlBuffer.addCDATA(exception.getMessage());
				xmlBuffer.pop();
			}

			String[] stackTraces = Utils.stackTrace(exception, false);
			if ((config.getStackTraceOutputMethod() & XMLReporterConfig.STACKTRACE_SHORT) == XMLReporterConfig
					.STACKTRACE_SHORT) {
				xmlBuffer.push(XMLReporterConfig.TAG_SHORT_STACKTRACE);
				xmlBuffer.addCDATA(stackTraces[0]);
				xmlBuffer.pop();
			}
			if ((config.getStackTraceOutputMethod() & XMLReporterConfig.STACKTRACE_FULL) == XMLReporterConfig
					.STACKTRACE_FULL) {
				xmlBuffer.push(XMLReporterConfig.TAG_FULL_STACKTRACE);
				xmlBuffer.addCDATA(stackTraces[1]);
				xmlBuffer.pop();
			}

			xmlBuffer.pop();
		}
	}

	private void addTestResultOutput(XMLStringBuffer xmlBuffer, ITestResult testResult) {
		xmlBuffer.push(XMLReporterConfig.TAG_REPORTER_OUTPUT);
		List<String> output = Reporter.getOutput(testResult);
		for (String line : output) {
			if (line != null) {
				xmlBuffer.push(XMLReporterConfig.TAG_LINE);
				xmlBuffer.addCDATA(line);
				xmlBuffer.pop();
			}
		}
		gatherLocalReportData(testResult);
		xmlBuffer.pop();
	}

	private void addTestResultAttributes(XMLStringBuffer xmlBuffer, ITestResult testResult) {
		if (testResult.getAttributeNames() != null && testResult.getAttributeNames().size() > 0) {
			xmlBuffer.push(XMLReporterConfig.TAG_ATTRIBUTES);
			for (String attrName: testResult.getAttributeNames()) {
				if (attrName == null) {
					continue;
				}
				Object attrValue = testResult.getAttribute(attrName);

				Properties attributeAttrs = new Properties();
				attributeAttrs.setProperty(XMLReporterConfig.ATTR_NAME, attrName);
				if (attrValue == null) {
					attributeAttrs.setProperty(XMLReporterConfig.ATTR_IS_NULL, "true");
					xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_ATTRIBUTE, attributeAttrs);
				} else {
					xmlBuffer.push(XMLReporterConfig.TAG_ATTRIBUTE, attributeAttrs);
					xmlBuffer.addCDATA(attrValue.toString());
					xmlBuffer.pop();
				}
			}
			xmlBuffer.pop();
		}
	}


	private void gatherLocalReportData(ITestResult testResult) {
		String className = testResult.getTestClass().getName();
		String currentMethod = testResult.getMethod().getMethodName();
		List<String> reportArray = new ArrayList<String>();
		String repOutput = null;
		if (!enableGrid.equalsIgnoreCase("true")) {
			if(!testResult.getMethod().isBeforeSuiteConfiguration() && !testResult.getMethod().isBeforeTestConfiguration()) {
				List<String> output = Reporter.getOutput(testResult);
				for (String line : output) {
					reportArray.add(line);
					repOutput = line;
				}
				if(!reportDataMap.containsKey(className)) {
					reportDataMap.put(className, reportArray);
				}
				else {
					reportDataMap.get(className).add(repOutput);
				}
				if(!currentMethod.equalsIgnoreCase("tearDown")) {
					classCounter++;
				}
				if(currentMethod.equals("tearDown")) {
					tearDownCounter++;
				}
			}
			if(uniqueClasses.size() == classCounter) {
				if(classCounter == tearDownCounter) {
					generateHtmlReport();
				}
			}
			/*for (ITestNGMethod method : testMethods) {
			String runMethod = method.getMethodName();
			if(runMethod.equalsIgnoreCase(currentMethod)) {
				generateHtmlReport();
			}
		}*/

		}
	}

	private void generateHtmlReport() {
		String status;
		String testStep = "";
		String signature;
		try {
			htmlReport.append("<html>");
			htmlReport.append("<head>");
			htmlReport.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"C:\\CTM_Automation\\localreport\\table.css\" />");
			htmlReport.append("</head>");
			for (Map.Entry<String, List<String>> entry : reportDataMap.entrySet()) {
				htmlReport.append("<center><h2>").append(entry.getKey()).append("</h2></center>");
				htmlReport.append("<div class=\"rounded\">");
				htmlReport.append("<table>");
				htmlReport.append("<thead>");
				htmlReport.append("<tr style=\"background-color: #9ECAE1;\">");
				htmlReport.append("<th>Test Step</th>");
				htmlReport.append("<th>Status</th>");
				htmlReport.append("<th>Signature</th>");
				htmlReport.append("</tr>");
				htmlReport.append("</thead>");

				for (int i = 0; i < entry.getValue().size(); i++) {
					if(entry.getValue().get(i) != null) { 
						status = entry.getValue().get(i).split(": ")[0];
						signature = entry.getValue().get(i).split("ID_")[1]+".jpg";
						htmlReport.append("<tbody>");
						htmlReport.append("<tr>");

						if(status.contains("PASS")) {
							testStep = entry.getValue().get(i).split("PASS: ")[1].split("ID_")[0];	
							htmlReport.append("<td style=\"word-wrap: break-word; width: 1000px;\" align=\"left\">")
							.append(testStep).append("</td>");
							htmlReport.append("<td style=\"word-wrap: break-word; width: 500px;\" align=\"center\">")
							.append("<FONT COLOR=\"#128d12\">").append(status).append("</FONT>").append("</td>");
							htmlReport.append("<td style=\"word-wrap: break-word; width: 700px;\" align=\"center\">")
							.append("<a href="+Constants.screenshotFilePath
									+"\\"+signature+">ScreenShot</a>").append("</td>");
						}
						else if (status.contains("FAIL")) {
							testStep = entry.getValue().get(i).split("FAIL: ")[1].split("ID_")[0];	
							htmlReport.append("<td style=\"word-wrap: break-word; width: 1000px;\" align=\"left\">")
							.append(testStep).append("</td>");
							htmlReport.append("<td style=\"word-wrap: break-word; width: 500px;\" align=\"center\">")
							.append("<FONT COLOR=\"#ff0000\">").append(status).append("</FONT>").append("</td>");
							htmlReport.append("<td style=\"word-wrap: break-word; width: 700px;\" align=\"center\">")
							.append("<a href="+Constants.screenshotFilePath
									+"\\"+signature+">ScreenShot</a>").append("</td>");
						}
						else if (status.contains("Test")) {
							testStep = entry.getValue().get(i).split(": ",2)[1].split("ID_")[0];	
							htmlReport.append("<td style=\"word-wrap: break-word; width: 1000px;\" align=\"left\">")
							.append(testStep).append("</td>");
							htmlReport.append("<td style=\"word-wrap: break-word; width: 500px;\" align=\"center\">")
							.append("<FONT COLOR=\"#0011ff\">").append(status).append("</FONT>").append("</td>");
							htmlReport.append("<td style=\"word-wrap: break-word; width: 700px;\" align=\"center\">")
							.append("NA").append("</td>");
						}
						else {
							testStep = entry.getValue().get(i).split("ID_")[0];
							htmlReport.append("<td style=\"word-wrap: break-word; width: 1000px;\" align=\"left\">")
							.append(testStep).append("</td>");
							htmlReport.append("<td style=\"word-wrap: break-word; width: 500px;\" align=\"center\">")
							.append("<FONT COLOR=\"#0011ff\">").append("Info").append("</FONT>").append("</td>");
							htmlReport.append("<td style=\"word-wrap: break-word; width: 700px;\" align=\"center\">")
							.append("<a href="+Constants.screenshotFilePath
									+"\\"+signature+">ScreenShot</a>").append("</td>");
						}
						htmlReport.append("</tr>");
						htmlReport.append("</tbody>");
					}
				}
				htmlReport.append("</table>");
				htmlReport.append("</div>");
				htmlReport.append("<b>").append("<b>");

			}
			htmlReport.append("</html>");
			createDirectories();
			archiveLocalReport();
			PrintWriter out = new PrintWriter(Constants.reportDirectory+Constants.reportFileName);
			out.println(htmlReport.toString());
			out.close();
			reportDataMap.clear();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {

		}
	}

	private void createDirectories() {
		try {
			File dir = new File(Constants.reportDirectory);
			File fromDir = new File(Constants.cssFile);
			File toDir = new File(Constants.reportDirectory+"table.css");
			if (!dir.exists()) {
				dir.mkdir();
			}
			Files.copy(fromDir, toDir);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void archiveLocalReport() {

		try {
			if(utils.checkIfFileExists(Constants.reportDirectory+Constants.reportFileName)) {
				File dir = new File(Constants.archieveDirectory);
				File fromDir = new File(Constants.reportDirectory+Constants.reportFileName);
				DateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss");
				Date date = new Date();
				File toDir = new File(Constants.archieveDirectory+"CTM_LocalReport"+"_"+dateFormat.format(date)+".html");
				if (!dir.exists()) {
					dir.mkdir();
				}
				Files.copy(fromDir, toDir);
			}
		} catch (Exception e) {
			e.printStackTrace();		
		}
	}



}
