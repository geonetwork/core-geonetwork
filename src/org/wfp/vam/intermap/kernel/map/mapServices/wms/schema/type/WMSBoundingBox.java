/**
 * BoundingBoxType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSBoundingBox extends WMSBaseBoundingBox
{
	public void setResy(float resy);
	public void setResx(float resx);

	public float getResy();
	public float getResx();
}

