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

package org.fao.geonet.api.records.editing;

import com.google.common.collect.Lists;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.schema.MultilingualSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Utilities.
 */
class EditUtils {

    protected ServiceContext context;
    protected DataManager dataManager;
    protected XmlSerializer xmlSerializer;
    protected GeonetContext gc;
    protected AccessManager accessMan;
    protected UserSession session;
    protected IMetadataValidator metadataValidator;

    public EditUtils(ServiceContext context) {
        this.context = context;
        this.gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        this.dataManager = gc.getBean(DataManager.class);
        this.xmlSerializer = gc.getBean(XmlSerializer.class);
        this.accessMan = gc.getBean(AccessManager.class);
        this.session = context.getUserSession();
        this.metadataValidator = gc.getBean(IMetadataValidator.class);

    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    /**
     * Visit all descendants of an element and add an empty geonet:ref element for later use by the
     * editor.
     */
    protected static void addMissingGeoNetRef(Element element) {
        Iterator<Object> descendants = element.getDescendants();
        List<Object> list = Lists.newArrayList(descendants);

        for (Object descendant : list) {
            if (descendant instanceof Element) {
                Element e = (Element) descendant;
                if (e.getName() != Edit.RootChild.ELEMENT
                    && e.getNamespace() != Edit.NAMESPACE) {
                    Element geonetRef = e.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
                    if (geonetRef == null) {
                        geonetRef = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
                        geonetRef.setAttribute(Edit.Element.Attr.REF, "");
                        e.addContent(geonetRef);
                    }
                }
            }
        }
    }

    /**
     * Performs common editor preprocessing tasks.
     */
    public void preprocessUpdate(Element params, ServiceContext context) throws Exception {

        String id = Util.getParam(params, Params.ID);

        //-----------------------------------------------------------------------
        //--- handle current tab and position

        Element elCurrTab = params.getChild(Params.CURRTAB);
        Element elCurrPos = params.getChild(Params.POSITION);
        boolean useEditTab = Util.getParam(params, "editTab", false);
        String sessionTabProperty = useEditTab ? Geonet.Session.METADATA_EDITING_TAB : Geonet.Session.METADATA_SHOW;

        if (elCurrTab != null) {
            session.setProperty(sessionTabProperty, elCurrTab.getText());
        }
        if (elCurrPos != null)
            session.setProperty(Geonet.Session.METADATA_POSITION, elCurrPos.getText());

        //-----------------------------------------------------------------------
        //--- check access
        int iLocalId = Integer.parseInt(id);

        if (!dataManager.existsMetadata(iLocalId))
            throw new BadParameterEx("id", id);

        if (!accessMan.canEdit(context, id))
            Lib.resource.denyAccess(context);
    }

    /**
     * Updates metadata content.
     */
    public void updateContent(Element params, boolean validate) throws Exception {
        updateContent(params, validate, false, IndexingMode.full);
    }

    /**
     * TODO javadoc.
     */
    public void updateContent(Element params, boolean validate, boolean embedded, IndexingMode indexingMode) throws Exception {
        String id = Util.getParam(params, Params.ID);
        String version = Util.getParam(params, Params.VERSION);
        String minor = Util.getParam(params, Params.MINOREDIT, "false");

        //--- build hashtable with changes
        //--- each change is a couple (pos, value)

        Map<String, String> htChanges = new HashMap<String, String>(100);
        @SuppressWarnings("unchecked")
        List<Element> list = params.getChildren();
        for (Element el : list) {
            String sPos = el.getName();
            String sVal = el.getText();

            if (sPos.startsWith("_")) {
                htChanges.put(sPos.substring(1), sVal);
            }
        }

        //
        // update element and return status
        //

        AbstractMetadata result = null;
        // whether to request automatic changes (update-fixed-info)
        boolean ufo = true;

        boolean updateDateStamp = !minor.equals("true");
        String changeDate = null;
        if (embedded) {
            Element updatedMetada = new AjaxEditUtils(context).applyChangesEmbedded(id, htChanges, version);
            if (updatedMetada != null) {
                result = dataManager.updateMetadata(context, id, updatedMetada, false, ufo, context.getLanguage(), changeDate, updateDateStamp, indexingMode);
            }
        } else {
            Element updatedMetada = applyChanges(id, htChanges, version);
            if (updatedMetada != null) {
                result = dataManager.updateMetadata(context, id, updatedMetada, validate, ufo, context.getLanguage(), changeDate, updateDateStamp, indexingMode);
            }
        }
        if (result == null) {
            throw new ConcurrentUpdateEx(id);
        }
    }

    /**
     * TODO javadoc.
     */
    private Element applyChanges(String id, Map<String, String> changes, String currVersion) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);
        Element md = xmlSerializer.select(context, id);

        //--- check if the metadata has been deleted
        if (md == null) {
            return null;
        }

        EditLib editLib = dataManager.getEditLib();

        String schema = dataManager.getMetadataSchema(id);
        editLib.expandElements(schema, md);
        editLib.enumerateTree(md);

