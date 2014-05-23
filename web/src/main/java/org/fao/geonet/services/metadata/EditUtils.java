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

package org.fao.geonet.services.metadata;

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.lib.Lib;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 * Utilities.
 */
class EditUtils {

    public EditUtils(ServiceContext context) {
        this.context = context;
        this.gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        this.dataManager = gc.getDataManager();
        this.xmlSerializer = gc.getXmlSerializer();
        this.accessMan = gc.getAccessManager();
        this.session = context.getUserSession();

    }
    protected ServiceContext context;
    protected DataManager dataManager;
    protected XmlSerializer xmlSerializer;
	protected GeonetContext gc;
	protected AccessManager accessMan;
	protected UserSession session;

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

    /**
     * Performs common editor preprocessing tasks.
     *
     * @param params
     * @param context
     * @throws Exception
     */
	public void preprocessUpdate(Element params, ServiceContext context) throws Exception {

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		String id = Util.getParam(params, Params.ID);

		//-----------------------------------------------------------------------
		//--- handle current tab and position

		Element elCurrTab = params.getChild(Params.CURRTAB);
		Element elCurrPos = params.getChild(Params.POSITION);

		if (elCurrTab != null) {
			session.setProperty(Geonet.Session.METADATA_SHOW, elCurrTab.getText());
		}
		if (elCurrPos != null)
			session.setProperty(Geonet.Session.METADATA_POSITION, elCurrPos.getText());

		//-----------------------------------------------------------------------
		//--- check access
		int iLocalId = Integer.parseInt(id);

		if (!dataManager.existsMetadata(dbms, iLocalId))
			throw new BadParameterEx("id", id);

		if (!accessMan.canEdit(context, id))
		    Lib.resource.denyAccess(context);
	}

    /**
     * Updates metadata content.
     *
     * @param params
     * @param validate
     * @throws Exception
     */
	public void updateContent(Element params, boolean validate) throws Exception {
		 updateContent(params, validate, false);
	}

    /**
     * TODO javadoc.
     *
     * @param params
     * @param validate
     * @param embedded
     * @throws Exception
     */
	public void updateContent(Element params, boolean validate, boolean embedded) throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		String id      = Util.getParam(params, Params.ID);
		String version = Util.getParam(params, Params.VERSION);
        String minor      = Util.getParam(params, Params.MINOREDIT, "false");

		//--- build hashtable with changes
		//--- each change is a couple (pos, value)

		Hashtable htChanges = new Hashtable(100);
		List list = params.getChildren();
		for(int i=0; i<list.size(); i++) {
			Element el = (Element) list.get(i);

			String sPos = el.getName();
			String sVal = el.getText();

			if (sPos.startsWith("_")) {
				htChanges.put(sPos.substring(1), sVal);
            }
		}

        //
		// update element and return status
        //

        boolean result = false;
        // whether to request automatic changes (update-fixed-info)
        boolean ufo = true;
        // whether to index on update
        boolean index = true;
        
