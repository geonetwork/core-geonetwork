package org.fao.geonet.test;

import jeeves.utils.Xml;
import jeeves.server.local.LocalJeeves;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLTestCase;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * JUnit/XMLUnit extension to facilitate GeoNetwork service testing.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class ProtocolTestCase extends XMLTestCase
{
	/**
	 * Holds symbolic vars used in XML test files.
	 */
	private XmlVars xmlVars = new XmlVars();


	public static final String TAG_REQUEST = "request";
	public static final String TAG_RESPONSE = "response";

	private String xmlDir = null;

	public ProtocolTestCase(String xmlDir)
	{
		this.xmlDir = xmlDir;
	}

	public ProtocolTestCase()
	{
	}

	protected void setUp()
	{
		// Setup Jeeves engine locally only once.
		if (!LocalJeeves.isRunning()) {
			TestConfig.init();
			LocalJeeves.init(TestConfig.getAppPath(), TestConfig.getConfigPath(), TestConfig.getBaseUrl());
		}

		// Directory with xml test files supposed to be the same as Junit/XMLUnit java files
		this.xmlDir = TestConfig.getTestHomePath() + "/src/" + getClass().getPackage().getName().toString().replaceAll("\\.", "/") + "/";
	}

	/**
	 * Post-test call by JUnit.
	 */
	protected void tearDown()
	{
		xmlVars.clearVariables();
	}

	/**
	 * Run single test using XML req/rsp specification in file.
	 *
	 * @param aFileName file name relative to xml scripts homedir
	 */
	public void doTest(String aFileName) throws Exception
	{
		doTest(aFileName, null);
	}

	/**
	 * Run single test using XML req/rsp specification in file.
	 *
	 * @param aFileName file name relative to xml scripts homedir
	 */
	public void doTest(String aFileName, DifferenceListener differenceListener) throws Exception
	{
		p(aFileName);
		// Load the file containing the test
		Element testElm = loadXMLFile(aFileName);

		// Get request-fragment
		Element reqElm = testElm.getChild(TAG_REQUEST);

		// Expand possible symbolic XmlVars
		setRequestVars(reqElm);

		// Do the Jeeves dispatch
		Element result = LocalJeeves.dispatch(reqElm);

		// Expected elm is first child of response-fragment
		Element expectedRspElm = (Element) testElm.getChild(TAG_RESPONSE).getChildren().get(0);
		processResponseVars(expectedRspElm, result);

		// Compare the expected XML and actual response XML using XMLUnit
		String strExpected = getString(expectedRspElm);
		String strResult = getString(result);
		Diff diff = new Diff(strExpected, strResult);

		if (differenceListener != null)
		{
			diff.overrideDifferenceListener(differenceListener);
		}
		assertXMLEqual("comparing test xml to control xml", diff, true);
	}

	/**
	 * Get a variable.
	 */
	public String getVariable(String name)
	{
		return xmlVars.getVariable(name);
	}

	/**
	 * Set a variable.
	 */
	public void setVariable(String name, String value)
	{
		xmlVars.setVariable(name, value);
	}

	/**
	 * Converts an xml element to a string
	 */
	public static String getString(Element data)
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		return outputter.outputString(data);
	}

	protected Element loadXMLFile(String aFilePath)
	{
		try
		{
			return Xml.loadFile(xmlDir + aFilePath);
		} catch (Throwable t)
		{
			err("Cannot load XML file=" + aFilePath, t);
		}
		return null;
	}

	protected void processResponseVars(Element expectedRspElm, Element actualRspElm)
	{
		// Store the variables found in the incoming message.
		// Unfortunately, I have to make a copy to be able to use Path,
		// which only works from the root of a document. I also need
		// to clean up the ${var} stuf from the expected msg because it will
		// not match otherwise.

		xmlVars.storeVariables((Element) expectedRspElm.clone(), actualRspElm);
		xmlVars.expandVariables(expectedRspElm);
		xmlVars.processVariables(expectedRspElm);
	}

	protected void setRequestVars(Element anElm)
	{
		xmlVars.expandVariables(anElm);
	}

	private static void p(String s)
	{
		System.out.println();
	}

	private static void err(String s, Throwable t)
	{
		p("ERROR: " + s + " msg=" + t.getMessage());
		t.printStackTrace();
	}
}
