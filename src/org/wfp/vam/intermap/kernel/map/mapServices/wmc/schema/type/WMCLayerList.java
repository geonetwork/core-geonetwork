/**
 * LayerListType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type;


/**
 * @author ETj
 */
public interface WMCLayerList extends Elementable
{
	public void addLayer(WMCLayer wmcLayer);
	public WMCLayer addNewLayer();
	public Iterable<WMCLayer> getLayerIterator();
}

