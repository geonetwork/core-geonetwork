/**
 * WMSBaseBoundingBox.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSBaseBoundingBox
{
	public void setMiny(float south);
	public void setMinx(float west);
	public void setMaxy(float north);
	public void setMaxx(float east);
	public void setSRS(String srs);

	public float getMiny();
	public float getMinx();
	public float getMaxy();
	public float getMaxx();
	public String getSRS();
}

