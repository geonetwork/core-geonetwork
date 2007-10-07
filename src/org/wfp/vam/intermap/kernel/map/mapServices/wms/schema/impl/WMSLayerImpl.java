/**
 * WMSLayer.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

/**
 * @author ETj
 */
public class WMSLayerImpl implements WMSLayer
{
	// attributes
	private boolean _queryable;
//	private int cascaded; // TODO
//	private boolean opaque = false;
//	private boolean noSubsets = false;
//	private int fixedWidth;
//	private int fixedheight;

	// children
	private String _name = null;	// 0..1
	private String _title = null;	// 1..1
	private String _abstract = null;
	// TODO keywordlist
	private List<String> 						_crsList = new ArrayList<String>(); // CRS 0..n
	private WMSEX_GeographicBoundingBox 		_geoBB = null; // EX_GeographicBoundingBox 0..1
	private List<WMSBoundingBox> 				_bbList = new ArrayList<WMSBoundingBox>(); // BoundingBox 0..n
	// WMS 11x defined also Extent element "Extent element indicates what _values_ along a dimension are valid.". This info is now held in dimension
	private List<WMSDimension> 					_dimensionList = new ArrayList<WMSDimension>(); // 0..n
	// TODO attribution authorityurl identifier
	private List<WMSMetadataURL> 				_metadataURLList = new ArrayList<WMSMetadataURL>(); // MetadataURL 0..n
	// TODO dataurl featurelisturl
	private List<WMSStyle> 						_stylesList = new ArrayList<WMSStyle>(); // Style 0..n
	// TODO minscaleden maxscaleden
	private List<WMSLayer> 						_layerList = new ArrayList<WMSLayer>(); // Layer 0..n

	private WMSLayerImpl __parent = null; // not in spec, but we can use it to find inherited info

	private WMSLayerImpl()
	{}


	static public WMSLayer newInstance()
	{
		return new WMSLayerImpl();
	}

	/**
	 * Method parse
	 */
	public static WMSLayerImpl parse(Element eLayer)
	{
		if(eLayer == null)
			return null;

		WMSLayerImpl layer = new WMSLayerImpl();

		layer.setQueryable(Utils.getBooleanAttrib(eLayer.getAttributeValue("queryable"), false));

		layer.setName(eLayer.getChildText("Name"));
		layer.setTitle(eLayer.getChildText("Title"));
		layer.setAbstract(eLayer.getChildText("Abstract"));
		layer.setCRS((List<Element>)eLayer.getChildren("CRS"));
		layer.setGeoBB(parseGeoBB(eLayer));
		layer.setBoundingBox((List<Element>)eLayer.getChildren("BoundingBox"));

		List<Element>extentList = (List<Element>)eLayer.getChildren("Extent");
		for(Element edim: (List<Element>)eLayer.getChildren("Dimension"))
		{
			WMSDimension dim = WMSFactory.parseDimension(edim);
			dim.setExtent(extentList);
			layer.addDimension(dim);
		}
		for(Element emdu: (List<Element>)eLayer.getChildren("MetadataURL"))
			layer.addMetadataURL(WMSFactory.parseMetadataURL(emdu));
		layer.setStyles((List<Element>)eLayer.getChildren("Style"));
		layer.setLayers((List<Element>)eLayer.getChildren("Layer"), layer);

		return layer;
	}

	/**
	 * Geographic BBox definition changed from 1.1.1 to 1.3.0.
	 * We have to parse either.
	 */
	private static WMSEX_GeographicBoundingBox parseGeoBB(Element eLayer)
	{
		// 1.3.0 element is equivalent to 19115 gbb
		Element egbb = eLayer.getChild("EX_GeographicBoundingBox");
		if(egbb != null)
		{
			return WMSFactory.parseGeoBBox(egbb);
		}

		// 1.1.1
		egbb = eLayer.getChild("LatLonBoundingBox");
		if(egbb != null)
		{
			return WMSFactory.parseGeoBBox111(egbb);
		}

		return null;
	}

	public boolean isQueryable()
	{
		return _queryable;
	}

	/**
	 * Sets Queryable
	 */
	public void setQueryable(boolean queryable)
	{
		_queryable = queryable;
	}


	/**
	 * Sets Name
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Returns Name
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Sets Title
	 */
	public void setTitle(String title)
	{
		_title = title;
	}

	/**
	 * Returns Title
	 */
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Sets Abstract
	 */
	public void setAbstract(String abs)
	{
		_abstract = abs;
	}

	/**
	 * Returns Abstract
	 */
	public String getAbstract()
	{
		return _abstract;
	}

	private void setCRS(List<Element> crsList)
	{
		for(Element ecrs: crsList)
		{
			String crsvalue = ecrs.getText();
			// in pre 111 version, multile crs may be enclosed in one element, space separated
			for(String s: crsvalue.split(" "))
			{
				addCRS(s);
			}
		}
	}

