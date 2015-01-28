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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class WAFRetriever implements RemoteRetriever {
    private AtomicBoolean cancelMonitor;
    //--------------------------------------------------------------------------
	//---
	//--- RemoteRetriever interface
	//---
	//--------------------------------------------------------------------------

	public void init(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, WebDavParams params) {
        this.cancelMonitor = cancelMonitor;
		this.log    = log;
		this.params = params;
	}

	//---------------------------------------------------------------------------

	public List<RemoteFile> retrieve() throws Exception {
		
		files.clear();
		retrieveFiles(params.url);
		return files;
	}

	//---------------------------------------------------------------------------

	public void destroy() {
		return;
	}

	//---------------------------------------------------------------------------

	private void retrieveFiles(String wafurl) throws IOException {

        if(log.isDebugEnabled()) log.debug("Scanning resource : "+ wafurl);
		
        Document doc = Jsoup.parse(new URL(wafurl),3000);
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            if (cancelMonitor.get()) {
                files.clear();
                return ;
            }

            String url = link.attr("abs:href");


            String fileType = getFileType(url);

            if(fileType == null) {
                continue;
            } else if (fileType.equals(type_dir)) {
                if (params.recurse) {
                    // Parent directory or same directory links, ignore
                    if (!url.contains(wafurl) || url.equals(wafurl)) continue;

                    // Try as a directory
                    retrieveFiles(url);
                }
            } else {
                files.add(new WAFRemoteFile(url));
            }
        }
	}
	
	//---------------------------------------------------------------------------
	//---
	//--- check type if url is a xml file or GetCapabilities file
	//---
	//---------------------------------------------------------------------------
	
	public static String getFileType(String path)
	{
		if(path.toUpperCase().contains("REQUEST=GETCAPABILITIES")) {
			return type_GetCapabilities;
        } else if(path.toUpperCase().endsWith(".XML")) {
			return	type_xml;
        } else if(path.toUpperCase().endsWith("/")) {
            return type_dir;
        } else {
			return null;
        }
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private WebDavParams   params;
	private List<RemoteFile> files = new ArrayList<RemoteFile>();
	
	public static final String type_GetCapabilities = "GetCapabilities";
	public static final String type_xml = "xml";
    public static final String type_dir = "directory";
}

//=============================================================================
