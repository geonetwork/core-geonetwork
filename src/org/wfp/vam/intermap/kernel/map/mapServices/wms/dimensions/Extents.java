/**
 * Extents.java
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.dimensions;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.dimensions.time.*;

import java.util.*;

import org.jdom.*;

public class Extents
{
	private Element elExtents = new Element("extents");

	public Extents(Element elService) {
		List lExtents = elService.getChildren("Extent");

		for (Iterator i = lExtents.iterator(); i.hasNext(); ) {
			Element elExtent =(Element)i.next();
			Element extent = null;

			String name = elExtent.getAttributeValue("name");

			if (name.equals("time")) {  // only time for now
				TimeExtent te = new TimeExtent(elExtent);
				extent = te.getJdom();
			}
			
			if (extent != null) { // DEBUG
				extent.setAttribute("default", elExtent.getAttributeValue("default"));
				elExtents.addContent(extent);
			}
		}

	}

	public Element getJdom() { return elExtents; }
}

