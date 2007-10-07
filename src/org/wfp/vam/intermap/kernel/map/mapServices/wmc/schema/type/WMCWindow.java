/**
 * WindowType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;

/**
 * @author ETj
 */
public interface WMCWindow extends Elementable
{
	public void setHeight(int height);
	public int getHeight();

	public void setWidth(int width);
	public int getWidth();
}

