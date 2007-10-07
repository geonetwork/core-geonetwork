/**
 * ServerType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;

/**
 * @author ETj
 */
public interface WMCServer extends Elementable
{
	public WMCOnlineResource addNewOnlineResource();
	public void setOnlineResource(WMCOnlineResource onlineResource);
	public WMCOnlineResource getOnlineResource();

	public void setService(String service);
	public String getService();

	public void setVersion(String version);
	public String getVersion();

	public void setTitle(String title);
	public String getTitle();

}

