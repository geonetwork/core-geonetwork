/**
 * WMSEX_GeographicBoundingBox.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSEX_GeographicBoundingBox
{
	public void setWest(float west);
	public float getWest();

	public void setEast(float east);
	public float getEast();

	public void setNorth(float north);
	public float getNorth();

	public void setSouth(float south);
	public float getSouth();
}

