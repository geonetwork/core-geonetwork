/**
 * Extents.java
 *
 * @author Stefano Giaccio
 */

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

