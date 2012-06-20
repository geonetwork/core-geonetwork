package org.fao.geonet.kernel.harvest.harvester.cgp;

import jeeves.exceptions.JeevesClientEx;
import jeeves.utils.Xml;
import org.jdom.Element;

/**
 * SOAP Fault wrapper.
 * <p/>
 * Basic version for now. Full Fault Element string will be printed in log
 */
public class SOAPFaultEx extends JeevesClientEx
{
	public SOAPFaultEx(Element faultElm)
	{
		super(Xml.getString(faultElm), faultElm);
		id = "soap-fault";
	}
}
