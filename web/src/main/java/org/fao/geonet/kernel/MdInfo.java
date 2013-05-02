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

package org.fao.geonet.kernel;

import org.apache.lucene.document.Document;
import org.jdom.Element;

//=============================================================================

public class MdInfo
{
	public MdInfo(String id, Element record) {

        this.id          = id;
        uuid        = record.getChildText("uuid");
        schemaId    = record.getChildText("schemaid");
        isHarvested = "y".equals(record.getChildText("isharvested"));
        createDate  = record.getChildText("createdate");
        changeDate  = record.getChildText("changedate");
        source      = record.getChildText("source");
        title       = record.getChildText("title");
        root        = record.getChildText("root");
        owner       = record.getChildText("owner");
        groupOwner  = record.getChildText("groupowner");
        displayOrder  = record.getChildText("displayOrder");

        String temp = record.getChildText("istemplate");

        if ("y".equals(temp))
            template = MdInfo.Template.TEMPLATE;

        else if ("s".equals(temp))
            template = MdInfo.Template.SUBTEMPLATE;

        else
            template = MdInfo.Template.METADATA;

    }

    public MdInfo(Document doc) {
        id           = doc.get("_id");
        uuid         = doc.get("_uuid");
        schemaId     = doc.get("_schema");
        String isTemplate   = doc.get("_isTemplate");
        if (isTemplate.equals("y")) {
            template = MdInfo.Template.TEMPLATE;
        }
        else if (isTemplate.equals("s")) {
            template = MdInfo.Template.SUBTEMPLATE;
        }
        else {
            template = MdInfo.Template.METADATA;
        }
        String tmpIsHarvest  = doc.get("_isHarvested");
        if (tmpIsHarvest != null) {
            isHarvested  = doc.get("_isHarvested").equals("y");
        }
        else {
            isHarvested  = false;
        }
        createDate   = doc.get("_createDate");
        changeDate   = doc.get("_changeDate");
        source       = doc.get("_source");
        title        = doc.get("_title");
        root         = doc.get("_root");
        owner        = doc.get("_owner");
        groupOwner   = doc.get("_groupOwner");
        displayOrder   = doc.get("_displayOrder");
    }

    public final String   id;
	public final String   uuid;
	public final String   schemaId;
	public final Template template;
	public final boolean  isHarvested;
	public final String   createDate;
	public final String   changeDate;
	public final String   source;
	public final String   title;
	public final String   root;
	public final String   owner;
	public final String   groupOwner;
    public final String displayOrder;

	//--------------------------------------------------------------------------

	public enum Template { METADATA, TEMPLATE, SUBTEMPLATE}
}

//=============================================================================

