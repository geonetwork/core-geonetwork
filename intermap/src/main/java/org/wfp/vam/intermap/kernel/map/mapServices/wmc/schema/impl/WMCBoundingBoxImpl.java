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

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl;

import org.jdom.Element;
import org.jdom.Namespace;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCBoundingBox;

/**
 * @author ETj
 */
public class WMCBoundingBoxImpl implements WMCBoundingBox
{
	private String _srs = null;
	private float _minx = Float.NaN;
	private float _miny = Float.NaN;
	private float _maxx = Float.NaN;
	private float _maxy = Float.NaN;

	private WMCBoundingBoxImpl()
	{
	}

	public static WMCBoundingBox newInstance()
	{
		return new WMCBoundingBoxImpl();
	}

	public static WMCBoundingBox parse(Element ebb)
	{
		WMCBoundingBoxImpl bb = new WMCBoundingBoxImpl();

		bb.setSRS(ebb.getAttributeValue("srs"));
		bb.setMinx(Float.parseFloat(ebb.getAttributeValue("minx")));
		bb.setMiny(Float.parseFloat(ebb.getAttributeValue("miny")));
		bb.setMaxx(Float.parseFloat(ebb.getAttributeValue("maxx")));
		bb.setMaxy(Float.parseFloat(ebb.getAttributeValue("maxy")));

		return bb;
	}

	/**
	 * Sets Srs
	 */
	public void setSRS(String srs)
	{
		_srs = srs;
	}

	/**
	 * Returns Srs
	 */
	public String getSRS()
	{
		return _srs;
	}

	/**
	 * Sets Minx
	 */
	public void setMinx(float minx)
	{
		_minx = minx;
	}

	/**
	 * Returns Minx
	 */
	public float getMinx()
	{
		return _minx;
	}

	/**
	 * Sets Miny
	 */
	public void setMiny(float miny)
	{
		_miny = miny;
	}

	/**
	 * Returns Miny
	 */
	public float getMiny()
	{
		return _miny;
	}

	/**
	 * Sets Maxx
	 */
	public void setMaxx(float maxx)
	{
		_maxx = maxx;
	}

	/**
	 * Returns Maxx
	 */
	public float getMaxx()
	{
		return _maxx;
	}

	/**
	 * Sets Maxy
	 */
	public void setMaxy(float maxy)
	{
		_maxy = maxy;
	}

	/**
	 * Returns Maxy
	 */
	public float getMaxy()
	{
		return _maxy;
	}

	/**
	 * Method toElement
	 */
	public Element toElement(String name)
	{
		if(_srs == null)
			throw new IllegalStateException(name + "/SRS is missing");
		if(_minx == Float.NaN)
			throw new IllegalStateException(name + "/minx is missing");
		if(_miny == Float.NaN)
			throw new IllegalStateException(name + "/miny is missing");
		if(_maxx == Float.NaN)
			throw new IllegalStateException(name + "/maxx is missing");
		if(_maxy == Float.NaN)
			throw new IllegalStateException(name + "/maxy is missing");

        Namespace NS_WMC = Namespace.getNamespace("http://www.opengis.net/context");

		return new Element(name, NS_WMC)
			.setAttribute("SRS", _srs)
			.setAttribute("minx", ""+_minx)
			.setAttribute("miny", ""+_miny)
			.setAttribute("maxx", ""+_maxx)
			.setAttribute("maxy", ""+_maxy);
	}

}

