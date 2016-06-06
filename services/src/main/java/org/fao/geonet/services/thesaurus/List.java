//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.thesaurus;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;

//=============================================================================

/**
 * Given a metadata id returns all associated categories. Called by the metadata.category service
 */

public class List implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    private String _localThesaurusDir;
    private String _externalDir;
    private String _initType;
    /**
     * Filter on directory
     */
    private FilenameFilter directoryFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            if (dir.isDirectory() && !name.startsWith("."))
                return true;
            else
                return false;
        }
    };
    /**
     * Filter on directory
     */
    private FilenameFilter thesauriFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            if (dir.isDirectory() && !name.startsWith("."))
                return true;
            else if (!dir.isDirectory() && name.endsWith(".xml") || name.endsWith(".rdf"))
                return true;
            else return false;
        }
    };

    public void init(Path appPath, ServiceConfig params) throws Exception {

        _localThesaurusDir = File.separator + Geonet.CodeList.LOCAL + File.separator + Geonet.CodeList.THESAURUS + File.separator;
        _externalDir = File.separator + Geonet.CodeList.EXTERNAL + File.separator + Geonet.CodeList.THESAURUS + File.separator;

        _initType = params.getValue(Params.TYPE, "_none_");
    }


    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ThesaurusManager thesaurusMan = gc.getBean(ThesaurusManager.class);
        Path THESAURUS_DIR = thesaurusMan.getThesauriDirectory();

        Element thesauriList = new Element("thesaurusList");

        String type = Util.getParam(params, Params.TYPE, _initType);

        if (type.equals("all-directories")) {
            listThesauri(thesauriList, THESAURUS_DIR + _externalDir, 1, directoryFilter, Geonet.CodeList.EXTERNAL);
            listThesauri(thesauriList, THESAURUS_DIR + _localThesaurusDir, 1, directoryFilter, Geonet.CodeList.LOCAL);
        } else if (type.equals("upload-directories")) {
            listThesauri(thesauriList, THESAURUS_DIR + _externalDir, 1, directoryFilter, Geonet.CodeList.EXTERNAL);
        } else if (type.equals("all-thesauri")) {
            listThesauri(thesauriList, THESAURUS_DIR + _externalDir, 2, thesauriFilter, Geonet.CodeList.EXTERNAL);
            listThesauri(thesauriList, THESAURUS_DIR + _localThesaurusDir, 2, thesauriFilter, Geonet.CodeList.LOCAL);
        } else if (type.equals("update-thesauri")) {
            listThesauri(thesauriList, THESAURUS_DIR + _localThesaurusDir, 3, thesauriFilter, Geonet.CodeList.LOCAL);
        } else {
            listThesauri(thesauriList, THESAURUS_DIR + _externalDir, 3, thesauriFilter, Geonet.CodeList.EXTERNAL);
            listThesauri(thesauriList, THESAURUS_DIR + _localThesaurusDir, 3, thesauriFilter, Geonet.CodeList.LOCAL);
        }

        //-----------------------------------------------------------------------

        Element elRes = new Element(Jeeves.Elem.RESPONSE).addContent(thesauriList);

        //-----------------------------------------------------------------------

        String selected = Util.getParam(params, Params.SELECTED, "none");
        if (!selected.equals("none")) {
            Element elSelected = new Element("selectedThesaurus");
            elSelected.addContent(selected);
            elRes.addContent(elSelected);
        }
        String mode = Util.getParam(params, "mode", "none");
        if (!mode.equals("none")) {
            Element elMode = new Element("mode");
            elMode.addContent(mode);
            elRes.addContent(elMode);
        }
        //-----------------------------------------------------------------------

        return elRes;
    }


    /**
     * Browse directory tree and return thesaurus in xml and rdf format
     */
    private void listThesauri(Element list, String dir, int mode, FilenameFilter filter, String rootName) throws Exception {
        File thesauriDirectory = new File(dir);
        if (thesauriDirectory.isDirectory()) {

            File[] rdfDataDirectory = thesauriDirectory.listFiles(filter);
            for (int i = 0; i < rdfDataDirectory.length; i++) {


                if (rdfDataDirectory[i].isDirectory()) {

                    if (mode == 1) { // Simple tree
                        Element dirE = new Element("directory").setAttribute("label", rdfDataDirectory[i].getName());
                        dirE.setAttribute("type", rootName);
                        list.addContent(dirE);
                        listThesauri(dirE, rdfDataDirectory[i].getAbsolutePath(), mode, filter, rootName);
                    } else if (mode == 2) { // Complex tree local + external
                        Element dirE = new Element("directory").setAttribute("label", rdfDataDirectory[i].getName());
                        dirE.setAttribute("type", rootName);

                        // Search directory with same name
                        @SuppressWarnings("unchecked")
                        java.util.List<Element> children = list.getChildren("directory");
                        Element element = null;
                        for (Element e : children) {
                            if (e.getAttribute("label").getValue().equals(rdfDataDirectory[i].getName())) {
                                element = e;
                                break;
                            }
                        }

                        if (element != null) {
                            listThesauri(element, rdfDataDirectory[i].getAbsolutePath(), mode, filter, rootName);
                        } else {
                            list.addContent(dirE);
                            listThesauri(dirE, rdfDataDirectory[i].getAbsolutePath(), mode, filter, rootName);
                        }
                    } else // Flat tree
                        listThesauri(list, rdfDataDirectory[i].getAbsolutePath(), mode, filter, rootName);

                } else if (mode != 1) {    // File
                    String thesaurusName = rootName + '.' + thesauriDirectory.getName() + '.' + rdfDataDirectory[i].getName().substring(0, rdfDataDirectory[i].getName().indexOf(".rdf"));

                    Element thesaurusE = new Element("thesaurus").setAttribute("value", thesaurusName);
                    thesaurusE.setAttribute("type", rootName);
                    Element dirE = new Element("dname").setText(thesauriDirectory.getName());
                    Element fnameE = new Element("fname").setText(rdfDataDirectory[i].getName());
                    // Element dirNE = new Element("dir").setText(thesauriDirectory.getAbsolutePath());
                    thesaurusE.addContent(fnameE);
                    thesaurusE.addContent(dirE);
                    // thesaurusE.addContent(dirNE);
                    list.addContent(thesaurusE);
                }
            }
        }
    }


// =====================================================================================

}
