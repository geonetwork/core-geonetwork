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
public class WMCStyle
{
	private boolean _current = false;
	
	private Choice0 choice0 = null;
	private Choice1 choice1 = null;
		
	public static class Choice0
	{
		String name; // 1..1
		String title; // 1..1
		String _abstract = null; // 0..1
		
		WMCURL legendURL=null; // 0..1		

		public String getAbstract() {
			return _abstract;
		}

		public void setAbstract(String abs) {
			this._abstract = abs;
		}

		public WMCURL getLegendURL() {
			return legendURL;
		}

		public void setLegendURL(WMCURL legendURL) {
			this.legendURL = legendURL;
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
	}
	
	public static class Choice1
	{
		WMCSLD sld = null; // 0..1

		public WMCSLD getSld() {
			return sld;
		}

		public void setSld(WMCSLD sld) {
			this.sld = sld;
		}
		
	}

	private WMCStyle()
	{}

	public static WMCStyle newInstance()
	{
		return new WMCStyle();
	}


	public boolean isCurrent() {
		return _current;
	}

	public void setCurrent(boolean current) {
		this._current = current;
	}

	public WMCStyle.Choice0 getChoice0() {
		return choice0;
	}

	public void setChoice(Choice0 choice) {
		if(choice != null && choice1 != null)
			throw new IllegalStateException("Choice1 is already set for this Style");		
		this.choice0 = choice;
	}

	public WMCStyle.Choice1 getChoice1() {
		return choice1;
	}

	public void setChoice(Choice1 choice) {
		if(choice != null && choice0 != null)
			throw new IllegalStateException("Choice0 is already set for this Style");
		this.choice1 = choice;
	}

}

