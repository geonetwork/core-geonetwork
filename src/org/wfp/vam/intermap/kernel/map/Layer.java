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

import org.wfp.vam.intermap.kernel.map.mapServices.MapService;

public class Layer
{
	private final MapService _service;
	private float _transparency = 1.0F;
	private boolean _expanded = true; 	// Used for ARCims services only
	private boolean _visible = true;

	public Layer(MapService service)
	{
		_service = service;
	}

//		public void setService(MapService service){_service = service;}
	public MapService getService(){return _service;}

	public void setTransparency(float transparency){_transparency = transparency;}
	public float getTransparency(){return _transparency;}
	public int getIntTransparency(){return  (int)(_transparency * 100);}

	// Used for ARCims services only
	public void   setExpanded(boolean isExpanded){_expanded = isExpanded;}
	public boolean isExpanded(){return _expanded;}

	public void setVisible(boolean isVisibile){_visible = isVisibile;}
	public boolean isVisible(){return _visible;}
}

