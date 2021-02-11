//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.util.Sha1Encoder;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class WAFRemoteFile implements RemoteFile {

    private static final String OUTPUT_SCHEMA = "iso19139";
    private final CloseableHttpClient httpClient;
    private final String path;
    private ISODate changeDate;

    public WAFRemoteFile(String path, CloseableHttpClient httpClient) {
        this.path = path;
        this.httpClient = httpClient;
    }

    /**
     * Get Metadata from GetCapabilities file (Service).
     *
     * @param schemaMan
     * @return
     * @throws Exception
     */
    public Element getMetadata(SchemaManager schemaMan) throws Exception {
        String type = WAFRetriever.getFileType(this.path);
        if (WAFRetriever.type_GetCapabilities.equals(type)) {
            return getMdFromService(path, schemaMan);
        } else if (WAFRetriever.type_xml.equals(type)) {
            return getXmlFromUrl(new URL(path));
        } else {
            return null;
        }
    }

    private Element getXmlFromUrl(URL url) throws IOException, JDOMException {
        HttpGet request = new HttpGet(url.toString());
        Element result;
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() / 100 == 2) {// 2xx codes (integer division)
                HttpEntity entity = response.getEntity();
                try (InputStream responseInputStream = entity.getContent()) {
                    result = Xml.loadStream(responseInputStream);
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
            } else {
                EntityUtils.consumeQuietly(response.getEntity());
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }

        }
        return result;
    }

    private Element getMdFromService(String url, SchemaManager schemaMan) throws Exception {
        Element el;
        Path styleSheet = getStyleSheet(url, schemaMan);
        if (styleSheet == null) {
            return null;
        }

        Element xml = getXmlFromUrl(new URL(url));

        // md5 the full capabilities URL
        String uuid = Sha1Encoder.encodeString(url); // is the service identifier

        Map<String, Object> param = new HashMap<>();
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
        return schemaMan.getSchemaDir(OUTPUT_SCHEMA).
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

