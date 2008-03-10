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

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;

import org.jdom.Document;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCLayerList;

/**
 * @author ETj
 */
public interface WMCViewContext
{
	public void setId(String toString);
	public void setVersion(String p0);

	public WMCGeneral addNewGeneral();
	public void setGeneral(WMCGeneral general);
	public WMCGeneral getGeneral();

	public WMCLayerList addNewLayerList();
	public void setLayerList(WMCLayerList layerList);
	public WMCLayerList getLayerList();

	public Document getContextDocument();
	public Element toElement();

}

