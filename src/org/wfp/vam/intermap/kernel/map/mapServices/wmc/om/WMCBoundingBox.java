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
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.om;

import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSBaseBoundingBox;

/**
 * @author ETj
 */
public class WMCBoundingBox implements WMSBaseBoundingBox
{
	private String _srs = null;
	private double _minx = Double.NaN;
	private double _miny = Double.NaN;
	private double _maxx = Double.NaN;
	private double _maxy = Double.NaN;

	private WMCBoundingBox()
	{
	}

	public static WMCBoundingBox newInstance()
	{
		return new WMCBoundingBox();
	}


	public void setSRS(String srs)
	{
		_srs = srs;
	}

	public String getSRS()
	{
		return _srs;
	}


	public void setMinx(double minx)
	{
		_minx = minx;
	}

	public double getMinx()
	{
		return _minx;
	}


	public void setMiny(double miny)
	{
		_miny = miny;
	}

	public double getMiny()
	{
		return _miny;
	}


	public void setMaxx(double maxx)
	{
		_maxx = maxx;
	}

	public double getMaxx()
	{
		return _maxx;
	}


	public void setMaxy(double maxy)
	{
		_maxy = maxy;
	}

	public double getMaxy()
	{
		return _maxy;
	}

}
