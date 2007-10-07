/**
 * WMSURLif.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSURLif
{
	public void setFormat(String format);
	public void setFormat(WMSFormat format);
	public WMSFormat getFormat();

	public void setOnlineResource(WMSOnlineResource onlineResource);
	public WMSOnlineResource getOnlineResource();
}

