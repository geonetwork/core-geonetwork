/**
 * LayerType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;

/**
 * @author ETj
 */
public interface WMCLayer extends Elementable
{
	public void setServer(WMCServer server);
	public WMCServer getServer();
	public WMCServer addNewServer();

	public void setName(String name);
	public String getName();

	public void setTitle(String title);
	public String getTitle();

	public void setAbstract(String abs);
	public String getAbstract();

	public void setSRS(String sRS);
	public String getSRS();

	public void setQueryable(boolean queryable);
	public boolean isQueryable();

	public void setHidden(boolean hidden);
	public boolean isHidden();

	public WMCExtension addNewExtension();
	public void setExtension(WMCExtension extension);
	public WMCExtension getExtension();
}

