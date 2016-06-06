/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

//=============================================================================

package jeeves.services.http;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Constants;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.List;

//=============================================================================

/**
 * Returns a specific record given its id
 */

public class Get implements Service {
    public static final String URL_PARAM_NAME = "url";

    private String configUrl;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        configUrl = params.getValue(URL_PARAM_NAME);
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public Element exec(Element params, ServiceContext context) throws Exception {
        // read url
        String sUrl = params.getChildText(URL_PARAM_NAME);
        if (sUrl == null) sUrl = configUrl;
        if (sUrl == null)
            throw new IllegalArgumentException("The '" + URL_PARAM_NAME + "' configuration parameter is missing");

        // add other parameters to HTTP request
        boolean first = new URL(sUrl).getQuery() == null;
        StringBuffer sb = new StringBuffer(sUrl);
        for (Element child : (List<Element>) params.getChildren()) {
            // skip the url parameter
            if (child.getName().equals(URL_PARAM_NAME)) continue;

            if (first) {
                first = false;
                sb.append('?');
            } else {
                sb.append('&');
            }
            sb.append(child.getName()).append('=').append(URLEncoder.encode(child.getText(), Constants.ENCODING));
        }
        URL url = new URL(sb.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream input = conn.getInputStream();

        return Xml.loadStream(input);

		/* FIXME: use this instead of previous line to dump HTTP response body
        ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte buffer[] = new byte[BUFSIZE];
		for (int nRead; (nRead = input.read(buffer, 0, BUFSIZE)) > 0; )
			output.write(buffer, 0, nRead);
		output.close();
		conn.disconnect();
		String sResult = output.toString();
		System.out.println("HTTP response body:\n" + sResult);
		Element result = Xml.loadString(sResult, false); // do not validate
		return result;
		*/
    }
}

