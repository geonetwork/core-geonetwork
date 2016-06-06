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

package org.fao.oaipmh.responses;

import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.domain.ISODate;
import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.requests.IdentifyRequest;
import org.jdom.Element;

//=============================================================================

public class IdentifyResponse extends AbstractResponse {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    private String reposName;

    //---------------------------------------------------------------------------
    private String baseURL;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    private ISODate earlDateStamp;
    private DeletedRecord deletedRecord;
    private Granularity granularity;
    private List<String> adminEmails = new ArrayList<String>();
    private List<String> compressions = new ArrayList<String>();

    //---------------------------------------------------------------------------
    private List<Element> descriptions = new ArrayList<Element>();

    //---------------------------------------------------------------------------

    public IdentifyResponse() {
    }

    //---------------------------------------------------------------------------

    public IdentifyResponse(Element response) {
        super(response);
        build(response);
    }

    //---------------------------------------------------------------------------

    public String getRepositoryName() {
        return reposName;
    }

    //---------------------------------------------------------------------------

    public void setRepositoryName(String name) {
        reposName = name;
    }

    //---------------------------------------------------------------------------

    public String getBaseUrl() {
        return baseURL;
    }

    //---------------------------------------------------------------------------

    public void setBaseUrl(String url) {
        baseURL = url;
    }

    //---------------------------------------------------------------------------

    public ISODate getEarliestDateStamp() {
        return earlDateStamp;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public void setEarliestDateStamp(ISODate dateStamp) {
        earlDateStamp = dateStamp;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public DeletedRecord getDeletedRecord() {
        return deletedRecord;
    }

    public void setDeletedRecord(DeletedRecord dr) {
        deletedRecord = dr;
    }

    public Granularity getGranularity() {
        return granularity;
    }

    public void setGranularity(Granularity g) {
        granularity = g;
    }

    public void clearAdminEmails() {
        adminEmails.clear();
    }

    public void addAdminEmail(String email) {
        adminEmails.add(email);
    }

    public Element toXml() {
        Element root = new Element(IdentifyRequest.VERB, OaiPmh.Namespaces.OAI_PMH);

        add(root, "repositoryName", reposName);
        add(root, "baseURL", baseURL);
        add(root, "protocolVersion", "2.0");

        for (String email : adminEmails)
            add(root, "adminEmail", email);

        add(root, "earliestDatestamp", earlDateStamp + "Z");
        add(root, "deletedRecord", deletedRecord.toString());
        add(root, "granularity", granularity.toString());

        for (String compression : compressions)
            add(root, "compression", compression);

        for (Element descr : descriptions)
            root.addContent((Element) descr.clone());

        return root;
    }

    private void build(Element response) {
        Element ident = response.getChild("Identify", OaiPmh.Namespaces.OAI_PMH);

        reposName = ident.getChildText("repositoryName", OaiPmh.Namespaces.OAI_PMH);
        baseURL = ident.getChildText("baseURL", OaiPmh.Namespaces.OAI_PMH);

        //--- store earliest datestamp

        String eds = ident.getChildText("earliestDatestamp", OaiPmh.Namespaces.OAI_PMH);
        earlDateStamp = new ISODate(eds);

        //--- add admin emails

        for (Object o : ident.getChildren("adminEmail", OaiPmh.Namespaces.OAI_PMH)) {
            Element email = (Element) o;
            adminEmails.add(email.getText());
        }

        //--- handle granularity

        String gran = ident.getChildText("granularity", OaiPmh.Namespaces.OAI_PMH);
        granularity = Granularity.parse(gran);

        //--- handle deleted record

        String delRec = ident.getChildText("deletedRecord", OaiPmh.Namespaces.OAI_PMH);
        deletedRecord = DeletedRecord.parse(delRec);

        //--- add compressions

        for (Object o : ident.getChildren("compression", OaiPmh.Namespaces.OAI_PMH)) {
            Element comp = (Element) o;
            compressions.add(comp.getText());
        }

        //--- add descriptions

        for (Object o : ident.getChildren("description", OaiPmh.Namespaces.OAI_PMH))
            descriptions.add((Element) o);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Enumerations
    //---
    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
    //--- Granularity
    //---------------------------------------------------------------------------

    public enum Granularity {
        SHORT("YYYY-MM-DD"), LONG("YYYY-MM-DDThh:mm:ssZ");

        //------------------------------------------------------------------------

        private String type;

        //------------------------------------------------------------------------

        private Granularity(String type) {
            this.type = type;
        }

        //------------------------------------------------------------------------

        public static Granularity parse(String type) {
            if (type.equals(SHORT.toString())) return SHORT;
            if (type.equals(LONG.toString())) return LONG;

            throw new RuntimeException("Unknown granularity type : " + type);
        }

        //------------------------------------------------------------------------

        public String toString() {
            return type;
        }
    }

    //---------------------------------------------------------------------------
    //--- DeletedRecord
    //---------------------------------------------------------------------------

    public enum DeletedRecord {
        NO("no"), PERSISTENT("persistent"), TRANSIENT("transient");

        //------------------------------------------------------------------------

        private String type;

        //------------------------------------------------------------------------

        private DeletedRecord(String type) {
            this.type = type;
        }

        //------------------------------------------------------------------------

        public static DeletedRecord parse(String type) {
            if (type.equals(NO.toString())) return NO;
            if (type.equals(PERSISTENT.toString())) return PERSISTENT;
            if (type.equals(TRANSIENT.toString())) return TRANSIENT;

            throw new RuntimeException("Unknown deleted record type : " + type);
        }

        //------------------------------------------------------------------------

        public String toString() {
            return type;
        }
    }
}

//=============================================================================

