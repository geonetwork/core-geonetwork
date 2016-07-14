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

package jeeves.server.dispatchers;

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.GuiService;

import org.fao.geonet.Util;
import org.jdom.Element;

import java.util.List;
import java.util.Vector;

//=============================================================================

public abstract class AbstractPage {
    private String sheet;
    private String contentType;
    private String testCondition;

    private Vector<GuiService> vGuiServ = new Vector<GuiService>();

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    /**
     * Gets the stylesheet associated to this output page
     */

    public String getStyleSheet() {
        return sheet;
    }

    //--------------------------------------------------------------------------

    public void setStyleSheet(String s) {
        sheet = s;
    }

    //--------------------------------------------------------------------------

    public String getContentType() {
        return contentType;
    }

    //--------------------------------------------------------------------------

    public void setContentType(String type) {
        contentType = type;
    }

    //--------------------------------------------------------------------------

    /**
     * Return the xsl match condition
     */

    public String getTestCondition() {
        return testCondition;
    }

    //--------------------------------------------------------------------------

    public void setTestCondition(String c) {
        testCondition = c;
    }

    //--------------------------------------------------------------------------

    public void addGuiService(GuiService s) {
        vGuiServ.add(s);
    }

    //---------------------------------------------------------------------------

    /**
     * Invokes all gui services of this page (/root/gui tag)
     */

    public Element invokeGuiServices(ServiceContext context, Element response, List<GuiService> defaults) {
        Element root = new Element(Jeeves.Elem.GUI);

        //--- invoke default elements

        invokeGuiService(context, response, root, defaults);
        invokeGuiService(context, response, root, vGuiServ);

        return root;
    }

    //---------------------------------------------------------------------------

    private void invokeGuiService(ServiceContext context, Element response, Element root, List<GuiService> guiServices) {
        for (GuiService guiSrv : guiServices) {
            try {
                Element elGui = guiSrv.exec(response, context);

                if (elGui != null) {
                    root.addContent(elGui);
                }
            } catch (Exception e) {
                ServiceManager.error("Exception executing gui service : "
                    + e.toString());
                ServiceManager.error(" (C) Stack trace is :\n"
                    + Util.getStackTrace(e));
            }
        }
    }
}

//=============================================================================


