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

package org.fao.geonet.kernel.mef;

import org.fao.geonet.exceptions.BadFormatEx;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Import an XML file and create a temporary information file before passing the information to the
 * MEF visitor.
 */
public class XmlVisitor implements IVisitor {

    public void visit(Path xmlFile, IMEFVisitor v) throws Exception {
        handleXml(xmlFile, v);
    }

    /**
     * Load an XML file and pass it to a MEF visitor.
     */
    public Element handleXml(Path xmlFile, IMEFVisitor v) throws Exception {

        Element md;
        md = Xml.loadFile(xmlFile);
        if (md == null)
            throw new BadFormatEx("Missing xml metadata file .");

        v.handleMetadata(md, 0);

        // Generate dummy info file.
        Element info;
        info = new Element("info");
        v.handleInfo(info, 0);
        return info;
    }

    public void handleBin(Path mefFile, IMEFVisitor v, Element info, int index)
        throws Exception {
    }
}
