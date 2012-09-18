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

package org.fao.geonet.jms.message.thesaurus;

import org.fao.geonet.jms.message.Message;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;

/**
 * @author jose garcia
 */
public class AddThesaurusMessage extends Message {

    private String fname;
    private String type;
    private String dir;
    private String thesaurusFile;
    private String originatingClientID;

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
    public String getThesaurusFile() {
        return thesaurusFile;
    }

    public void setThesaurusFile(String thesaurusFile) {
        this.thesaurusFile = thesaurusFile;
    }

    public String getOriginatingClientID() {
        return originatingClientID;
    }

    public void setOriginatingClientID(String originatingClientID) {
        this.originatingClientID = originatingClientID;
    }

    public AddThesaurusMessage decode(String xml) {
        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
        AddThesaurusMessage updateThesaurusMessage = (AddThesaurusMessage)decoder.readObject();
        decoder.close();
        return updateThesaurusMessage;
    }
}