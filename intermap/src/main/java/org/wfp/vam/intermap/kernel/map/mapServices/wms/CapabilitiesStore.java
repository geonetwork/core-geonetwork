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

package org.wfp.vam.intermap.kernel.map.mapServices.wms;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.WMSCapabilitiesImpl;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSCapabilities;


/**
 * Caches/retrieves getCapabilities documents for WMS servers.<BR>
 * You can get the <UL>
 * <LI>JDom version ({#link getCapabilites(String)})</LI> or the
 * <LI>XMLBeans one {#link WMSCapabilitiesDocument} {link getCapabilityDocument(String serverUrl)})</LI>
 *
 * @author Emanuele Tajariol
 */
public class CapabilitiesStore
{
	private static Map<String, Capability> _store = new Hashtable<String, Capability>();

	private static final int CACHE_HOURS = 12;
	private static boolean _useCache = true;

	public static void useCache(boolean useCache)
	{
		_useCache = useCache;
	}

	public static Element getCapabilities(String serverUrl) throws Exception
	{
		return getCapabilities(serverUrl, false);
	}

	/**
	 * Method getCapability
	 */
	public static Element getCapabilities(String serverUrl, boolean forceRefresh) throws Exception
	{
		return (Element)retrieveCapabilites(serverUrl, forceRefresh).getCapa().clone();
	}

	public static WMSCapabilities getCapabilityDocument(String serverUrl) throws Exception
	{
		return getCapabilityDocument(serverUrl, false);
	}

	/**
	 * Method getCapabilityDocument
	 */
	public static WMSCapabilities getCapabilityDocument(String serverUrl, boolean forceRefresh) throws Exception
	{
		Capability c = retrieveCapabilites(serverUrl, forceRefresh);

		// CapaDoc is lazily built
		if(c.getCapaDoc() != null)
			return c.getCapaDoc();

		try
		{
			WMSCapabilities cd = WMSCapabilitiesImpl.parse(c.getCapa());
			c.setCapaDoc(cd);
			return cd;
		}
		catch (JDOMException e)
		{
			System.out.println("Internal error: capability for serverUrl '"+serverUrl+"' is not parsable.");
			return null;
		}
	}

	/**
	 * Retrieve a Capability, either from the cache or by requesting it to the server.
	 * This method will handle the cache, if needed.
	 */
	private static Capability retrieveCapabilites(String serverUrl, boolean forceRefresh) throws Exception
	{
		if ( ! _useCache)
			return new Capability(WmsGetCapClient.sendGetCapRequest(serverUrl));

		Capability capabilities = _store.get(serverUrl);
		Calendar now = Calendar.getInstance();

		if (capabilities == null || now.after(capabilities.getExpiration()) || forceRefresh)
		{
			Element ce = WmsGetCapClient.sendGetCapRequest(serverUrl);
			Calendar expirationDate = Calendar.getInstance();
			expirationDate.add(Calendar.HOUR, CACHE_HOURS);

			capabilities = new Capability(ce);
			capabilities.setExpiration(expirationDate);
			_store.put(serverUrl, capabilities);
		}

		return capabilities;
	}

	static class Capability
	{
		private final Element 	_capa;
		private WMSCapabilities _gcd;
		private Calendar 		_expiration;

		public Capability(Element capa)
		{
			_capa = capa;
		}

		public WMSCapabilities getCapaDoc()	{ return _gcd; }

		public Element getCapa()			{ return _capa; }

		public Calendar getExpiration()		{ return _expiration; }

		public void setExpiration(Calendar exp)
		{
			_expiration = exp;
		}

		public void setCapaDoc(WMSCapabilities capaDoc)
		{
			_gcd = capaDoc;
		}

	}
}

