/**
 * WMSOperationType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

import java.util.Set;

/**
 * @author ETj
 */
public interface WMSOperationType
{
	// Format
	public void addFormat(WMSFormat format);
	public Set<WMSFormat> getFormats();

	// DCPType
	public void addDCPType(WMSDCPType dcpType);
	public WMSDCPType getDCPType(int index);
	public Iterable<WMSDCPType> getDCPTypeIterator();
}

