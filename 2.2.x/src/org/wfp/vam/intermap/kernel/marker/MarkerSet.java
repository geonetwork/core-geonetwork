/**
 * MarkerSet.java
 *
 */

package org.wfp.vam.intermap.kernel.marker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.marker.Marker;

/**
 * A set of Markers
 *
 * @author ETj
 */
public class MarkerSet implements Iterable<Marker>
{
	private List<Marker> _list = new ArrayList<Marker>();

	public MarkerSet()
	{}

	public void add(Marker m)
	{
		_list.add(m);
	}

	public Marker get(long id)
	{
		for(Marker marker: _list)
		{
			if(marker.getId() == id)
				return marker;
		}

		return null;
	}

	public boolean isEmpty()
	{
		return _list.isEmpty();
	}

	public boolean remove(long id)
	{
		for(Iterator i = _list.iterator(); i.hasNext(); )
		{
			Marker marker = (Marker)i.next();
			if(marker.getId() == id)
			{
				i.remove();
				return true;
			}
		}

		return false;
	}

	public Element toElement()
	{
		Element ret = new Element("markers");

		for(Marker m: _list)
			ret.addContent(m.toElement());

		return ret;
	}

	public MarkerSet select(BoundingBox bb)
	{
		return select(bb.getSouth(), bb.getNorth(), bb.getWest(), bb.getEast());
	}

	public MarkerSet select(float minlat, float maxlat, float minlon, float maxlon)
	{
		MarkerSet ret = new MarkerSet();

		for(Marker m: _list)
			if(m.isIn(minlat, maxlat, minlon, maxlon))
				ret.add(m);

		return ret;
	}

	/**
	 * Returns an iterator over a set of elements of type T.
	 *
	 * @return an Iterator.
	 */
	public Iterator<Marker> iterator()
	{
		return _list.iterator();
	}

}

