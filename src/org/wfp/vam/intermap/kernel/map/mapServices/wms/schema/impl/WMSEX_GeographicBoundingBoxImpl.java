/**
 * BoundingBoxType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;



import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSEX_GeographicBoundingBox;

/**
 * This entity has been introduced with WMS 1.3.0 specification.
 * Earlier specs referred to LatLonBoundingBox, with different attrib names but same semantics.
 * <BR>
 * This class can also parse the elements of LatLonBoundingBox
 *
 * @author ETj
 */
public class WMSEX_GeographicBoundingBoxImpl implements WMSEX_GeographicBoundingBox
{
//	private WMSLongitudeType _westBoundLongitude = null;
//	private WMSLongitudeType _eastBoundLongitude = null;
//	private WMSLatitudeType _northBoundLatitude = null;
//	private WMSLatitudeType _southBoundLatitude = null;
	private float _west = Float.NaN;
	private float _east = Float.NaN;
	private float _north = Float.NaN;
	private float _south = Float.NaN;

	private WMSEX_GeographicBoundingBoxImpl()
	{
	}


	public static WMSEX_GeographicBoundingBox newInstance()
	{
		return new WMSEX_GeographicBoundingBoxImpl();
	}

	/**
	 * Method parse
	 */
	public static WMSEX_GeographicBoundingBox parse(Element ebb)
	{
		WMSEX_GeographicBoundingBoxImpl wbb = new WMSEX_GeographicBoundingBoxImpl();

		wbb.setWest(Float.parseFloat(ebb.getChildText("westBoundLongitude")));
		wbb.setEast(Float.parseFloat(ebb.getChildText("eastBoundLongitude")));
		wbb.setSouth(Float.parseFloat(ebb.getChildText("southBoundLatitude")));
		wbb.setNorth(Float.parseFloat(ebb.getChildText("northBoundLatitude")));

		return wbb;
	}

	public static WMSEX_GeographicBoundingBox parse111(Element ebb)
	{
		WMSEX_GeographicBoundingBoxImpl wbb = new WMSEX_GeographicBoundingBoxImpl();

		wbb.setNorth(Float.parseFloat(ebb.getAttributeValue("maxy")));
		wbb.setEast(Float.parseFloat(ebb.getAttributeValue("maxx")));
		wbb.setSouth(Float.parseFloat(ebb.getAttributeValue("miny")));
		wbb.setWest(Float.parseFloat(ebb.getAttributeValue("minx")));

		return wbb;
	}

	/**
	 * Sets West
	 */
	public void setWest(float west)
	{
		_west = west;
	}

	/**
	 * Returns West
	 */
	public float getWest()
	{
		return _west;
	}

	/**
	 * Sets East
	 */
	public void setEast(float east)
	{
		_east = east;
	}

	/**
	 * Returns East
	 */
	public float getEast()
	{
		return _east;
	}

	/**
	 * Sets North
	 */
	public void setNorth(float north)
	{
		_north = north;
	}

	/**
	 * Returns North
	 */
	public float getNorth()
	{
		return _north;
	}

	/**
	 * Sets South
	 */
	public void setSouth(float south)
	{
		_south = south;
	}

	/**
	 * Returns South
	 */
	public float getSouth()
	{
		return _south;
	}

}

