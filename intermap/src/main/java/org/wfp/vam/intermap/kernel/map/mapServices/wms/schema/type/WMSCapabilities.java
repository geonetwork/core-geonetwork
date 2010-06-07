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

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSCapabilities
{
	public enum WMSVer
	{
		V100("1.0.0"),
		V110("1.1.0"),
		V111("1.1.1"),
		V130("1.3.0");

		private String _verString;

		private WMSVer(String v)
		{
			_verString = v;
			System.out.println("CREATING VERSION " + this.name() + " with string '"+v);//+"' on map " + _map );
		}

		public String toString()
		{
			return _verString;
		}

		public static WMSVer parse(String ver)
		{
			for(WMSVer v: WMSVer.values())
				if(v.toString().equals(ver))
					return v;
			System.out.println("WARNING: WMSVer: version '"+ver+"' is not defined internally. Please update the internal version list.");
			return null;
		}
	}

	public static final WMSVer DEFVERSION = WMSVer.V130;
	/**
	 * Method setService
	 */
	public void setService(WMSService oservice);
	public WMSService getService();

	/**
	 * Sets Capability
	 */
	public void setCapability(WMSCapability capability);
	public WMSCapability getCapability();

	/**
	 * Sets Version
	 */
	public void setVersion(String version);
	public void setVersion(WMSVer version);
	public WMSVer getVersion();

	public void setUpdateSequence(String updateSequence);
	public String getUpdateSequence();

}

