/**
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.util;

import java.io.StringReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCExtension;

public class WMCUtil
{
	public static void addExtensionChild(WMCExtension ext, Element child)
	{
		String name = child.getName();
		XMLOutputter xo = new XMLOutputter(Format.getCompactFormat());
		String schild = xo.outputString(child);

		ext.add(name, schild);
	}

	public static Element getExtensionChild(WMCExtension ext, String name)
	{
		String schild = ext.get(name);
		if(schild == null)
			return null;

		try
		{
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new StringReader(schild));
			return doc.detachRootElement();
		}
		catch (Exception e)
		{
			System.out.println("Error extracting child '"+name+"' from WMCExtension");
			e.printStackTrace();
			return null;
		}
	}

}

