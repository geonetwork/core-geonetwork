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
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.om;

/**
 * @author ETj
 */
public class WMCSLD
{
	private String name = null; // 0..1
	private String title = null; // 0..1
	
	private Choice0 choice0 = null;
	
	public static class Choice0
	{
		WMCOnlineResource onlineResource;

		public WMCOnlineResource getOnlineResource() {
			return onlineResource;
		}

		public void setOnlineResource(WMCOnlineResource onlineResource) {
			this.onlineResource = onlineResource;
		}
				
	}

	class Choice1
	{
//		SLDStyledLayerDescriptor sld = null; // TODO
	}
	
	class Choice2
	{
//		SLDFeatureTypeStyle fts = null; // TODO
	}
	
	private WMCSLD()
	{}

	public static WMCSLD newInstance()
	{
		return new WMCSLD();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	
	public Choice0 getChoice0() {
		return choice0;
	}

	public void setChoice0(Choice0 choice0) {
		this.choice0 = choice0;
	}
	
}
