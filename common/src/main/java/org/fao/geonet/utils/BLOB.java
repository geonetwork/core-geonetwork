//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.utils;

import org.apache.commons.codec.binary.Base64;
import org.fao.geonet.Constants;
import org.jdom.Element;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

//=============================================================================

/**
 * class to encode/decode blobs to base64 strings
 */

public final class BLOB {
    private static final int BUF_SIZE = 8192;

    /**
     * Default constructor. Builds a BLOB.
     */
    private BLOB() {
    }

    public static Element encode(int responseCode, byte blob[], String contentType, String filename) {
        Element response = new Element("response");
        response.setAttribute("responseCode", responseCode + "");
        response.setAttribute("contentType", contentType);
        response.setAttribute("contentLength", blob.length + "");
        if (filename != null)
            response.setAttribute("contentDisposition", "attachment;filename=" + filename);
        //String data = new BASE64Encoder().encode(blob);
        String data = new String(new Base64().encode(blob), Charset.forName(Constants.ENCODING));
        response.setText(data);
        return response;
    }

    //---------------------------------------------------------------------------

    public static String getContentType(Element response) {
        return response.getAttributeValue("contentType");
    }

    //---------------------------------------------------------------------------

    public static String getContentLength(Element response) {
        return response.getAttributeValue("contentLength");
    }

    //---------------------------------------------------------------------------

    public static String getContentDisposition(Element response) {
        return response.getAttributeValue("contentDisposition");
    }

    //---------------------------------------------------------------------------

    public static int getResponseCode(Element response) {
        return Integer.parseInt(response.getAttributeValue("responseCode"));
    }

    //---------------------------------------------------------------------------

    public static void write(Element response, OutputStream output) throws IOException {
        String data = response.getText();

        //byte blob[] = new BASE64Decoder().decodeBuffer(data);
        byte blob[] = new Base64().decode(data.getBytes(Charset.forName(Constants.ENCODING)));
        ByteArrayInputStream input = new ByteArrayInputStream(blob);
        copy(input, output);
    }

    //-----------------------------------------------------------------------------
    // copies an input stream to an output stream

    private static void copy(InputStream in, OutputStream output) throws IOException {
        BufferedInputStream input = new BufferedInputStream(in);
        try {
            byte buffer[] = new byte[BUF_SIZE];
            int nRead;
            do {
                nRead = input.read(buffer, 0, BUF_SIZE);
                output.write(buffer, 0, nRead);

            } while (nRead == BUF_SIZE);
            input.close();
        } catch (IOException e) {
            input.close();
            throw e;
        }
    }
}

//=============================================================================

