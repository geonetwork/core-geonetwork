/**
 * WMSService.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSService
{
	public void setName(String name);
	public String getName();

	public void setTitle(String title);
	public String getTitle();

	public void setAbstract(String abs);
	public String getAbstract();

	public void setOnlineResource(WMSOnlineResource onlineResource);
	public WMSOnlineResource getOnlineResource();
}

