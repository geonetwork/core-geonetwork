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

package org.wfp.vam.intermap.kernel.map.mapServices.wms.dimensions;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.dimensions.time.TimeExtent;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSDimension;

public class Extents
{
	private Element _extents = new Element("extents");

	/**
	 * @deprecated use Extents(Iterable<WMSDimension> iDim)
	 */
	public Extents(Element elService)
	{
		List lExtents = elService.getChildren("Extent");

		for (Iterator i = lExtents.iterator(); i.hasNext(); ) {
			Element elExtent = (Element)i.next();
			Element extent = null;

			String name = elExtent.getAttributeValue("name");

			if (name.equals("time")) {  // only time for now
				TimeExtent te = new TimeExtent(elExtent);
				extent = te.getJdom();
			}

			if (extent != null) { // DEBUG
				extent.setAttribute("default", elExtent.getAttributeValue("default"));
				_extents.addContent(extent);
			}
		}

	}

	/**
	 * @author ETj
	 */
	public Extents(Iterable<WMSDimension> iDim)
	{
		for(WMSDimension dim: iDim)
		{
			Element extent = null;
			String name = dim.getName();
			if (name.equals("time"))  // only time for now
			{
				TimeExtent te = new TimeExtent(dim.getValue());
				extent = te.getJdom();
			}

			if (extent != null) // DEBUG
			{
				extent.setAttribute("default", dim.getDefault());
				_extents.addContent(extent);
			}
		}
	}


	public Element getJdom() { return _extents; }
}

