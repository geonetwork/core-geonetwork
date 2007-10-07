/**
 * WMSStyle.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSStyle
{
	public void setName(String name);
	public String getName();

	public void setTitle(String title);
	public String getTitle();

	public void setAbstract(String abs);
	public String getAbstract();

	public void addLegendURL(WMSLegendURL legendUrl);
	public WMSLegendURL getLegendURL(int index);
	public Iterable<WMSLegendURL> getLegendURLIterator();

	public void setStyleSheetURL(WMSStyleSheetURL styleSheetURL);
	public WMSStyleSheetURL getStyleSheetURL();

	public void setStyleURL(WMSStyleURL styleURL);
	public WMSStyleURL getStyleURL();
}

