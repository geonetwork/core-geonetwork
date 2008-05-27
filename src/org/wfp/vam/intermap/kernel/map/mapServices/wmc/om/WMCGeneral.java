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
public class WMCGeneral 
{
	private WMCWindow _window = null; // 0..1
	private WMCBoundingBox _boundingBox = null; // 1..1
	private String _title = null; // 1..1
//	private WMCKeywordList _keywordList = null; // TODO
	private String _abstract = null;
//	private WMCUrl _logoURL = null; // TODO
//	private WMCUrl _descriptionURL = null; // TODO
//	private WMCContactInformation _contactInformation = null; // TODO
	private WMCExtension _extension = null;


	private WMCGeneral()
	{
	}

	/**
	 * Method newInstance
	 */
	public static WMCGeneral newInstance()
	{
		return new WMCGeneral();
	}


	/***************************************************************************
	 * Window
	 */
	public WMCWindow addNewWindow()
	{
		if(_window != null)
			throw new IllegalStateException("A Window element already exists");

		_window = WMCWindow.newInstance();

		return _window;
	}

	/**
	 * Sets Window
	 */
	public void setWindow(WMCWindow window)
	{
		_window = window;
	}

	/**
	 * Returns Window
	 */
	public WMCWindow getWindow()
	{
		return _window;
	}


	/**
	 * Sets BoundingBox
	 */
	public void setBoundingBox(WMCBoundingBox boundingBox)
	{
		_boundingBox = boundingBox;
	}

	/**
	 * Returns BoundingBox
	 */
	public WMCBoundingBox getBoundingBox()
	{
		return _boundingBox;
	}


	/***************************************************************************
	 * Title
	 */
	public void setTitle(String title)
	{
		if(_title != null)
			throw new IllegalStateException("A Title element already exists");

		_title = title;
	}

	/**
	 * Returns Title
	 */
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

	/**
	 * Returns Abstract
	 */
	public String getAbstract()
	{
		return _abstract;
	}

	/**
	 * Sets Extension
	 */
	public void setExtension(WMCExtension extension)
	{
		_extension = extension;
	}

	/**
	 * Returns Extension
	 */
	public WMCExtension getExtension()
	{
		return _extension;
	}

}

