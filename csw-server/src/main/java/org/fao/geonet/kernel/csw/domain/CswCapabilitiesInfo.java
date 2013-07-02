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

package org.fao.geonet.kernel.csw.domain;

import java.util.List;

import jeeves.resources.dbms.Dbms;

import org.jdom.Element;

public class CswCapabilitiesInfo {
    private String title;
    private String abstractz;
    private String fees;
    private String accessContraints;

    private String langId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstract() {
        return abstractz;
    }

    public void setAbstract(String abstractz) {
        this.abstractz = abstractz;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getAccessConstraints() {
        return accessContraints;
    }

    public void setAccessConstraints(String accessContraints) {
        this.accessContraints = accessContraints;
    }

    public String getLangId() {
        return langId;
    }

    public void setLangId(String langId) {
        this.langId = langId;
    }

    /**
    *
    * @param dbms
    * @param language
    * @return
    * @throws Exception
    */
   public static CswCapabilitiesInfo getCswCapabilitiesInfo(Dbms dbms, String language) throws Exception {

       CswCapabilitiesInfo cswCapabilitiesInfo = new CswCapabilitiesInfo();
       cswCapabilitiesInfo.setLangId(language);
       Element capabilitiesInfoRecord = dbms.select("SELECT * FROM CswServerCapabilitiesInfo WHERE langId = ?", language);

       @SuppressWarnings("unchecked")
       List<Element> records = capabilitiesInfoRecord.getChildren();
       for(Element record : records) {
           String field = record.getChild("field").getText();
           String label = record.getChild("label").getText();

           if (field.equals("title")) {
               cswCapabilitiesInfo.setTitle(label);
           }
           else if (field.equals("abstract")) {
               cswCapabilitiesInfo.setAbstract(label);
           }
           else if (field.equals("fees")) {
               cswCapabilitiesInfo.setFees(label);
           }
           else if (field.equals("accessConstraints")) {
               cswCapabilitiesInfo.setAccessConstraints(label);
           }
       }
       return cswCapabilitiesInfo;
   }

   /**
    *
    * @param dbms
    * @return
    * @throws Exception
    */
   public static Element getCswCapabilitiesInfo(Dbms dbms) throws Exception {
       return dbms.select("SELECT * FROM CswServerCapabilitiesInfo");
   }

   /**
    *
    * @param dbms
    * @param cswCapabilitiesInfo
    * @throws Exception
    */
   public static void saveCswCapabilitiesInfo(Dbms dbms, CswCapabilitiesInfo cswCapabilitiesInfo)
           throws Exception {

       String langId = cswCapabilitiesInfo.getLangId();

       dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getTitle(), langId, "title");
       dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getAbstract(), langId, "abstract");
       dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getFees(), langId, "fees");
       dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?",  cswCapabilitiesInfo.getAccessConstraints(), langId, "accessConstraints");
   }

}
