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

package org.fao.geonet.kernel.csw.services;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.OperationNotSupportedEx;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.services.harvesting.Util;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

//=============================================================================
/**
 * "This is the pull mechanism that 'pulls' data into the catalogue. That is,
 * this operation only references the data to be inserted or updated in the
 * catalogue, and it is the job of the catalogue service to resolve the
 * reference, fetch that data, and process it into the catalogue."
 * 
 */
public class Harvest extends AbstractOperation implements CatalogService {
	// ---------------------------------------------------------------------------
	// ---
	// --- Constructor
	// ---
	// ---------------------------------------------------------------------------

	public Harvest() {
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// ---------------------------------------------------------------------------

	public String getName() {
		return "Harvest";
	}

	// ---------------------------------------------------------------------------
	public Element execute(Element request, ServiceContext context)
	throws CatalogException {

		throw new OperationNotSupportedEx(getName() + 
			"Harvest operation is not supported by this catalogue.\n");
	}
	
	
	/**
	 * If Harvest operation is required, user could rename that method to 
	 * replace the execute one.
	 * 
	 * @param request
	 * @param context
	 * @return
	 * @throws CatalogException
	 */
	public Element executeMethodToBeRenamedToTurnItOn(Element request, ServiceContext context)
			throws CatalogException {
		
//		throw new OperationNotSupportedEx(getName() + 
//		"Harvest operation is not supported by this catalogue.\n" +
//		"\n" +
//		"Main problems are:\n" +
//		" * make it protected (define a specific service for Harvest and Transaction operations)\n" +
//		" * Harvest operation does not allow to restrict harvested records by criterias (CQL or Filter)\n" +
//		" * Harvest operation does not allow to define privileges for harvested records\n" +
//		" * Harvest operation does not allow to define logos\n" +
//		"\n" +
//		"TODO :\n" +
//		" * Add a notification system to Harvester to deal with ResponseHandler." +
//		" * Run harvester synchronously"
//		);
		
		checkService(request);
		checkVersion(request);

		//-- Define an id for the operation to be used
		// for harvester node name identification and
		// response handler info.
		operationId = "CSW.HarvestOperation:" + UUID.randomUUID().toString();
		
		// handle request to params node
		Element node = null;
		try {
			node = getHarvestNode(request, context);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO : swallow the exception with catalogException
		}

		Element response = null;

		String strHandler = request.getChildText("ResponseHandler",
				Csw.NAMESPACE_CSW);

		if (strHandler == null || 0 == strHandler.length()) {
			// FIXME : response = processSynchronous(request, node, context);
			throw new OperationNotSupportedEx ("HarvestOperation using synchronous action is not supported.");
		} else {
			// Immediate acknowledgement answer.
			response = processAsynchronous(request, node, context);
		}

		return response;
	}

	// ---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params) {
		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		return request;
	}

	// ---------------------------------------------------------------------------

	public Element retrieveValues(String parameterName) throws CatalogException {
		// TODO Auto-generated method stub
		return null;
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Check source URL parameter. Create a CSW Harvester configuration and save
	 * it do database.
	 * 
	 * @param request
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private Element getHarvestNode(Element request, ServiceContext context)
			throws Exception {
		// -- Check source URL
		String strSource = request.getChildText("Source", Csw.NAMESPACE_CSW);
		if (strSource == null)
			throw new MissingParameterValueEx("Source");
		
		try {
			URL cswURL = new URL(strSource);
		} catch (MalformedURLException e) {
			throw new InvalidParameterValueEx("Source", "Invalid source URL:"
					+ strSource + " - " + e.getMessage());
		}

		// -- Create on configuration for the harvester.
		Element node = new Element("node");
		// Only CSW harvester could be configured here.
		node = node.setAttribute("type", "csw");

		Element site = new Element("site");
		String strName = operationId;
		Element name = new Element("name").addContent(strName);
		site.addContent(name);

		Element capabilitiesUrl = new Element("capabilitiesUrl");
		capabilitiesUrl.addContent(strSource);
		site.addContent(capabilitiesUrl);

		Element eleIcon = new Element("icon").addContent("csw.gif");
		site.addContent(eleIcon);

		// -- TODO : this parameter is not used by
		// GeoNetwork because harvesting CSW will use any kind of
		// response type.
		// String resourceType = request.getChildText("ResourceType",
		// Csw.NAMESPACE_CSW);

		// -- if CSW node is protected by HTTP/BA
		Element account = new Element("account");
		Element username = new Element("username");
		Element password = new Element("password");
		Element use = new Element("use").addContent("false");

		account.addContent(use);
		account.addContent(username);
		account.addContent(password);
		site.addContent(account);

		node.addContent(site);

		// -- Harvester interval
		Element options = new Element("options");
		Element eleEvery = new Element("every");
		Element eleOneRun = new Element("oneRunOnly");
		String strEvery = request.getChildText("HarvestInterval",
				Csw.NAMESPACE_CSW);
		if (strEvery == null || strEvery.equals("P0Y0M0DT0H0M0S")) {
			eleEvery.addContent("90");
			eleOneRun.addContent("true");
		} else {
			int nMinutes = 0;
			nMinutes += Integer.parseInt(strEvery.substring(strEvery
					.indexOf('P') + 1, strEvery.indexOf('Y'))) * 365 * 24 * 60;
			nMinutes += Integer.parseInt(strEvery.substring(strEvery
					.indexOf('Y') + 1, strEvery.indexOf('M'))) * 30 * 24 * 60;
			nMinutes += Integer.parseInt(strEvery.substring(strEvery
					.indexOf('M') + 1, strEvery.indexOf('D'))) * 24 * 60;
			nMinutes += Integer.parseInt(strEvery.substring(strEvery
					.indexOf('T') + 1, strEvery.indexOf('H'))) * 60;
			nMinutes += Integer.parseInt(strEvery.substring(strEvery
					.indexOf('H') + 1, strEvery.indexOf('M', strEvery
					.indexOf('H'))));
			nMinutes += Integer.parseInt(strEvery.substring(strEvery.indexOf(
					'M', strEvery.indexOf('H')) + 1, strEvery.indexOf('S'))) / 60;

			eleEvery.addContent(Integer.toString(nMinutes));
			eleOneRun.addContent("false");
		}
		options.addContent(eleEvery);
		options.addContent(eleOneRun);
		node.addContent(options);

		Element searches = new Element("searches"); // You cannot define a
		// search for this
		// harvester!
		node.addContent(searches);

		Element elePrivileges = new Element("privileges");
		node.addContent(elePrivileges);

		
		// FIXME : here we should make harvested record visible to
		// Internet/Intranet group ?
		// If not this is not really useful.
		Element categories = new Element("categories");
		node.addContent(categories);

		// -- Check an harvester already exist
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		HarvestManager hm = gc.getHarvestManager();
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);
		// FIXME : Unable to find the getHarvesterID method from patch provided
		// for now id is null
		// String id = hm.getHarvesterID(strSource);
		// Here we should do an update of an existing node if exist.
		String id = null;
		id = hm.add(dbms, node);
		node.setAttribute("id", id);
		
//		if (id == null) {
//			
//		} else {
//			node.setAttribute("id", id);
//			if (!hm.update(dbms, node))
//				return null;
//		}

		return node;
	}

