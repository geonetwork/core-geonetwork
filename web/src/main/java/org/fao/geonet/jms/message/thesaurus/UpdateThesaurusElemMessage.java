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
package org.fao.geonet.jms.message.thesaurus;

import org.fao.geonet.jms.message.Message;
import org.fao.geonet.kernel.KeywordBean;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;

/**
 * @author jose garcia
 */
public class UpdateThesaurusElemMessage extends Message {

    private String thesaurusName;
    private KeywordBean keyword;
    private boolean replace;
    private String oldId;
    private String newId;
    private String namespace;
    private String originatingClientID;

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    public String getNewId() {
        return newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }

    public KeywordBean getKeyword() {
        return keyword;
    }

    public void setKeyword(KeywordBean keyword) {
        this.keyword = keyword;
    }


    public String getThesaurusName() {
        return thesaurusName;
    }

    public void setThesaurusName(String thesaurusName) {
        this.thesaurusName = thesaurusName;
    }

    public String getOriginatingClientID() {
        return originatingClientID;
    }

    public void setOriginatingClientID(String originatingClientID) {
        this.originatingClientID = originatingClientID;
    }

    public UpdateThesaurusElemMessage decode(String xml) {
        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
        UpdateThesaurusElemMessage updateThesaurusElemMessage = (UpdateThesaurusElemMessage)decoder.readObject();
        decoder.close();
        return updateThesaurusElemMessage;
    }
}