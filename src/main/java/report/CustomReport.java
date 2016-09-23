package report;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.internal.Utils;
import org.testng.reporters.XMLReporterConfig;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlSuite;

/**
 * This is a listener class. Runs after "After Invocation" is done. 
 * This class is responsible to generate suite level results.
 
 *
 */
public class CustomReport extends TestListenerAdapter implements IReporter {

	//public static final String FILE_NAME = Constants.reportFileName;
	private String FILE_NAME = generateFileName();
	private final XMLReporterConfig config = new XMLReporterConfig();
	private XMLStringBuffer rootBuffer;
	static final String timeStampFormat = "yyyy-MM-dd HH:mm:ss";
	static final String outputDir = "c:\\CTM_Automation\\CTM_Reports\\";
	protected static String application = System.getProperty("application", "NA");
	protected static String environment = System.getProperty("environment", "NA");
	protected static String browserName = System.getProperty("browserName", "NA");
//"/home/agilematrix/Documents/agilematrixzone"
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		FailureReporter fR = new FailureReporter(); 
		try {
			System.out.println("\n\n\n");
			Art art = new Art();
			System.out.println(art.generateText()+"\n\n\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (Utils.isStringEmpty(config.getOutputDirectory())) {
			config.setOutputDirectory(outputDirectory);
		}
		int passed = 0;
		int failed = 0;
		int skipped = 0;
		for (ISuite s : suites) {
			for (ISuiteResult sr : s.getResults().values()) {
				ITestContext testContext = sr.getTestContext();
				passed += testContext.getPassedTests().size();
				failed += testContext.getFailedTests().size();
				skipped += testContext.getSkippedTests().size();
			}
		}

		rootBuffer = new XMLStringBuffer();
		Properties p = new Properties();
		p.put("passed", passed);
		p.put("failed", failed);
		p.put("skipped", skipped);
		p.put("total", passed + failed + skipped);
		
		//packageName = System.getProperty("packageName","NA").equalsIgnoreCase("${packageName}") ? System.getProperty("testName","NA") : System.getProperty("packageName","NA");
		p.put("package", getPackageName());
		p.put("browsername", browserName);
		p.put("environment", environment);
		p.put("application", application);
		try {
			p.put("hostname", InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		rootBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS, p);
		writeReporterOutput(rootBuffer);
		for (int i = 0; i < suites.size(); i++) {
			writeSuite(suites.get(i).getXmlSuite(), suites.get(i));
		}
		rootBuffer.pop();
		//Utils.writeUtf8File(config.getOutputDirectory(),FILE_NAME,rootBuffer.toXML());
		Utils.writeUtf8File(outputDir,FILE_NAME,rootBuffer.toXML());
		//cleanSuite();
		
	    for (ISuite suite : suites) {
	    	fR.generateFailureSuite(suite.getXmlSuite(), suite, outputDirectory);
	      }
	    
		System.out.println("AutoAMZ Execution Complete");
	}
	

	public String generateFileName() {
		String fileName = "";
		try {

			DateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss");
			Date date = new Date();
			fileName = "CustomReport_"+ application + "_" + environment +"_"+getPackageName()
					+"_"+ browserName +"_"+dateFormat.format(date)+".xml";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileName;
	}
	
	private String getPackageName() {
		String packageName = "";
		packageName = System.getProperty("packageName", "NA");
		if(packageName != null && !packageName.equalsIgnoreCase("")) {
			if(packageName.equalsIgnoreCase("${packageName}")) {
				packageName = "NA";
			}
		}
		return packageName;
	}

	public void cleanSuite() {
		try {
			File dir = new File("./target/surefire-reports");
			for(File file: dir.listFiles()) {
				System.out.println("File Name : "+file.getName());
				if(!file.getName().startsWith("CustomReport")) {
					System.out.println("Deleting : "+file.getName());
					file.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeReporterOutput(XMLStringBuffer xmlBuffer) {
		xmlBuffer.push(XMLReporterConfig.TAG_REPORTER_OUTPUT);
		List<String> output = Reporter.getOutput();
		for (String line : output) {
			if (line != null) {
				xmlBuffer.push(XMLReporterConfig.TAG_LINE);
				xmlBuffer.addCDATA(line);
				xmlBuffer.pop();
			}
		}
		xmlBuffer.pop();
	}

	private void writeSuite(XmlSuite xmlSuite, ISuite suite) {
		switch (config.getFileFragmentationLevel()) {
		case XMLReporterConfig.FF_LEVEL_NONE:
			writeSuiteToBuffer(rootBuffer, suite);
			break;
		case XMLReporterConfig.FF_LEVEL_SUITE:
		case XMLReporterConfig.FF_LEVEL_SUITE_RESULT:
			File suiteFile = referenceSuite(rootBuffer, suite);
			writeSuiteToFile(suiteFile, suite);
		}
	}

	private void writeSuiteToFile(File suiteFile, ISuite suite) {
		XMLStringBuffer xmlBuffer = new XMLStringBuffer();
		writeSuiteToBuffer(xmlBuffer, suite);
		File parentDir = suiteFile.getParentFile();
		if (parentDir.exists() || suiteFile.getParentFile().mkdirs()) {
			Utils.writeFile(parentDir.getAbsolutePath(), FILE_NAME, xmlBuffer.toXML());
		}
	}

	private File referenceSuite(XMLStringBuffer xmlBuffer, ISuite suite) {
		String relativePath = suite.getName() + File.separatorChar + FILE_NAME;
		File suiteFile = new File(config.getOutputDirectory(), relativePath);
		Properties attrs = new Properties();
		attrs.setProperty(XMLReporterConfig.ATTR_URL, relativePath);
		xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_SUITE, attrs);
		return suiteFile;
	}

	private void writeSuiteToBuffer(XMLStringBuffer xmlBuffer, ISuite suite) {
		xmlBuffer.push(XMLReporterConfig.TAG_SUITE, getSuiteAttributes(suite));
		writeSuiteGroups(xmlBuffer, suite);

		Map<String, ISuiteResult> results = suite.getResults();
		/* Change here to get any parameter from Jenkins or Maven.
		 * Pls do not add a new constructor. - Praveen*/
		XMLWriter suiteResultWriter = new XMLWriter(config);
		for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
			suiteResultWriter.writeSuiteResult(xmlBuffer, result.getValue());
		}
		xmlBuffer.pop();
	}

	private void writeSuiteGroups(XMLStringBuffer xmlBuffer, ISuite suite) {
		xmlBuffer.push(XMLReporterConfig.TAG_GROUPS);
		Map<String, Collection<ITestNGMethod>> methodsByGroups = suite.getMethodsByGroups();
		for (Map.Entry<String, Collection<ITestNGMethod>> entry : methodsByGroups.entrySet()) {
			Properties groupAttrs = new Properties();
			groupAttrs.setProperty(XMLReporterConfig.ATTR_NAME, entry.getKey());
			xmlBuffer.push(XMLReporterConfig.TAG_GROUP, groupAttrs);
			Set<ITestNGMethod> groupMethods = getUniqueMethodSet(entry.getValue());
			for (ITestNGMethod groupMethod : groupMethods) {
				Properties methodAttrs = new Properties();
				methodAttrs.setProperty(XMLReporterConfig.ATTR_NAME, groupMethod.getMethodName());
				methodAttrs.setProperty(XMLReporterConfig.ATTR_METHOD_SIG, groupMethod.toString());
				methodAttrs.setProperty(XMLReporterConfig.ATTR_CLASS, groupMethod.getRealClass().getName());
				xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_METHOD, methodAttrs);
			}
			xmlBuffer.pop();
		}
		xmlBuffer.pop();
	}

	private Properties getSuiteAttributes(ISuite suite) {
		Properties props = new Properties();
		props.setProperty(XMLReporterConfig.ATTR_NAME, suite.getName());
		Map<String, ISuiteResult> results = suite.getResults();
		Date minStartDate = new Date();
		Date maxEndDate = null;
		for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
			ITestContext testContext = result.getValue().getTestContext();
			Date startDate = testContext.getStartDate();
			Date endDate = testContext.getEndDate();
			if (minStartDate.after(startDate)) {
				minStartDate = startDate;
			}
			if (maxEndDate == null || maxEndDate.before(endDate)) {
				maxEndDate = endDate != null ? endDate : startDate;
			}
		}

		if (maxEndDate == null) {
			maxEndDate = minStartDate;
		}
		addDurationAttributes(config, props, minStartDate, maxEndDate);
		return props;
	}

	public static void addDurationAttributes(XMLReporterConfig config, Properties attributes,
			Date minStartDate, Date maxEndDate) {
		SimpleDateFormat format = new SimpleDateFormat(timeStampFormat);
		//TimeZone est = TimeZone.getTimeZone("EST");
		//format.setTimeZone(est);
		String startTime = format.format(minStartDate);
		String endTime = format.format(maxEndDate);
		long duration = maxEndDate.getTime() - minStartDate.getTime();

		attributes.setProperty(XMLReporterConfig.ATTR_STARTED_AT, startTime);
		attributes.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, endTime);
		attributes.setProperty(XMLReporterConfig.ATTR_DURATION_MS, Long.toString(duration));
		try {
			attributes.setProperty("hostname", InetAddress.getLocalHost().getHostName());
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private Set<ITestNGMethod> getUniqueMethodSet(Collection<ITestNGMethod> methods) {
		Set<ITestNGMethod> result = new LinkedHashSet<ITestNGMethod>();
		for (ITestNGMethod method : methods) {
			result.add(method);
		}
		return result;
	}

}
