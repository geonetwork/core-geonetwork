package org.fao.geonet.test;

import junit.framework.Test;
import junit.framework.TestResult;

import java.util.Enumeration;

/**
 * TestSuite util that runs suite created in subclass.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
abstract public class SuiteRunner {

	public String run() {
		//junit.textui.TestRunner.run(createSuite());
		TestResult testResult = new TestResult();
		createSuite().run(testResult);
		String result = resultToString(testResult);
		p(result);
		return result;
	}

	/**
	 * Subclass must create the test suite.
	 */
	abstract protected Test createSuite();

	protected void p(String s) {
		System.out.println(s);
	}

	/**
	 * Rand.: convert test result to String.
	 */
	private String resultToString(TestResult aTestResult) {
		StringBuffer sb = new StringBuffer();
		sb.append("=== AllTests JUnit results ===\n");
		sb.append("success4All=" + aTestResult.wasSuccessful() + "\n");
		sb.append("numberOfTests=" + aTestResult.runCount() + "\n");
		sb.append("numberOfFailures=" + aTestResult.failureCount() + "\n");
		sb.append("numberOfErrors=" + aTestResult.errorCount() + "\n");

		if (aTestResult.failureCount() > 0) {
			int failureNr = 1;
			sb.append("failure details\n");
			for (Enumeration e = aTestResult.failures(); e.hasMoreElements();) {
				sb.append("#" + (failureNr++) + ": " + e.nextElement() + "\n");
			}
		}

		if (aTestResult.errorCount() > 0) {
			int errorNr = 1;
			sb.append("error details\n");
			for (Enumeration e = aTestResult.errors(); e.hasMoreElements();) {
				sb.append("#" + (errorNr++) + ": " + e.nextElement() + "\n");
			}
		}

		return sb.toString();
	}
}
