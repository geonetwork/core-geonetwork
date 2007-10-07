/**
 * WMSCapability.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSCapability
{
	public void setRequest(WMSRequest request);
	public WMSRequest getRequest();

	public void setLayer(WMSLayer layer);
	public WMSLayer getLayer();
}