	public void addCRS(String crs)
	{
		_crsList.add(crs);
	}

	public Iterable<String> getCRSIterator()
	{
		return new Iterable<String>()
		{
			public Iterator<String> iterator()
			{
				return _crsList.iterator();
			}
		};
	}

	/**
	 * Method setGeoBB
	 */
	public void setGeoBB(WMSEX_GeographicBoundingBox geoBB)
	{
		_geoBB = geoBB;
	}

	public WMSEX_GeographicBoundingBox getGeoBB()
	{
		if(_geoBB != null)
			return _geoBB;
		else
			if(__parent != null)
				return __parent.getGeoBB();
			else
			{
				System.out.println("WMSLayerImpl: ERROR: no geoBB defined for layer and its ancestors.");
				return null; // be nice and comprehensive and don't fire any Exception
			}
	}

	private void setBoundingBox(List<Element> bblist)
	{
		for(Element ebb: bblist)
		{
			WMSBoundingBox obb = WMSFactory.parseBoundingBox(ebb);
			addBoundingBox(obb);
		}
	}

	/**
	 * Method addBoundingBox
	 */
	public void addBoundingBox(WMSBoundingBox boundingBox)
	{
		_bbList.add(boundingBox);
	}

	public Iterable<WMSBoundingBox> getBoundingBoxIterator()
	{
		return new Iterable<WMSBoundingBox>()
		{
			public Iterator<WMSBoundingBox> iterator()
			{
				return _bbList.iterator();
			}
		};
	}

	public void addDimension(WMSDimension dimension)
	{
		_dimensionList.add(dimension);
	}

	public Iterable<WMSDimension> getDimensionIterator()
	{
		return new Iterable<WMSDimension>()
		{
			public Iterator<WMSDimension> iterator()
			{
				return _dimensionList.iterator();
			}
		};
	}

	public WMSDimension getDimension(String name)
	{
		for(WMSDimension dim: _dimensionList)
			if(dim.getName().equals(name))
			   return dim;

		return null;
	}


	/**
	 * Method addMetadataURL
	 */
	public void addMetadataURL(WMSMetadataURL metadataURL)
	{
		_metadataURLList.add(metadataURL);
	}

	public Iterable<WMSMetadataURL> getMetadataURLIterator()
	{
		return new Iterable<WMSMetadataURL>()
		{
			public Iterator<WMSMetadataURL> iterator()
			{
				return _metadataURLList.iterator();
			}
		};
	}

	/**
	 * Method setStyles
	 */
	private void setStyles(List<Element> styleList)
	{
		for(Element eStyle: styleList)
		{
			addStyle(eStyle);
		}
	}

	/**
	 * Method addStyle
	 */
	private void addStyle(Element eStyle)
	{
		WMSStyle style = WMSFactory.parseStyle(eStyle);
		addStyle(style);
	}

	public void addStyle(WMSStyle style)
	{
		_stylesList.add(style);
	}

	public Iterable<WMSStyle> getStyleIterator()
	{
		return new Iterable<WMSStyle>()
		{
			public Iterator<WMSStyle> iterator()
			{
				return _stylesList.iterator();
			}
		};
	}

	public int getStyleSize()
	{
		return _stylesList.size();
	}

	/**
	 * Method getStyle
	 *
	 * @return   the WMSStyle with given name, or null if none was found
	 */
	public WMSStyle getStyle(String name)
	{
		for(WMSStyle style: _stylesList)
		{
			if(style.getName().equals(name))
				return style;
		}
		return null;
	}

	public WMSStyle getStyle(int index)
	{
		if(-1 < index && index < _stylesList.size())
			return _stylesList.get(index);
		else
			return null;
	}

	/**
	 * Method setLayers
	 */
	private void setLayers(List<Element> layerList, WMSLayerImpl parent)
	{
		for(Element eLayer: layerList)
		{
			WMSLayerImpl layer = parse(eLayer);
			layer.setParent(parent);
			addLayer(layer);
		}
	}

	/**
	 * Method addLayer
	 */
	public void addLayer(WMSLayer layer)
	{
		_layerList.add(layer);
	}

	public WMSLayer getLayer(String name)
	{
		if(name.equals(getName()))
			return this;

		for(WMSLayer layer: _layerList)
		{
			WMSLayer ret = layer.getLayer(name);
			if(ret != null)
				return ret;
		}

		return null; // Layer not found
	}

	public Iterable<WMSLayer> getLayerIterator()
	{
		return new Iterable<WMSLayer>()
		{
			public Iterator<WMSLayer> iterator()
			{
				return _layerList.iterator();
			}
		};
	}

	private void setParent(WMSLayerImpl parent)
	{
		__parent = parent;
	}

	public WMSLayer getParent()
	{
		return __parent;
	}

}

