/**
 * MetadataURL.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSMetadataURL
{
	public enum MDTYPE
	{
		FGDC, 		// FGDC
		ISO19115 	// ISO19115
	};

	public void setType(MDTYPE type);
	public MDTYPE getType();

	public void setFormat(WMSFormat format);
	public WMSFormat getFormat();

	public void setOnlineResource(WMSOnlineResource onlineResource);
	public WMSOnlineResource getOnlineResource();
}

