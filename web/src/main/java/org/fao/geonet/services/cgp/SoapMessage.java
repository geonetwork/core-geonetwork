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

package org.fao.geonet.services.cgp;

import org.jdom.Element;
import org.jdom.Namespace;

//=============================================================================

/**
 * Wrapper for SOAP Envelope.
 */
public class SoapMessage
{
	public static final Namespace NAMESPACE_ENV = Namespace.getNamespace("env", "http://schemas.xmlsoap.org/soap/envelope/");

	public SoapMessage(Element envelope)
	{
		this.envelope = envelope;
	}

	public SoapMessage()
	{
		this.envelope = new Element("Envelope", NAMESPACE_ENV);
		this.envelope.addContent(new Element("Body", NAMESPACE_ENV));
	}

	public Element getEnvelope()
	{
		return this.envelope;
	}

	public Element getBody()
	{
		return this.envelope.getChild("Body", NAMESPACE_ENV);
	}

	public Element getHeader()
	{
		return this.envelope.getChild("Header", NAMESPACE_ENV);
	}

	public Element setFault(String code, String string)
	{
		/*
		<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>
  <env:Body>
    <env:Fault>
      <faultcode>xml:lang</faultcode>
      <faultstring>details bla</faultstring>
    </env:Fault>
  </env:Body>
</env:Envelope>
		 */
		Element faultElm = new Element("Fault", NAMESPACE_ENV);
		Element faultCodeElm = new Element("faultcode", NAMESPACE_ENV);
		Element faultStringElm = new Element("faultstring", NAMESPACE_ENV);

		faultCodeElm.setText(code);
		faultStringElm.setText(string);

		faultElm.addContent(faultCodeElm);
		faultElm.addContent(faultStringElm);

		return getBody().addContent(faultElm);
	}


	public Element setBodyContent(Element content)
	{
		return getBody().addContent(content);
	}

	public Element setHeader(Element header)
	{
		return this.envelope.addContent(0, header);
	}

	private Element envelope;
}

//=============================================================================

