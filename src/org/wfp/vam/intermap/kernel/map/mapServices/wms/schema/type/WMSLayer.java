/**
 * WMSLayer.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.WMSLayerImpl;


/**
 * @author ETj
 */
public interface WMSLayer
{
	public void setQueryable(boolean queryable);
	public boolean isQueryable();


	public void setName(String name);
	public String getName();

	public void setTitle(String title);
	public String getTitle();

	public void setAbstract(String abs);
	public String getAbstract();

	public void addCRS(String crs);
	public Iterable<String> getCRSIterator();

	public void setGeoBB(WMSEX_GeographicBoundingBox geoBB);
	public WMSEX_GeographicBoundingBox getGeoBB();

	public void addBoundingBox(WMSBoundingBox boundingBox);
	public Iterable<WMSBoundingBox> getBoundingBoxIterator();

	public void addDimension(WMSDimension dimension);
	public Iterable<WMSDimension> getDimensionIterator();
	public WMSDimension getDimension(String name);

	public void addMetadataURL(WMSMetadataURL metadataURL);
	public Iterable<WMSMetadataURL> getMetadataURLIterator();

	public void addStyle(WMSStyle style);
	public Iterable<WMSStyle> getStyleIterator();
	public WMSStyle getStyle(String name);
	public WMSStyle getStyle(int index);
	public int getStyleSize();

	public void addLayer(WMSLayer layer);
	public Iterable<WMSLayer> getLayerIterator();
	public WMSLayer getLayer(String name);

	public WMSLayer getParent();


}

