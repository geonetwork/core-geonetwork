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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import org.fao.geonet.Constants;

//=============================================================================

/**
 * Simple class used to send an e-mail
 */

public class EMail {
    private static final int DEFAULT_PORT = 25;
    private String sFrom;
    private String sTo;
    private String sSubject;
    private String sBody;
    private String sMailServer;
    private int iPort;
    private String sLastError;
    private BufferedReader in;
    private OutputStreamWriter out;

    //--------------------------------------------------------------------------
    //---
    //--- Constructors
    //---
    //--------------------------------------------------------------------------

    public EMail(String mailServer) {
        setMailServer(mailServer, DEFAULT_PORT);
    }

    //--------------------------------------------------------------------------

    public EMail(String mailServer, int port) {
        setMailServer(mailServer, port);
    }

    //--------------------------------------------------------------------------
    //---
    //--- API
    //---
    //--------------------------------------------------------------------------

    /**
     * setup the mail server (and port) to which the e-mail will be sent
     */

    public void setMailServer(String mailServer, int port) {
        sMailServer = mailServer;
        iPort = port;
    }

    //--------------------------------------------------------------------------

    public void setFrom(String from) {
        sFrom = from;
    }

    //--------------------------------------------------------------------------

    public void setTo(String to) {
        sTo = to;
    }

    //--------------------------------------------------------------------------

    public void setSubject(String subject) {
        sSubject = subject;
    }

    //--------------------------------------------------------------------------

    public void setBody(String body) {
        sBody = body;
    }

    //--------------------------------------------------------------------------

    /**
     * Sends the message to the mail server
     */

    public boolean send() throws IOException {
        Socket socket = new Socket(sMailServer, iPort);
        try {
            in = new BufferedReader(new InputStreamReader(new DataInputStream(socket.getInputStream()), Charset.forName(Constants.ENCODING)));
            out = new OutputStreamWriter(new DataOutputStream(socket.getOutputStream()), "ISO-8859-1");

            if (lookMailServer())
                if (sendData("2", "HELO " + InetAddress.getLocalHost().getHostName() + "\r\n"))
                    if (sendData("2", "MAIL FROM: <" + sFrom + ">\r\n"))
                        if (sendData("2", "RCPT TO: <" + sTo + ">\r\n"))
                            if (sendData("354", "DATA\r\n"))
                                if (sendData("2", buildContent()))
                                    if (sendData("2", "QUIT\r\n"))
                                        return true;

            sendData("2", "QUIT\r\n");
        } finally {
            IOUtils.closeQuietly(socket);
        }
        return false;
    }

    //--------------------------------------------------------------------------

    public String getLastError() {
        return sLastError;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    private boolean lookMailServer() throws IOException {
        sLastError = in.readLine();
        if (sLastError != null) {
            return sLastError.startsWith("2");
        } else {
            return false;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Sends a string to the socket
     */

    private boolean sendData(String error, String data) throws IOException {
        out.write(data);
        out.flush();

        sLastError = in.readLine();
        if (sLastError != null) {
            return sLastError.startsWith(error);
        } else {
            return false;
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Builds the message putting all pieces together
     */

    private String buildContent() {
        return "Date: " + new Date().toString() + "\r\n" +
            "From: " + sFrom + "\r\n" +
            "Subject: " + sSubject + "\r\n" +
            "To: " + sTo + "\r\n" +
            "\r\n" +
            sBody + "\r\n" +
            "\r\n" +
            "." +
            "\r\n";
    }
}

//=============================================================================

