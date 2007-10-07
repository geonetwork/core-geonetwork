/**
 * WMSDCPType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

/**
 * @author ETj
 */
public interface WMSDCPType
{
	public void setHttpGetHref(String httpGetHref);
	public String getHttpGetHref();

	public void setHttpPostHref(String httpPostHref);
	public String getHttpPostHref();
}

