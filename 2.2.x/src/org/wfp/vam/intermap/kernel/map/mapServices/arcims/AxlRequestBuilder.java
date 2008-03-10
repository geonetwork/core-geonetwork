//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.arcims;

import java.io.File;
import jeeves.utils.Xml;
import org.jdom.Element;
import org.wfp.vam.intermap.util.XmlTransformer;

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
			return XmlTransformer.transform(data, t);
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