	/**
	 * Run harvesting configuration asynchronously
	 * 
	 * @param request
	 * @param node
	 * @param context
	 * @return
	 * @throws InvalidParameterValueEx
	 */
	private Element processAsynchronous(Element request, Element node,
			ServiceContext context) throws InvalidParameterValueEx {
		Element response = new Element(getName() + "Response",
				Csw.NAMESPACE_CSW);
		try {
			doHarvest(node, context, false, request.getChildText(
					"ResponseHandler", Csw.NAMESPACE_CSW));
		} catch (Exception e) {
			throw new InvalidParameterValueEx("Harvest process asynchronous:",
					e.getMessage());
		}

		// response
		Element acknowledgement = new Element("Acknowledgement",
				Csw.NAMESPACE_CSW);
		String strTime = null;
		ISODate date = new ISODate();
		strTime = date.toString();

		if (strTime != null)
			acknowledgement.setAttribute("timeStamp", strTime);

		Element echoedRequest = new Element("EchoedRequest", Csw.NAMESPACE_CSW);
		echoedRequest.addContent(request);
		acknowledgement.addContent(echoedRequest);

		Element requestId = new Element("RequestId", Csw.NAMESPACE_CSW);
		// TODO : this requestId is strange ?
		requestId.addContent(UUID.randomUUID().toString());
		acknowledgement.addContent(requestId);

		response.addContent(acknowledgement);

		return response;
	}

	/**
	 * Run harvesting synchronously
	 * 
	 * @param request
	 * @param node
	 * @param context
	 * @return
	 * @throws InvalidParameterValueEx
	
	private Element processSynchronous(Element request, Element node,
			ServiceContext context) throws InvalidParameterValueEx {
		Element response = new Element(getName() + "Response",
				Csw.NAMESPACE_CSW);
		Element child = null;
		try {
			child = doHarvest(node, context, true, null);
			if (child != null) {
				response.addContent(child);
			}
		} catch (Exception e) {
			// FIXME :
			throw new InvalidParameterValueEx("Harvest process synchronous:", e
					.getMessage());
		}

		return response;
	} */

	/**
	 * @param params
	 * @param context
	 * @param synchronous
	 * @param responseHandler
	 * @return
	 * @throws Exception
	 */
	private Element doHarvest(Element params, ServiceContext context,
			boolean synchronous, String responseHandler) throws Exception {
		Element response = null;

		// params
		String id = params.getAttributeValue("id");
		Element activeParams = new Element("request");
		Element idele = new Element("id");
		idele.addContent(id);
		activeParams.addContent(idele);

		// run
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		HarvestManager hm = gc.getHarvestManager();
		AbstractHarvester ah = hm.getHarvester(id);
//		if (synchronous) {
//			// Harvester
//			// FIXME unable to find this method from patch :
//			// response = ah.doHarvest();
//			response = null;
//		} else
//		// Start OK
//		{
//			if (responseHandler != null) {
//				// FIXME unable to find this method from patch :
//				// ah.setResponseHandler(responseHandler);
//			}

			response = Util.exec(activeParams, context, new Util.Job() {
				public OperResult execute(Dbms dbms, HarvestManager hm,
						String id) throws SQLException {
					hm.start(dbms, id);
					return hm.run(dbms, id);
				}
			});

//		}
		return response;
	}

	private String operationId = "";
}

// =============================================================================

