/**
 * GeneralType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;

/**
 * @author ETj
 */
public interface WMCGeneral extends Elementable
{
	public WMCWindow addNewWindow();
	public void setWindow(WMCWindow window);
	public WMCWindow getWindow();

	public WMCBoundingBox addNewBoundingBox();
	public void setBoundingBox(WMCBoundingBox boundingBox);
	public WMCBoundingBox getBoundingBox();

	public void setTitle(String title);
	public String getTitle();

	public void setAbstract(String abs);
	public String getAbstract();

	public WMCExtension addNewExtension();
	public void setExtension(WMCExtension extension);
	public WMCExtension getExtension();
}

