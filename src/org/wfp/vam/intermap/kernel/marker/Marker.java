/**
 * Marker.java
 */

package org.wfp.vam.intermap.kernel.marker;

/**
 * A Marker is a geographic point, with a title and a description.
 *
 * @author ETj
 */
import org.jdom.Element;

public class Marker
{
	private static long counter = 1;

	private final long id;
	private double _lat;
	private double _lon;
	private String _title;
	private String _desc;

	/**
	 * Constructor
	 */
	public Marker(double lat, double lon, String title, String desc)
	{
		synchronized(getClass())
		{
			id = counter++;
		}

		_lat = lat;
		_lon = lon;
		_title = title;
		_desc = desc;
	}

	/**
	 * Returns Id
	 *
	 * @return    a  long
	 */
	public long getId()
	{
		return id;
	}

	public boolean isIn(double minlat, double maxlat, double minlon, double maxlon)
	{
		if(_lat < minlat) return false;
		if(maxlat < _lat) return false;
		if(_lon < minlon) return false;
		if(maxlon < _lon) return false;

		return true;
	}

	/**
	 * Sets Lat
	 */
	public void setLat(double lat)
	{
		_lat = lat;
	}

	/**
	 * Returns Lat
	 */
	public double getLat()
	{
		return _lat;
	}

	/**
	 * Sets Lon
	 */
	public void setLon(double lon)
	{
		_lon = lon;
	}

	/**
	 * Returns Lon
	 */
	public double getLon()
	{
		return _lon;
	}

	/**
	 * Sets Title
	 */
	public void setTitle(String title)
	{
		_title = title;
	}

	/**
	 * Returns Title
	 */
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Sets Desc
	 */
	public void setDesc(String desc)
	{
		_desc = desc;
	}

	/**
	 * Returns Desc
	 */
	public String getDesc()
	{
		return _desc;
	}


	public Element toElement()
	{
		Element ret = new Element("marker");

		ret.setAttribute("id", ""+id);

		ret.setAttribute("lat", ""+_lat);
		ret.setAttribute("lon", ""+_lon);

		if(_title != null)
			ret.addContent(new Element("title").setText(_title));

		if(_desc != null)
			ret.addContent(new Element("description").setText(_desc));

		return ret;
	}
}

