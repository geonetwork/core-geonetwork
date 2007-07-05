/**
 * BoundingBox.java
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.kernel.map.mapServices;

import java.util.List;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.constants.MapServices;

public class BoundingBox
{
	// Bounding Box coordinates
	private float northBound, southBound, eastBound, westBound;

	public BoundingBox() { setDefault(); }

	public void setDefault() {
		northBound = MapServices.DEFAULT_NORTH;
		southBound = MapServices.DEFAULT_SOUTH;
		eastBound = MapServices.DEFAULT_EAST;
		westBound = MapServices.DEFAULT_WEST;
	}

	public BoundingBox(float north, float south, float east, float west)
//		throws Exception
	{
		// Throw an exception if not valid coordinates
//		if (south >= north || north > Constants.MAX_LATITUDE
//			|| south < -Constants.MAX_LATITUDE)
//			throw new Exception("Illegal bounding box");

		northBound = north;
		southBound = south;
//		if (Math.abs(west - east) > 360) {
//			eastBound = Constants.DEFAULT_EAST;
//			westBound = Constants.DEFAULT_WEST;
//		}
//		else {
			eastBound = east;
			westBound = west;
//		}

		// IMPORTANT: Calculate the module of east and west
	}

	public String toString() {
		return("N: " + northBound + " S: " + southBound + " E: " + eastBound + " W: " + westBound);
	}

	public float getNorth() { return northBound; }

	public float getSouth() { return southBound; }

	public float getEast() { return eastBound; }

	public float getWest() { return westBound; }

	/**
	 * Centers the BoundingBox to the specified coordinates.
	 * If the resulting coordinates are not valid, moves to the
	 * nearest valid bounding box.
	 *
	 *
	 * @param    latitude            the new latitude
	 * @param    longitude           the new longitude
	 *
	 * @throws   Exception
	 *
	 */
	public BoundingBox moveTo(float x, float y)
//		throws Exception
	{
		// Throw an exception if not valid coordinates
//		if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180)
//			throw new Exception();

		float semiNS = (northBound - southBound) / 2;
		float semiEW = Math.abs(eastBound - westBound) / 2;

		// Calculate the temporary coordinates (maybe not valid)
		float tNorth = y + semiNS;
		float tSouth = y - semiNS;
		float tEast = x + semiEW;
		float tWest = x - semiEW;

		// Fix if not valid coordinates
//		if (tNorth > Constants.MAX_LATITUDE) {
//			northBound = Constants.MAX_LATITUDE;
//			southBound = tSouth - (tNorth - Constants.MAX_LATITUDE);
//		}
//		else {
//			northBound = tNorth;
//		}
//		if (tSouth < - Constants.MAX_LATITUDE) {
//			southBound = - Constants.MAX_LATITUDE;
//			northBound = tNorth + (Constants.MAX_LATITUDE - tSouth);
//		}
//		else {
//			southBound = tSouth;
//		}

		northBound = tNorth;
		southBound = tSouth;
		eastBound = tEast;
		westBound = tWest;

		return this;
		// IMPORTANT: Calculate the module of east and west
	}

	public BoundingBox move(float x, float y)
	{
		northBound = northBound + y;
		southBound = southBound + y;
		eastBound = eastBound + x;
		westBound = westBound + x;

		return this;
	}

	/**
	 * Calculates the union of one or more bounding boxes. It still doesn't
	 * handle bounding boxes that cross the 12<sup>th</sup> meridian.
	 *
	 * @param    v                   a  Vector of BoundingBox
	 *
	 * @return   a BoundingBox
	 *
	 * @throws   Exception
	 *
	 */
	public static BoundingBox union(List<BoundingBox> v)
	{
		if (v.size() == 0) {
			return new BoundingBox();
		}

		BoundingBox bb = v.get(0);
		for (int i = 1; i < v.size(); i++) {
			BoundingBox t = v.get(i);
			bb = new BoundingBox (
				Math.max(bb.getNorth(), t.northBound),
				Math.min(bb.getSouth(), t.southBound),
				Math.max(bb.getEast(), t.eastBound),
				Math.min(bb.getWest(), t.westBound)
			);
		}

		return bb;
	}

	public BoundingBox zoom(float factor)
		throws Exception
	{
		if (factor == 0)
			throw new Exception("Illegal zoom factor");
		float deltaNS = Math.abs(northBound - southBound) * (1 - (1 / factor));
		float deltaEW = Math.abs(eastBound - westBound) * (1 - (1 / factor));

		northBound = northBound - deltaNS / 2;
		southBound = southBound + deltaNS / 2;

		if (eastBound > westBound) {
			eastBound = eastBound - deltaEW / 2;
			westBound = westBound + deltaEW / 2;
		}
		else {
			eastBound = eastBound + deltaEW / 2;
			westBound = westBound - deltaEW / 2;
		}

		if (Math.abs(eastBound - westBound) > MapServices.DEFAULT_EAST - MapServices.DEFAULT_WEST) {
			 setDefault();
		}

		return this;
	}

	public Element toElement() {
		return new Element("extent")
			.setAttribute("minx", westBound + "")
			.setAttribute("maxx", eastBound + "")
			.setAttribute("miny", southBound + "")
			.setAttribute("maxy", northBound + "");
	}

}

