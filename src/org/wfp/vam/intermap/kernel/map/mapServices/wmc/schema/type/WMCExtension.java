/**
 * ExtensionType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;

import org.jdom.Element;

/**
 * @author ETj
 */
public interface WMCExtension extends Elementable
{
	public void add(Element etransp);
	public Element getChild(String name);
}

