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

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import java.util.List;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSDimension;

/**
 * @author ETj
 */
public class WMSDimensionImpl implements WMSDimension
{
	private String _value = null;

	private String _name = null;  // req
	private String _units = null; // req
	private String _unitSymbol = null;
	private String _default = null;
	private boolean _multipleValues = false;
	private boolean _nearestValue = false;
	private boolean _current = false;

	private WMSDimensionImpl()
	{}

	public static WMSDimension newInstance()
	{
		return new WMSDimensionImpl();
	}

	public static WMSDimension parse(Element eDimension)
	{
		WMSDimensionImpl dim = new WMSDimensionImpl();

		dim.setValue(eDimension.getText());

		dim.setName(eDimension.getAttributeValue("name"));
		dim.setUnits(eDimension.getAttributeValue("units"));
		dim.setUnitSymbol(eDimension.getAttributeValue("unitSymbol"));
		dim.setDefault(eDimension.getAttributeValue("default"));
		dim.setMultipleValues(Utils.getBooleanAttrib(eDimension.getAttributeValue("multipleValues"), false));
		dim.setNearestValue(Utils.getBooleanAttrib(eDimension.getAttributeValue("nearestValue"), false));
		dim.setCurrent(Utils.getBooleanAttrib(eDimension.getAttributeValue("current"), false));

		return dim;
	}

	/**
	 * Import in current 1.3.0 Dimension the attributes that in 1.1.x were in Extent elements
	 */
	public void setExtent(List<Element> extentList)
	{
		if(extentList==null)
			return;

		for(Element eext: extentList)
		{
			if( getName().equals(eext.getAttributeValue("name")))
			{
				setDefault(eext.getAttributeValue("default"));
				setNearestValue(Utils.getBooleanAttrib(eext.getAttributeValue("nearestValue"), false));
				break;
			}
		}
	}


	/**
	 * Sets Value
	 */
	public void setValue(String value)
	{
		_value = value;
	}

	/**
	 * Returns Value
	 */
	public String getValue()
	{
		return _value;
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
	 * Sets Units
	 */
	public void setUnits(String units)
	{
		_units = units;
	}

	/**
	 * Returns Units
	 */
	public String getUnits()
	{
		return _units;
	}

	/**
	 * Sets UnitSymbol
	 */
	public void setUnitSymbol(String unitSymbol)
	{
		_unitSymbol = unitSymbol;
	}

	/**
	 * Returns UnitSymbol
	 */
	public String getUnitSymbol()
	{
		return _unitSymbol;
	}

	/**
	 * Sets Default
	 */
	public void setDefault(String def)
	{
		_default = def;
	}

	/**
	 * Returns Default
	 */
	public String getDefault()
	{
		return _default;
	}

	/**
	 * Sets MultipleValues
	 */
	public void setMultipleValues(boolean multipleValues)
	{
		_multipleValues = multipleValues;
	}

	/**
	 * Returns MultipleValues
	 */
	public boolean isMultipleValues()
	{
		return _multipleValues;
	}

	/**
	 * Sets NearestValue
	 */
	public void setNearestValue(boolean nearestValue)
	{
		_nearestValue = nearestValue;
	}

	/**
	 * Returns NearestValue
	 */
	public boolean isNearestValue()
	{
		return _nearestValue;
	}

	/**
	 * Sets Current
	 */
	public void setCurrent(boolean current)
	{
		_current = current;
	}

	/**
	 * Returns Current
	 */
	public boolean isCurrent()
	{
		return _current;
	}
}

