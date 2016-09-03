package core;

import org.testng.IClass;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class ListenerFactory extends TestListenerAdapter{
	@Override
	public void onTestStart(ITestResult tr) {
		log("Test Started....");
		 long id = Thread.currentThread().getId();
	        System.out.println("Before test-method. Thread id is: " + id);
	      
		
	}

	@Override
	public void onTestSuccess(ITestResult tr) {

		log("Test '" + tr.getName() + "' PASSED");
		

		// This will print the class name in which the method is present
		log(tr.getTestClass());

		// This will print the priority of the method.
		// If the priority is not defined it will print the default priority as
		// 'o'
		log("Priority of this method is " + tr.getMethod().getPriority());

		System.out.println(".....");
	}

	@Override
	public void onTestFailure(ITestResult tr) {

		log("Test '" + tr.getName() + "' FAILED");
		log("Priority of this method is " + tr.getMethod().getPriority());
	 	System.out.println("***** Error "+tr.getName()+" test has failed *****");
    	String methodName=tr.getName().toString().trim();
    	FunctionalFactory.takeScreenShot(methodName);
		
	}

	@Override
	public void onTestSkipped(ITestResult tr) {
		log("Test '" + tr.getName() + "' SKIPPED");
		System.out.println(".....");
	}

	private void log(String methodName) {
		System.out.println(methodName);
	}

	private void log(IClass testClass) {
		System.out.println(testClass);
	}
	

}