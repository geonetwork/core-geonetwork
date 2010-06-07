//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;


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

