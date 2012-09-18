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
package org.fao.geonet.jms.message.reindex;

import org.fao.geonet.jms.message.Message;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;

/**
 * @author heikki doeleman
 */
public class ReIndexMessage extends Message {
    
    private String id;
    private String indexGroup;
    private String senderClientID;
    private boolean workspace;
    private boolean deleteMetadata = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isWorkspace() {
        return workspace;
    }

    public void setWorkspace(boolean workspace) {
        this.workspace = workspace;
    }

    public String getIndexGroup() {
        return indexGroup;
    }

    public void setIndexGroup(String indexGroup) {
        this.indexGroup = indexGroup;
    }

    public boolean isDeleteMetadata() {
        return deleteMetadata;
    }

    public void setDeleteMetadata(boolean deleteMetadata) {
        this.deleteMetadata = deleteMetadata;
    }


    public String getSenderClientID() {
        return senderClientID;
    }

    public void setSenderClientID(String senderClientID) {
        this.senderClientID = senderClientID;
    }

    public ReIndexMessage decode(String xml) {
        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
        ReIndexMessage reIndexMessage = (ReIndexMessage)decoder.readObject();
        decoder.close();
        return reIndexMessage;
    }
}