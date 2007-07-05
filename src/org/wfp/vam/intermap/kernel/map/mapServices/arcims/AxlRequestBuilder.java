/**
 * AxlRequestBuilder.java
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.kernel.map.mapServices.arcims;

import java.io.*;

import org.jdom.*;

import jeeves.utils.Xml;

public class AxlRequestBuilder
{
	private static File directory;

	/**
	 * Initializes the class with the stylesheets contained in the specified
	 * directory
	 *
	 * @param    dir                 the directiry containing the stylesheets
	 *
	 */
	public static void init(String dir) {
		directory = new File(dir);
	}

	/**
	 * Transform a data Element in the AXL request using a stylesheet
	 *
	 * @param    fileName            the file name of the stylesheet
	 * @param    data                the data Element
	 *
	 * @return   the AXL request
	 *
	 * @throws   Exception
	 *
	 */
	public static Element getRequest(Element data, String fileName)
		throws Exception
	{
		try // DEBUG
		{
			Element stylesheet = Xml.loadFile(directory + File.separator + fileName);
			Element t = (Element)stylesheet.clone();
			return Xml.transform(data, t);
		}
		catch (Exception e) { e.printStackTrace(); }
		return null; // DEBUG
	}

	/**
	 * Returns an AXL request
	 *
	 * @param    fileName the name of the request file
	 *
	 * @return   the AXL request (the file content)
	 *
	 */
	public static Element getRequest(String fileName) throws Exception {
		Element stylesheet = Xml.loadFile(directory + File.separator + fileName);
		return stylesheet;
	}
	
}

