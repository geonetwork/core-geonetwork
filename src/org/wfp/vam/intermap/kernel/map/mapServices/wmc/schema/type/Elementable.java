/**
 * Elementable.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;

import org.jdom.Element;

/**
 * @author ETj
 */
public interface Elementable
{
	public Element toElement(String name);
}

