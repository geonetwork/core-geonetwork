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

package org.wfp.vam.intermap.kernel.map;

import java.util.*;

import org.jdom.*;

import org.wfp.vam.intermap.Constants;

/**
 * This class holds informations about map servers listed in the map servers
 * configuration file.
 */
public class DefaultMapServers {

	private static Element mapServers;
	private static Element mapContexts;
	private static Hashtable htContexts;
	private static Element _defaultContext;

	/**
	 * Initializes map servers configuration
	 *
	 * @param    config              an Element
	 *
	 */
	public static void init(Element config) {
		// Get map servers from the configuration file
		mapServers = getServersEl(config);
		mapContexts = getContextsEl(config);
		htContexts = getContextsHt(config);

		_defaultContext = getDefaultContext(config);
//		mapContexts = elContexts;
	}

	private static Element getServersEl(Element config)
	{
		Element elServers = (Element)(config.getChild(Constants.MAP_SERVERS).detach());
		List<Element> servers = elServers.getChildren();

		// Set the id attribute for each server
		int n = 1;
		for(Element elServer: servers)
			elServer.setAttribute(Constants.MAP_SERVER_ID, "" + n++);

		return elServers;
	}

	private static Element getContextsEl(Element config)
	{
		Element elContexts = (Element)config.getChild(Constants.MAP_CONTEXTS).clone();
		List<Element> contextList = elContexts.getChildren(Constants.MAP_CONTEXT);

		// Set the id attribute for each server
		int n = 1;
		for(Element elContext: contextList)
			elContext.setAttribute(Constants.MAP_CONTEXT_ID, "" + n++);

//		System.out.println(Xml.getString(elContexts));
		return elContexts;
	}

	/**
	 * @author ETj
	 */
	private static Element getDefaultContext(Element config)
	{
		Element elContexts = config.getChild(Constants.MAP_CONTEXTS);
		Element defCon = (Element)elContexts.getChild(Constants.MAP_DEFAULTCONTEXT).clone();

		return defCon;
	}

	private static Hashtable getContextsHt(Element config)
	{
		Element elContexts = (Element)config.getChild(Constants.MAP_CONTEXTS).clone();
		List<Element> lContexts = elContexts.getChildren();

		// Set the id attribute for each server
		Hashtable htContexts = new Hashtable();

		int n = 1;
		for(Element elContext: lContexts)
			htContexts.put(new Integer(n++), elContext);

		return htContexts;
	}

	/**
	 * Selects all default map servers
	 *
	 * @return   an Element containing all default map servers
	 *
	 */
	public static Element getServers() {
		return (Element)mapServers.clone();
	}

	/**
	 * Selects all default map contexts
	 *
	 * @return   an Element containing all default contexts
	 *
	 */
	public static Element getContexts() {
		return (Element)mapContexts.clone();
	}

	public static Element getContext(int id) {
		Element context = (Element)htContexts.get(new Integer(id));
		return (Element)context.clone();
	}

	/**
	 * @author ETj
	 */
	public static Element getDefaultContext() {
		return (Element)_defaultContext.clone();
	}


	/**
	 * Selects all default map servers having the selected type attribute
	 *
	 * @param    type                map server type
	 *
	 * @return   an Element containing the selected map servers
	 *
	 */
	public static Element getServers(String type) {
		List servers = mapServers.getChildren();
		Element elServers = new Element(Constants.MAP_SERVERS);

		// Select map servers having the selected type attribute
		for (Iterator i = servers.iterator(); i.hasNext(); ) {
			Element elServer = (Element)i.next();
			if (elServer.getAttributeValue(Constants.MAP_SERVER_TYPE) == type)
				elServers.addContent(elServer);
		}

		return elServers;
	}

	/**
	 * Returns the type of the selected default map server
	 *
	 * @param    id                  a  String
	 *
	 * @return   the type of the mapserver identified by id, 0 if no server
	 * found
	 *
	 * @throws   Exception
	 *
	 */
	public static int getType(String id)
		throws Exception
	{
		List servers = mapServers.getChildren();

		// Look for the mapserver identified by id and return its type
		for (Iterator i = servers.iterator(); i.hasNext(); ) {
			Element server = (Element)i.next();
			if (server.getAttributeValue(Constants.MAP_SERVER_ID).equals(id))
				return Integer.parseInt(
					server.getAttributeValue(Constants.MAP_SERVER_TYPE)
				);
		}
		// Return 0 if no server found
		return 0;
	}

	/**
	 * Returns the url of the selected default mapserver
	 *
	 * @param    id                  a  String
	 *
	 * @return   the url of the mapserver identified by id, null if no server
	 * found
	 *
	 * @throws   Exception
	 *
	 */
	public static String getUrl (String id)
		throws Exception
	{
		List servers = mapServers.getChildren();

		// Look for the mapserver identified by id and return its url
		for (Iterator i = servers.iterator(); i.hasNext(); ) {
			Element server = (Element)i.next();
			if (server.getAttributeValue(Constants.MAP_SERVER_ID).equals(id))
				return server.getChildText(Constants.MAP_SERVER_URL);
		}
		// Return null if no server found
		return null;
	}

}

