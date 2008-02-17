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
	private static long counter = 0;

	private final long id;
	private float _lat;
	private float _lon;
	private String _title;
	private String _desc;

	/**
	 * Constructor
	 */
	public Marker(float lat, float lon, String title, String desc)
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

	public boolean isIn(float minlat, float maxlat, float minlon, float maxlon)
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
	public void setLat(float lat)
	{
		_lat = lat;
	}

	/**
	 * Returns Lat
	 */
	public float getLat()
	{
		return _lat;
	}

	/**
	 * Sets Lon
	 */
	public void setLon(float lon)
	{
		_lon = lon;
	}

	/**
	 * Returns Lon
	 */
	public float getLon()
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

