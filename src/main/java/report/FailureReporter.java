package report;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.MethodHelper;
import org.testng.internal.Utils;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This reporter is responsible for creating ctm-failed.xml
 *
 *
 */

public class FailureReporter extends CustomReport {
	public static final String CTM_FAILED_XML = "ctm-failed.xml";
	public static final String CTM_OUTPUT_DIR = "./resources/";

	private XmlSuite m_xmlSuite;

	public FailureReporter() {
	}

	public FailureReporter(XmlSuite xmlSuite) {
		m_xmlSuite = xmlSuite;
	}
	
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
	    for (ISuite suite : suites) {
	    	generateFailureSuite(suite.getXmlSuite(), suite, outputDirectory);
	      }
		
	}

	protected void generateFailureSuite(XmlSuite xmlSuite, ISuite suite, String outputDir) {
		XmlSuite failedSuite = (XmlSuite) xmlSuite.clone();
		failedSuite.setName(application + "_" + environment + "_" + "Failed_Suite");
		m_xmlSuite= failedSuite;

		Map<String, XmlTest> xmlTests= Maps.newHashMap();
		for(XmlTest xmlT: xmlSuite.getTests()) {
			xmlTests.put(xmlT.getName(), xmlT);
		}
		Map<String, ISuiteResult> results = suite.getResults();
		for(Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
			ISuiteResult suiteResult = entry.getValue();
			ITestContext testContext = suiteResult.getTestContext();
			generateTestXMLReport(suite,
					xmlTests.get(testContext.getName()),
					testContext,
					testContext.getFailedTests().getAllResults(),
					testContext.getSkippedTests().getAllResults());
		}

		if(null != failedSuite.getTests() && failedSuite.getTests().size() > 0) {
			Utils.writeUtf8File(CTM_OUTPUT_DIR, CTM_FAILED_XML, failedSuite.toXml());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void generateTestXMLReport(ISuite suite, XmlTest xmlTest, ITestContext context, Collection<ITestResult> failedTests,
			Collection<ITestResult> skippedTests) {
		System.out.println("Total number of failed tests : "+failedTests.size());
		if (skippedTests.size() > 0 || failedTests.size() > 0) {
			Set<ITestNGMethod> methodsToReRun = Sets.newHashSet();
			Collection[] allTests = new Collection[] {
					failedTests, skippedTests
			};
			for (Collection<ITestResult> tests : allTests) {
				for (ITestResult failedTest : tests) {
					ITestNGMethod current = failedTest.getMethod();
					if (current.isTest()) {
						methodsToReRun.add(current);
						ITestNGMethod method = failedTest.getMethod();
						if (method.isTest()) {
							List<ITestNGMethod> methodsDependedUpon =
									MethodHelper.getMethodsDependedUpon(method, context.getAllTestMethods());
							for (ITestNGMethod m : methodsDependedUpon) {
								if (m.isTest()) {
									methodsToReRun.add(m);
								}
							}
						}
					}
				}
			}
			List<ITestNGMethod> result = Lists.newArrayList();
			for (ITestNGMethod m : context.getAllTestMethods()) {
				if (methodsToReRun.contains(m)) {
					result.add(m);
				}
			}
			methodsToReRun.clear();
			createTestReportData(context, result, xmlTest);
		}
	}

	private void createTestReportData(ITestContext context, List<ITestNGMethod> methods, XmlTest srcXmlTest) {
		XmlTest xmlTest = new XmlTest(m_xmlSuite);
		xmlTest.setName(application + "_" + environment + "_" + "Failed_Test");
		xmlTest.setIncludedGroups(srcXmlTest.getIncludedGroups());
		xmlTest.setExcludedGroups(srcXmlTest.getExcludedGroups());
		xmlTest.setParallel(srcXmlTest.getParallel());
		xmlTest.setParameters(srcXmlTest.getLocalParameters());
		xmlTest.setJUnit(srcXmlTest.isJUnit());
		List<XmlClass> xmlClasses = createClassReport(methods, srcXmlTest);
		xmlTest.setXmlClasses(xmlClasses);
	}

	
	private List<XmlClass> createClassReport(List<ITestNGMethod> methods, XmlTest srcXmlTest) {
		List<XmlClass> result = Lists.newArrayList();
		Map<Class<?>, Set<ITestNGMethod>> methodsMap= Maps.newHashMap();

		for (ITestNGMethod m : methods) {
			Object instances= m.getInstance();
			Class<?> clazz= instances == null || instances == null
					? m.getRealClass()
							: instances.getClass();
					Set<ITestNGMethod> methodList= methodsMap.get(clazz);
					if(null == methodList) {
						methodList= new HashSet<ITestNGMethod>();
						methodsMap.put(clazz, methodList);
					}
					methodList.add(m);
		}
		Map<String, String> parameters = Maps.newHashMap();
		for (XmlClass c : srcXmlTest.getClasses()) {
			parameters.putAll(c.getLocalParameters());
		}
		int index = 0;
		for(Map.Entry<Class<?>, Set<ITestNGMethod>> entry: methodsMap.entrySet()) {
			Class<?> clazz= entry.getKey();
			XmlClass xmlClass= new XmlClass(clazz.getName(), index++, false);
			result.add(xmlClass);
		}
		return result;
	}
}
