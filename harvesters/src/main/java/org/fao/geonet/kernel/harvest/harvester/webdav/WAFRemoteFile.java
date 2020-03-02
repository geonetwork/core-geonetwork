//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.util.Sha1Encoder;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

//=============================================================================

class WAFRemoteFile implements RemoteFile {

    private static String outputSchema = "iso19139";

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------
    private String path;
    private ISODate changeDate;

    //---------------------------------------------------------------------------
    //---
    //--- RemoteFile interface
    //---
    //---------------------------------------------------------------------------

    public WAFRemoteFile(String path) {
        this.path = path;
    }


    //---------------------------------------------------------------------------
    //---
    //--- Get Metadata from GetCapabilities file (Service)
    //---
    //---------------------------------------------------------------------------

    public Element getMetadata(SchemaManager schemaMan) throws Exception {
        String type = WAFRetriever.getFileType(this.path);
        if (type.equals(WAFRetriever.type_GetCapabilities))
            return getMdFromService(path, schemaMan);
        else if (type.equals(WAFRetriever.type_xml))
            return Xml.loadFile(new URL(path));
        else
            return null;
    }

    private Element getMdFromService(String url, SchemaManager schemaMan) throws Exception {
        Element el = null;
        Path styleSheet = getStyleSheet(url, schemaMan);
        if (styleSheet == null) {
            return null;
        }
        Element xml = Xml.loadFile(new URL(url));

        // md5 the full capabilities URL
        String uuid = Sha1Encoder.encodeString(url); // is the service identifier

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("uuid", uuid);

        el = Xml.transform(xml, styleSheet, param);
        return el;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Get Service Type
    //---
    //---------------------------------------------------------------------------

    private Path getStyleSheet(String url, SchemaManager schemaMan) {
        String serviceType = getServiceType(url);
        if (serviceType == null)
            return null;
        return schemaMan.getSchemaDir(outputSchema).
            resolve(Geonet.Path.CONVERT_STYLESHEETS).
            resolve("OGCWxSGetCapabilitiesto19119").
            resolve("OGC" + serviceType + "GetCapabilities-to-ISO19119_ISO19139.xsl");
    }

    //---------------------------------------------------------------------------

    private String getServiceType(String url) {
        if (url.toUpperCase().contains(WXS.WMS))
            return "WMS";
        else if (url.toUpperCase().contains(WXS.WCS))
            return "WCS";
        else if (url.toUpperCase().contains(WXS.WFS))
            return "WFS";
        else if (url.toUpperCase().contains(WXS.WPS))
            return "WPS";
        else
            return null;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public boolean isMoreRecentThan(String localChangeDate) {
        ISODate remoteDate = changeDate;
        if (remoteDate == null) {
            return true;
        }
        ISODate localDate = new ISODate(localChangeDate);
        //--- accept if remote date is greater than local date
        return (remoteDate.timeDifferenceInSeconds(localDate) > 0);
    }

    public String getPath() {
        return path;
    }

    public ISODate getChangeDate() {
        return changeDate;
    }
    public void setChangeDate(String date) {
        try {
            changeDate = new ISODate(ISODate.parseBasicOrFullDateTime(date).getMillis());
        } catch (Exception e) {
            changeDate = null;
        }
    }

    public static class WXS {
        public static final String WMS = "SERVICE=WMS";
        public static final String WCS = "SERVICE=WCS";
        public static final String WFS = "SERVICE=WFS";
        public static final String WPS = "SERVICE=WPS";
    }

}

//=============================================================================
