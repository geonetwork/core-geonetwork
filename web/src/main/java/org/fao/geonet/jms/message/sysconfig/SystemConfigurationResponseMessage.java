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
package org.fao.geonet.jms.message.sysconfig;

import org.fao.geonet.jms.message.Message;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;

/**
 * @author heikki doeleman
 */
public class SystemConfigurationResponseMessage extends Message {
    
    private String info;
    private String senderClientID;
    private String recipientClientID;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSenderClientID() {
        return senderClientID;
    }

    public void setSenderClientID(String senderClientID) {
        this.senderClientID = senderClientID;
    }

    public String getRecipientClientID() {
        return recipientClientID;
    }

    public void setRecipientClientID(String recipientClientID) {
        this.recipientClientID = recipientClientID;
    }

    public SystemConfigurationResponseMessage decode(String xml) {
        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
        SystemConfigurationResponseMessage message = (SystemConfigurationResponseMessage)decoder.readObject();
        decoder.close();
        return message;
    }
}