        boolean updateDateStamp = !minor.equals("true");
        String changeDate = null;
		if (embedded) {
            Element updatedMetada = new AjaxEditUtils(context).applyChangesEmbedded(dbms, id, htChanges, version);
            if(updatedMetada != null) {
                result = dataManager.updateMetadata(context, dbms, id, updatedMetada, false, ufo, index, context.getLanguage(), changeDate, updateDateStamp);
            }
   		}
        else {
            Element updatedMetada = applyChanges(dbms, id, htChanges, version);
            if(updatedMetada != null) {
			    result = dataManager.updateMetadata(context, dbms, id, updatedMetada, validate, ufo, index, context.getLanguage(), changeDate, updateDateStamp);
            }
		}
		if (!result) {
			throw new ConcurrentUpdateEx(id);
        }
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param id
     * @param changes
     * @param currVersion
     * @return
     * @throws Exception
     */
    private Element applyChanges(Dbms dbms, String id, Hashtable changes, String currVersion) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);
        Element md = xmlSerializer.select(dbms, "Metadata", id, context);

		//--- check if the metadata has been deleted
		if (md == null) {
			return null;
        }

        EditLib editLib = dataManager.getEditLib();

        String schema = dataManager.getMetadataSchema(dbms, id);
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion)) {
			return null;
        }

		//--- update elements
		for(Enumeration e=changes.keys(); e.hasMoreElements();) {
			String ref = ((String) e.nextElement()) .trim();
			String val = ((String) changes.get(ref)).trim();
			String attr= null;

			if(updatedLocalizedTextElement(md, ref, val, editLib)) {
			    continue;
			}

			int at = ref.indexOf('_');
			if (at != -1) {
				attr = ref.substring(at +1);
				ref  = ref.substring(0, at);
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
					String prefix = attr.substring(0,indexColon);
                    String localname = attr.substring(indexColon + 5);
                    String namespace = editLib.getNamespace(prefix + ":" + localname, md, dataManager.getSchema(schema));
					Namespace attrNS = Namespace.getNamespace(prefix,namespace);
                    if (el.getAttribute(localname,attrNS) != null) {
                        el.setAttribute(new Attribute(localname,val,attrNS));
                    }
                // End of work-around
                }
                else {
                    if (el.getAttribute(attr) != null)
                        el.setAttribute(new Attribute(attr, val));
                }
			}
            else if(xmlContent) {
                if(Log.isDebugEnabled(Geonet.EDITOR))
                    Log.debug(Geonet.EDITOR, "replacing XML content");
				el.removeContent();
				val = addNamespaceToFragment(val);
				el.addContent(Xml.loadString(val, false));
            }
			else {
				List content = el.getContent();
				for(int i=0; i<content.size(); i++) {
					if (content.get(i) instanceof Text) {
						el.removeContent((Text) content.get(i));
						i--;
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
     * Adds a localised character string to an element.
     *
     * @param md metadata record
     * @param ref current ref of element. All _lang_AB_123 element will be processed.
     * @param val
     * @return
     */
    protected static boolean updatedLocalizedTextElement(Element md, String ref, String val, EditLib editLib) {
        if (ref.startsWith("lang")) {
            if (val.length() > 0) {
                String[] ids = ref.split("_");
                // --- search element in current metadata record
                Element parent = editLib.findElement(md, ids[2]);

                // --- add required attribute
                parent.setAttribute("type", "gmd:PT_FreeText_PropertyType", Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));

                // --- add new translation
                Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
                Element langElem = new Element("LocalisedCharacterString", gmd);
                langElem.setAttribute("locale", "#" + ids[1]);
                langElem.setText(val);

                Element freeText = getOrAdd(parent, "PT_FreeText", gmd);

                Element textGroup = new Element("textGroup", gmd);
                freeText.addContent(textGroup);
                textGroup.addContent(langElem);
                Element refElem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
                refElem.setAttribute(Edit.Element.Attr.REF, "");
                textGroup.addContent(refElem);
                langElem.addContent((Element) refElem.clone());
            }
            return true;
        }
        return false;
    }

	/**
	 * Adds missing namespace (ie. GML) to XML inputs. It should be done by the client side
	 * but add a check in here.
	 *
	 * @param fragment 		The fragment to be checked and processed.
	 *
	 * @return 				The updated fragment.
	 */
	protected static String addNamespaceToFragment(String fragment) {
        //add the gml namespace if its missing
        if (fragment.contains("<gml:") && !fragment.contains("xmlns:gml=\"")) {
            if(Log.isDebugEnabled(Geonet.EDITOR))
                Log.debug(Geonet.EDITOR, "  Add missing GML namespace.");
        	fragment = fragment.replaceFirst("<gml:([^ >]+)", "<gml:$1 xmlns:gml=\"http://www.opengis.net/gml\"");
        }
		return fragment;
	}

    /**
     * If no PT_FreeText element exists, creates a geonet:element with an empty ref.
     *
     * @param parent
     * @param name
     * @param ns
     * @return
     */
	protected static Element getOrAdd(Element parent, String name, Namespace ns) {
		Element child = parent.getChild(name, ns);
		if (child == null) {
			child = new Element(name, ns);
			Element refElem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
			refElem.setAttribute(Edit.Element.Attr.REF, "");
			child.addContent(refElem);
			parent.addContent(child);
		}
		return child;
	}

    /**
     *
     * @param params
     * @throws Exception
     */
	public void updateContent(Element params) throws Exception {
		updateContent(params, false);
	}

    /**
     * Used for editing : swaps 2 elements.
     *
     * @param el1
     * @param el2
     * @throws Exception
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

		Element el1Spare = (Element)el1.clone();

		parent.setContent(index1, (Element)el2.clone());
		parent.setContent(index2, el1Spare);
	}
}
