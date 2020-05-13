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

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.google.common.net.UrlEscapers;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;


//=============================================================================

class WebDavRemoteFile implements RemoteFile {
    private final Sardine sardine;

    private String path;
    private ISODate changeDate;


    public WebDavRemoteFile(Sardine sardine, String baseURL, DavResource davResource) {
        this.sardine = sardine;
        path = baseURL + UrlEscapers.urlFragmentEscaper().escape(davResource.getPath());
        Date modifiedDate = davResource.getModified();
        if (modifiedDate == null) {
            modifiedDate = Calendar.getInstance().getTime();
        }
        changeDate = new ISODate(modifiedDate.getTime(), false);
    }

    //---------------------------------------------------------------------------
    //---
    //--- RemoteFile interface
    //---
    //---------------------------------------------------------------------------

    public String getPath() {
        return path;
    }

    public ISODate getChangeDate() {
        return changeDate;
    }

    //---------------------------------------------------------------------------

    public Element getMetadata(SchemaManager schemaMan) throws Exception {
        InputStream in = null;
        try {
            in = sardine.get(path);
            return Xml.loadStream(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    //---------------------------------------------------------------------------

    public boolean isMoreRecentThan(String localChangeDate) {
        ISODate remoteDate = changeDate;
        ISODate localDate = new ISODate(localChangeDate);
        //--- accept if remote date is greater than local date
        return (remoteDate.timeDifferenceInSeconds(localDate) > 0);
    }
}

//=============================================================================
