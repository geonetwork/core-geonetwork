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

import java.util.List;
import org.jdom.Element;


/**
 * @author ETj
 */
public interface WMSDimension
{
	public void setValue(String value);
	public String getValue();

	public void setName(String name);
	public String getName();

	public void setUnits(String units);
	public String getUnits();

	public void setUnitSymbol(String unitSymbol);
	public String getUnitSymbol();

	public void setDefault(String def);
	public String getDefault();

	public void setMultipleValues(boolean multipleValues);
	public boolean isMultipleValues();

	public void setNearestValue(boolean nearestValue);
	public boolean isNearestValue();

	public void setCurrent(boolean current);
	public boolean isCurrent();

	/**
	 * Import in current 1.3.0 Dimension the attributes that in 1.1.x were in Extent elements
	 */
	public void setExtent(List<Element> extentList);

}

