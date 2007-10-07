/**
 * ViewContextType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;

import org.jdom.Document;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCLayerList;

/**
 * @author ETj
 */
public interface WMCViewContext
{
	public void setId(String toString);
	public void setVersion(String p0);

	public WMCGeneral addNewGeneral();
	public void setGeneral(WMCGeneral general);
	public WMCGeneral getGeneral();

	public WMCLayerList addNewLayerList();
	public void setLayerList(WMCLayerList layerList);
	public WMCLayerList getLayerList();

	public Document getContextDocument();
	public Element toElement();

}