        //--- check if the metadata has been modified from last time
        if (currVersion != null && !editLib.getVersion(id).equals(currVersion)) {
            return null;
        }

        //--- update elements
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            String ref = entry.getKey().trim();
            String val = entry.getValue().trim();
            String attr = null;

            if (updatedLocalizedTextElement(md, schema, ref, val, editLib)) {
                continue;
            }

            int at = ref.indexOf('_');
            if (at != -1) {
                attr = ref.substring(at + 1);
                ref = ref.substring(0, at);
            }
            boolean xmlContent = false;
            if (ref.startsWith("X")) {
                ref = ref.substring(1);
                xmlContent = true;
            }
            Element el = editLib.findElement(md, ref);
            if (el == null)
                throw new IllegalStateException("Element not found at ref = " + ref);

            if (attr != null) {
                // The following work-around decodes any attribute name that has a COLON in it
                // The : is replaced by the word COLON in the xslt so that it can be processed
                // by the XML Serializer when an update is submitted - a better solution is
                // to modify the argument handler in Jeeves to store arguments with their name
                // as a value rather than as the element itself
                Integer indexColon = attr.indexOf("COLON");
                if (indexColon != -1) {
                    String prefix = attr.substring(0, indexColon);
                    String localname = attr.substring(indexColon + 5);
                    String namespace = editLib.getNamespace(prefix + ":" + localname, md, dataManager.getSchema(schema));
                    Namespace attrNS = Namespace.getNamespace(prefix, namespace);
                    if (el.getAttribute(localname, attrNS) != null) {
                        el.setAttribute(new Attribute(localname, val, attrNS));
                    }
                    // End of work-around
                } else {
                    if (el.getAttribute(attr) != null)
                        el.setAttribute(new Attribute(attr, val));
                }
            } else if (xmlContent) {
                if (Log.isDebugEnabled(Geonet.EDITOR))
                    Log.debug(Geonet.EDITOR, "replacing XML content");
                el.removeContent();
                val = EditLib.addGmlNamespaceToFragment(val);
                el.addContent(Xml.loadString(val, false));
            } else {
                @SuppressWarnings("unchecked")
                List<Content> content = el.getContent();

                for (Iterator<Content> iterator = content.iterator(); iterator.hasNext(); ) {
                    Content content2 = iterator.next();

                    if (content2 instanceof Text) {
                        iterator.remove();
                    }
                }
                el.addContent(val);
            }
        }
        //--- remove editing info added by previous call
        editLib.removeEditingInfo(md);

        editLib.contractElements(md);
        return md;
    }

    /**
     * Adds a localised character string to an element for an ISO19139 record.
     *
     * <pre>
     * <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
     *    <gco:CharacterString>Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
     *    <gmd:PT_FreeText>
     *        <gmd:textGroup>
     *            <gmd:LocalisedCharacterString locale="#FRE">Modèle de données vectorielles en
     * ISO19139 (multilingue)</gmd:LocalisedCharacterString>
     *        </gmd:textGroup>
     * </pre>
     *
     * @param md  metadata record
     * @param ref current ref of element. All _lang_AB_123 element will be processed.
     */
    protected boolean updatedLocalizedTextElement(Element md, String schema,
                                                  String ref, String val, EditLib editLib) {
        if (ref.startsWith("lang")) {
            if (val.length() > 0) {

                SchemaPlugin schemaPlugin = SchemaManager.getSchemaPlugin(schema);
                if (schemaPlugin instanceof MultilingualSchemaPlugin) {
                    String[] ids = ref.split("_");
                    // --- search element in current parent
                    Element parent = editLib.findElement(md, ids[2]);
                    String language = ids[1];
                    List<Element> elems = ((MultilingualSchemaPlugin) schemaPlugin)
                        .getTranslationForElement(parent, language);

                    // Element exists, set the value
                    if (elems != null && elems.size() > 0) {
                        elems.get(0).setText(val);
                    } else {
                        ((MultilingualSchemaPlugin) schemaPlugin).addTranslationToElement(
                            parent, language, val
                        );
                        addMissingGeoNetRef(parent);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param params
     * @throws Exception
     */
    public void updateContent(Element params) throws Exception {
        updateContent(params, false);
    }

    /**
     * Used for editing : swaps 2 elements.
     */
    protected void swapElements(Element el1, Element el2) throws Exception {

        Element parent = el1.getParentElement();
        if (parent == null) {
            throw new IllegalArgumentException("No parent element for swapping");
        }

        int index1 = parent.indexOf(el1);
        if (index1 == -1) {
            throw new IllegalArgumentException("Element 1 not found for swapping");
        }
        int index2 = parent.indexOf(el2);
        if (index2 == -1) {
            throw new IllegalArgumentException("Element 2 not found for swapping");
        }

        Element el1Spare = (Element) el1.clone();

        parent.setContent(index1, (Element) el2.clone());
        parent.setContent(index2, el1Spare);
    }
}
