/**
 * WMSLegendURL.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSLegendURL extends WMSURLif
{
	public void setWidth(int width);
	public int getWidth();

	public void setHeight(int height);
	public int getHeight();
}

