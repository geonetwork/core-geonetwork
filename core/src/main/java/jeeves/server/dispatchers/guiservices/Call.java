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

package jeeves.server.dispatchers.guiservices;

import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.jdom.Element;

import java.nio.file.Path;


//=============================================================================

public class Call implements GuiService {
    private String name;
    private Service serviceObj;

    //---------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public Call(Element config, String pack, Path appPath) throws Exception {
        name = Util.getAttrib(config, ConfigFile.Call.Attr.NAME);

        //--- handle 'class' attrib

        String clas = Util.getAttrib(config, ConfigFile.Call.Attr.CLASS);

        if (clas.startsWith("."))
            clas = pack + clas;

        //--- let everyone else know that this is a guiservice
        Element guiService = new Element("param");
        guiService.setAttribute(Jeeves.Attr.NAME, Jeeves.Text.GUI_SERVICE);
        guiService.setAttribute(Jeeves.Attr.VALUE, "yes");
        config.addContent(guiService);

        serviceObj = (Service) Class.forName(clas).newInstance();
        serviceObj.init(appPath, new ServiceConfig(config.getChildren()));
    }

    //---------------------------------------------------------------------------
    //---
    //--- Exec
    //---
    //--------------------------------------------------------------------------

    public Element exec(final Element response, final ServiceContext context) throws Exception {
        //--- invoke the method and obtain a jdom result

        Element finalResponse = serviceObj.exec(response, context);

        if (finalResponse != null) {
            finalResponse.setName(name);
        }

        return finalResponse;
    }
}

//=============================================================================

