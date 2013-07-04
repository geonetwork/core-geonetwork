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

package org.fao.geonet.kernel.harvest.harvester.webdav;

import jeeves.utils.Xml;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;


//=============================================================================

class WebDavRemoteFile implements RemoteFile {
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public WebDavRemoteFile(WebdavResource wr) {
		this.wr = wr;
		path       = wr.getPath();
		changeDate = new ISODate(wr.getGetLastModified()).toString();
	}

	//---------------------------------------------------------------------------
	//---
	//--- RemoteFile interface
	//---
	//---------------------------------------------------------------------------

	public String getPath()       { return path;       }
	public String getChangeDate() { return changeDate; }

	//---------------------------------------------------------------------------

	public Element getMetadata(SchemaManager  schemaMan) throws Exception {
		try {
			wr.setPath(path);
            return Xml.loadStream(wr.getMethodData());
		}
		catch (HttpException x) {
			throw new Exception("HTTPException : " + x.getMessage());
		}
	}

	//---------------------------------------------------------------------------

	public boolean isMoreRecentThan(String localChangeDate) {
		ISODate remoteDate = new ISODate(changeDate);
		ISODate localDate  = new ISODate(localChangeDate);
		//--- accept if remote date is greater than local date
		return (remoteDate.sub(localDate) > 0);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String path;
	private String changeDate;

	private WebdavResource wr;
}

//=============================================================================