//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.utils.BinaryFile;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Allows a user load a file from the identified formatter bundle. Typically used for reading images
 * or the bundle editor UI.
 * 
 * @author jeichar
 */
public class Resource extends AbstractFormatService {

    public Element exec(Element params, ServiceContext context) throws Exception {
        ensureInitializedDir(context);
        String xslid = Util.getParam(params, Params.ID);
        String fileName = Util.getParam(params, Params.FNAME);

        Path formatDir = getAndVerifyFormatDir(Params.ID, xslid);
        Path desiredFile = formatDir.resolve(fileName);
        
        if(!containsFile(formatDir, desiredFile)) {
            throw new BadParameterEx(Params.FNAME, fileName+" does not identify a file in the "+xslid+" format bundle");
        }
        if(!Files.isRegularFile(desiredFile)) {
            throw new BadParameterEx(Params.FNAME, fileName+" does not identify a file");
        }
        
        return BinaryFile.encode(200, desiredFile, fileName, false).getElement();
    }
}