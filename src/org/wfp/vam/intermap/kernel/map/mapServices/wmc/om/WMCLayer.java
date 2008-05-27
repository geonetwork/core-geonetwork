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

/**
 * @author ETj
 */
public class WMCLayer 
{
	private boolean _queryable;
	private boolean _hidden;

	private WMCServer _server = null; // 1..1
	private String _name = null; // 1..1
	private String _title = null; // 1..1
	private String _abstract = null;
//	private WMCurl _dataUrl = null;
//	private WMCurl _metadataUrl = null;
	private String _SRS = null;
//	private WMCDimensionList;
	private WMCFormatList _formatList = null; // 0..1
	private WMCStyleList _styleList = null; // 0..1
//	private SLDMinScaleDenominator;
//	private SLDMaxScaleDenominator;
	private WMCExtension _extension = null;


	private WMCLayer()
	{}

	public static WMCLayer newInstance()
	{
		return new WMCLayer();
	}

	/***************************************************************************
	 * Server
	 */
	public void setServer(WMCServer server)
	{
		_server = server;
	}

	public WMCServer getServer()
	{
		return _server;
	}

	/***************************************************************************
	 * Name
	 */
	public void setName(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}

	/***************************************************************************
	 * Title
	 */
	public void setTitle(String title)
	{
		_title = title;
	}

	public String getTitle()
	{
		return _title;
	}

	/***************************************************************************
	 * Abstract
	 */
	public void setAbstract(String abs)
	{
		_abstract = abs;
	}

	public String getAbstract()
	{
		return _abstract;
	}

	/***************************************************************************
	 * SRS
	 */
	public void setSRS(String sRS)
	{
		_SRS = sRS;
	}

	public String getSRS()
	{
		return _SRS;
	}

	/***************************************************************************
	 * Queryable
	 */
	public void setQueryable(boolean queryable)
	{
		_queryable = queryable;
	}

	public boolean isQueryable()
	{
		return _queryable;
	}

	/***************************************************************************
	 * Hidden
	 */
	public void setHidden(boolean hidden)
	{
		_hidden = hidden;
	}

	public boolean isHidden()
	{
		return _hidden;
	}

	/***************************************************************************
	 * FormatList
	 */
	public void setFormatList(WMCFormatList formatList) {
		_formatList = formatList;
	}

	public WMCFormatList getFormatList() {
		return _formatList;
	}

	/***************************************************************************
	 * StyleList
	 */
	public void setStyleList(WMCStyleList styleList) {
		_styleList = styleList;
	}

	public WMCStyleList getStyleList() {
		return _styleList;
	}
	
	/***************************************************************************
	 * Extension
	 */
	public void setExtension(WMCExtension extension)
	{
		_extension = extension;
	}
	
	public WMCExtension getExtension()
	{
		return _extension;
	}

}
