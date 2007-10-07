/**
 * WMSRequest.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSRequest
{
	public void setGetCapabilities(WMSOperationType getCapabilities);
	public WMSOperationType getGetCapabilities();

	public void setGetMap(WMSOperationType getMap);
	public WMSOperationType getGetMap();

	public void setGetFeatureInfo(WMSOperationType getFeatureInfo);
	public WMSOperationType getGetFeatureInfo();
}

