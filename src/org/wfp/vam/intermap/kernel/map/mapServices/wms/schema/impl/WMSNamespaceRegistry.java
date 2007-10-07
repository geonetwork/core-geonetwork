/**
 * WMSNamespaceRegistry.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Namespace;

/**
 * @author ETj
 */
public class WMSNamespaceRegistry
{
	public static Namespace getXLink()
	{
		return Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

	}
}

