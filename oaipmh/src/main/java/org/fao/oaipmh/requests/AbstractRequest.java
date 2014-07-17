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

package org.fao.oaipmh.requests;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.fao.oaipmh.exceptions.OaiPmhException;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.util.Lib;
//import org.fao.oaipmh.util.Xml;
import jeeves.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

//=============================================================================

public abstract class AbstractRequest
{
	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private File      schemaPath;
	private Transport transport = new Transport();
	
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	/**
	 * @return the schemaPath
	 */
	public File getSchemaPath() {
		return schemaPath;
	}


	/**
	 * @param schemaPath the schemaPath to set
	 */
	public void setSchemaPath(File schemaPath) {
		this.schemaPath = schemaPath;
	}
	
	/**
	 * @return the transport
	 */
	public Transport getTransport() {
		return transport;
	}


	/**
	 * @param transport the transport to set
	 */
	public void setTransport(Transport transport) {
		this.transport = transport;
	}


	public abstract String getVerb();

	public abstract AbstractResponse execute() throws IOException, OaiPmhException,
																	  JDOMException, SAXException, Exception;

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected Element sendRequest(Map<String, String> params) throws IOException, OaiPmhException,
																						  JDOMException, SAXException, Exception
	{
		transport.clearParameters();

		for (String name : params.keySet())
			transport.addParameter(name, params.get(name));

		transport.addParameter("verb", getVerb());

		Element response = transport.execute();

		if (!Lib.isRootValid(response))
			throw new Exception("Response is not in OAI-PMH format");

		//--- validate the result
		try {
			Xml.validate(response);
		} catch (Exception e) {
			System.out.println("Response didn't validate! Continuing. Here's the validation error: "+e.getMessage());
			e.printStackTrace();
		}

		//--- raises an exception if the case
		OaiPmhException.unmarshal(response);

		return response;
	}
}

//=============================================================================


