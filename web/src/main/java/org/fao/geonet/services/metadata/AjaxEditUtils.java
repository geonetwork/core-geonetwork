package org.fao.geonet.services.metadata;

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * // --------------------------------------------------------------------------
 * // ---
 * // --- Embedded Metadata Update API for AJAX Editor support
 * // ---
 * // --------------------------------------------------------------------------
 */
public class AjaxEditUtils extends EditUtils {

    public AjaxEditUtils(ServiceContext context) {
        super(context);
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
    protected Element applyChangesEmbedded(Dbms dbms, String id, Hashtable changes, String currVersion) throws Exception {
        String schema = dataManager.getMetadataSchema(dbms, id);
        EditLib editLib = dataManager.getEditLib();

        // --- check if the metadata has been modified from last time
        if (currVersion != null && !editLib.getVersion(id).equals(currVersion)) {
            Log.error(Geonet.EDITOR, "Version mismatch: had " + currVersion + " but expected " + editLib.getVersion(id));
            return null;
        }

        // --- get metadata from session
        Element md = getMetadataFromSession(session, id);

        // Store XML fragments to be handled after other elements update
        Map<String, String> xmlInputs = new HashMap<String, String>();

        // --- update elements
        for (Enumeration e = changes.keys(); e.hasMoreElements();) {
            String ref = ((String) e.nextElement()).trim();
            String val = ((String) changes.get(ref)).trim();
            String attr = null;

            // Catch element starting with a X to replace XML fragments
            if (ref.startsWith("X")) {
                ref = ref.substring(1);
                xmlInputs.put(ref, val);
                continue;
            }

            if (ref.equals(""))
                continue;

            if (updatedLocalizedTextElement(md, ref, val, editLib)) {
                continue;
            }

            int at = ref.indexOf('_');
            if (at != -1) {
                attr = ref.substring(at + 1);
                ref = ref.substring(0, at);
            }

            Element el = editLib.findElement(md, ref);
            if (el == null) {
                Log.error(Geonet.EDITOR, "Element not found at ref = " + ref);
                continue;
            }

            if (attr != null) {
                Integer indexColon = attr.indexOf("COLON");
                if (indexColon != -1) {
                    String prefix = attr.substring(0, indexColon);
                    String localname = attr.substring(indexColon + 5);
                    String namespace = editLib.getNamespace(prefix + ":" + localname, md, dataManager.getSchema(schema));
                    Namespace attrNS = Namespace.getNamespace(prefix, namespace);
                    if (el.getAttribute(localname, attrNS) != null) {
                        el.setAttribute(new Attribute(localname, val, attrNS));
                    }
                }
                else {
                    if (el.getAttribute(attr) != null) {
                        el.setAttribute(new Attribute(attr, val));
                    }
                }
            }
            else {
                List content = el.getContent();

                for (int i = 0; i < content.size(); i++) {
                    if (content.get(i) instanceof Text) {
                        el.removeContent((Text) content.get(i));
                        i--;
                    }
                }
                el.addContent(val);
            }
        }

        // Deals with XML fragments to insert or update
        if (!xmlInputs.isEmpty()) {

            // Loop over each XML fragments to insert or replace
            for (String ref : xmlInputs.keySet()) {
                String value = xmlInputs.get(ref);

                String name = null;
                int addIndex = ref.indexOf('_');
                if (addIndex != -1) {
                    name = ref.substring(addIndex + 1);
                    ref = ref.substring(0, addIndex);
                }

                // Get element to fill
                Element el = editLib.findElement(md, ref);

                if (el == null) {
                    throw new IllegalStateException("Element not found at ref = " + ref);
                }

                if (value != null && !value.equals("")) {
                    String[] fragments = value.split("&&&");
                    for (String fragment : fragments) {
                        if (name != null) {
                            name = name.replace("COLON", ":");
                            editLib.addFragment(schema, el, name, fragment);
                        }
                        else {
                            // clean before update
                            el.removeContent();

                            fragment = addNamespaceToFragment(fragment);

                            // Add content
                            el.addContent(Xml.loadString(fragment, false));
                        }
                    }
                    Log.debug(Geonet.EDITOR, "replacing XML content");
                }
            }
        }

        // --- remove editing info
        editLib.removeEditingInfo(md);

		editLib.contractElements(md);

        return (Element) md.detach();
    }

    /**
     * TODO javadoc.
     *
     * @param session
     * @param id
     * @return
     */
	protected static Element getMetadataFromSession(UserSession session, String id) {
		Log.debug(Geonet.EDITOR, "Retrieving metadata from session " + session.getUserId());
		Element md = (Element) session.getProperty(Geonet.Session.METADATA_EDITING + id);
		md.detach();
		return md;
	}

    /**
     * TODO javadoc.
     *
     * @param session
     * @param md
     * @param id
     */
	private void setMetadataIntoSession(UserSession session, Element md, String id) {
		Log.debug(Geonet.EDITOR, "Storing metadata in session "+session.getUserId());
		session.setProperty(Geonet.Session.METADATA_EDITING + id, md);
	}

    /**
     * For Ajax Editing : removes metadata from session.
     *
     * @param session
     * @param id
     */
	public void removeMetadataEmbedded(UserSession session, String id) {
		Log.debug(Geonet.EDITOR, "Removing metadata from session "+session.getUserId());
		session.removeProperty(Geonet.Session.METADATA_EDITING + id);
		session.removeProperty(Geonet.Session.VALIDATION_REPORT + id);
	}

    /**
     * For Ajax Editing : gets Metadata from database and places it in session.
     * @param srvContext
     * @param id
     * @param forEditing
     * @param withValidationErrors
     * @return
     * @throws Exception
     */
	public Element getMetadataEmbedded(ServiceContext srvContext, String id, boolean forEditing, boolean withValidationErrors) throws Exception {
		Element md = dataManager.getMetadata(srvContext, id, forEditing, withValidationErrors);
		UserSession session = srvContext.getUserSession();
		setMetadataIntoSession(session, md, id);
		return md;
	}

    /**
     * For Ajax Editing : adds an element or an attribute to a metadata element ([add] link).
     *
     * @param dbms
     * @param session
     * @param id
     * @param ref
     * @param name
     * @param childName
     * @return
     * @throws Exception
     */
	public synchronized Element addElementEmbedded(Dbms dbms, UserSession session, String id, String ref, String name, String childName)  throws Exception {
		String  schema = dataManager.getMetadataSchema(dbms, id);
		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- ref is parent element so find it
		EditLib editLib = dataManager.getEditLib();
        Element el = editLib.findElement(md, ref);
		if (el == null)
			throw new IllegalStateException("Element not found at ref = " + ref);

		//--- locate the geonet:element and geonet:info elements and clone for
		//--- later re-use
		Element refEl = (Element)(el.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE)).clone();
		Element info = (Element)(md.getChild(Edit.RootChild.INFO,Edit.NAMESPACE)).clone();
		md.removeChild(Edit.RootChild.INFO,Edit.NAMESPACE);

		Element child = null;
		MetadataSchema mds = dataManager.getSchema(schema);
		if (childName != null) {
			if (childName.equals("geonet:attribute")) {
				 String defaultValue = "";
				 List attributeDefs = el.getChildren(Edit.RootChild.ATTRIBUTE, Edit.NAMESPACE);
				 for (Object a : attributeDefs) {
					 Element attributeDef = (Element) a;
					 if (attributeDef != null && attributeDef.getAttributeValue(Edit.Attribute.Attr.NAME).equals(name)) {
						 Element defaultChild = attributeDef.getChild(Edit.Attribute.Child.DEFAULT, Edit.NAMESPACE);
						 if (defaultChild != null) {
							defaultValue = defaultChild.getAttributeValue(Edit.Attribute.Attr.VALUE);
						 }
					 }
				 }
				 //--- Add new attribute with default value
				 el.setAttribute(new Attribute(name, defaultValue));
				 // TODO : add attribute should be false and del true after adding an attribute
				child = el;
			} else {
				//--- normal element
				child = editLib.addElement(schema, el, name);
				if (!childName.equals(""))
				{
					//--- or element
					String uChildName = editLib.getUnqualifiedName(childName);
			        String prefix     = editLib.getPrefix(childName);
			        String ns         = editLib.getNamespace(childName,md,mds);
			        if (prefix.equals("")) {
			           prefix = editLib.getPrefix(el.getName());
			           ns = editLib.getNamespace(el.getName(),md,mds);
			        }
			        Element orChild = new Element(uChildName,prefix,ns);
			        child.addContent(orChild);

			        //--- add mandatory sub-tags
			        editLib.fillElement(schema, child, orChild);
				}
			}
		}
        else {
			child = editLib.addElement(schema, el, name);
		}
		//--- now enumerate the new child (if not a simple attribute)
		if (childName == null || !childName.equals("geonet:attribute")) {
			//--- now add the geonet:element back again to keep ref number
			el.addContent(refEl);

			int iRef = editLib.findMaximumRef(md);
			editLib.expandElements(schema, child);
			editLib.enumerateTreeStartingAt(child, iRef+1, Integer.parseInt(ref));

			//--- add editing info to everything from the parent down
			editLib.expandTree(mds,el);

		}
		//--- attach the info element to the child
		child.addContent(info);

		//--- attach the info element to the metadata root)
		md.addContent((Element)info.clone());

		//--- store the metadata in the session again
		setMetadataIntoSession(session,(Element)md.clone(), id);

		// Return element added
		return child;

	}

    /**
     * For Ajax Editing : removes an element from a metadata ([del] link).
     *
     * @param dbms
     * @param session
     * @param id
     * @param ref
     * @param parentRef
     * @return
     * @throws Exception
     */
	public synchronized Element deleteElementEmbedded(Dbms dbms, UserSession session, String id, String ref, String parentRef) throws Exception {

		String schema = dataManager.getMetadataSchema(dbms, id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- locate the geonet:info element and clone for later re-use
		Element info = (Element)(md.getChild(Edit.RootChild.INFO,Edit.NAMESPACE)).clone();
		md.removeChild(Edit.RootChild.INFO,Edit.NAMESPACE);

		//--- get element to remove
        EditLib editLib = dataManager.getEditLib();
		Element el = editLib.findElement(md, ref);

		if (el == null)
			throw new IllegalStateException("Element not found at ref = " + ref);


		String uName = el.getName();
		Namespace ns = el.getNamespace();
		Element parent = el.getParentElement();
		Element result = null;
		if (parent != null) {
			int me = parent.indexOf(el);

			//--- check and see whether the element to be deleted is the last one of its kind
			Filter elFilter = new ElementFilter(uName,ns);
			if (parent.getContent(elFilter).size() == 1) {

				//--- get geonet child element with attribute name = unqualified name
				Filter chFilter = new ElementFilter(Edit.RootChild.CHILD, Edit.NAMESPACE);
				List children = parent.getContent(chFilter);

				for (int i = 0; i < children.size(); i++) {
					Element ch = (Element) children.get(i);
					String name = ch.getAttributeValue("name");
					if (name != null && name.equals(uName)) {
						result = (Element) ch.clone();
						break;
					}
				}

				//--- existing geonet child element not present so create it and insert it
				//--- where the last element was deleted
				if (result == null) {
					result = editLib.createElement(schema,el,parent);
					parent.addContent(me,result);
				} else {
                    // -- now delete the element as requested
				    parent.removeContent(me);
				}
				result.setAttribute(Edit.ChildElem.Attr.PARENT,parentRef);
				result.addContent(info);
			}
			//--- if not the last one then just delete it
			else {
				parent.removeContent(me);
			}
		} else {
			throw new IllegalStateException("Element at ref = " + ref + " doesn't have a parent");
		}

		// if we don't need a child then create a geonet:null element
		if (result == null) {
			result = new Element(Edit.RootChild.NULL, Edit.NAMESPACE);
			result.addContent(info);
		}

		//--- reattach the info element to the metadata
		md.addContent((Element)info.clone());

		//--- store the metadata in the session again
		setMetadataIntoSession(session,(Element)md.clone(), id);

		return result;
	}

	/**
	 * Removes attribute in embedded mode.
	 *
	 * @param dbms
	 * @param session
	 * @param id
	 * @param ref	Attribute identifier (eg. _169_uom).
	 * @return
	 * @throws Exception
	 */
	public synchronized Element deleteAttributeEmbedded(Dbms dbms, UserSession session, String id, String ref) throws Exception {
		String[] token = ref.split("_");
		String elementId = token[1];
		String attributeName = token[2];
		Element result = new Element(Edit.RootChild.NULL, Edit.NAMESPACE);

		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- get element to remove
        EditLib editLib = dataManager.getEditLib();
		Element el = editLib.findElement(md, elementId);

		if (el != null) {
			el.removeAttribute(attributeName);
		}

		//--- store the metadata in the session again
		setMetadataIntoSession(session,(Element)md.clone(), id);

		return result;
	}

    /**
     * For Ajax Editing : swap element with sibling ([up] and [down] links).
     *
     * @param dbms
     * @param session
     * @param id
     * @param ref
     * @param down
     * @throws Exception
     */
	public synchronized void swapElementEmbedded(Dbms dbms, UserSession session, String id, String ref, boolean down) throws Exception {
        dataManager.getMetadataSchema(dbms, id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- get element to swap
        EditLib editLib = dataManager.getEditLib();
		Element elSwap = editLib.findElement(md, ref);

		if (elSwap == null)
			throw new IllegalStateException("Element not found at ref = " + ref);

		//--- swap the elements
		int iSwapIndex = -1;

		List list = ((Element) elSwap.getParent()).getChildren(elSwap.getName(), elSwap.getNamespace());

		for(int i=0; i<list.size(); i++)
			if (list.get(i) == elSwap)
			{
				iSwapIndex = i;
				break;
			}

		if (iSwapIndex == -1)
			throw new IllegalStateException("Index not found for element --> " + elSwap);

		if (down)	swapElements(elSwap, (Element) list.get(iSwapIndex +1));
			else		swapElements(elSwap, (Element) list.get(iSwapIndex -1));

		//--- store the metadata in the session again
		setMetadataIntoSession(session,(Element)md.clone(), id);

    }

    /**
     * For Ajax Editing : retrieves metadata from session and validates it.
     *
     * @param session
     * @param dbms
     * @param id
     * @param lang
     * @return
     * @throws Exception
     */
	public Element validateMetadataEmbedded(UserSession session, Dbms dbms, String id, String lang) throws Exception {
		String schema = dataManager.getMetadataSchema(dbms, id);

		//--- get metadata from session and clone it for validation
		Element realMd = getMetadataFromSession(session, id);
		Element md = (Element)realMd.clone();

		//--- remove editing info
        EditLib editLib = dataManager.getEditLib();
		editLib.removeEditingInfo(md);
		editLib.contractElements(md);
        String parentUuid = null;
        md = dataManager.updateFixedInfo(schema, id, null, md, parentUuid, DataManager.UpdateDatestamp.no, dbms, null);

		//--- do the validation on the metadata
		return dataManager.doValidate(session, dbms, schema, id, md, lang, false).one();

	}

    /**
     * For Editing : adds an attribute from a metadata ([add] link).
	 * FIXME: Modify and use within Ajax controls
     *
     * @param dbms
     * @param id
     * @param ref
     * @param name
     * @param currVersion
     * @return
     * @throws Exception
     */
	public synchronized boolean addAttribute(Dbms dbms, String id, String ref, String name, String currVersion) throws Exception {
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		//--- check if the metadata has been deleted
		if (md == null)
			return false;

		String schema = dataManager.getMetadataSchema(dbms, id);
        EditLib editLib = dataManager.getEditLib();
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
			return false;

		//--- get element to add
		Element el = editLib.findElement(md, ref);

		if (el == null)
			Log.error(Geonet.DATA_MANAGER, "Element not found at ref = " + ref);
			//throw new IllegalStateException("Element not found at ref = " + ref);

		//--- remove editing info added by previous call
		editLib.removeEditingInfo(md);

        if (el != null) {
            el.setAttribute(new Attribute(name, ""));
        }

        editLib.contractElements(md);
        String parentUuid = null;
		md = dataManager.updateFixedInfo(schema, id, null, md, parentUuid, DataManager.UpdateDatestamp.no, dbms, null);
        String changeDate = null;
		XmlSerializer.update(dbms, id, md, changeDate);

        // Notifies the metadata change to metatada notifier service
        dataManager.notifyMetadataChange(dbms, md, id);

		//--- update search criteria
        boolean indexGroup = false;
        dataManager.indexMetadata(dbms, id, indexGroup);

		return true;
	}

    /**
     * For Editing : removes an attribute from a metadata ([del] link).
	 * FIXME: Modify and use within Ajax controls
     *
     * @param dbms
     * @param id
     * @param ref
     * @param name
     * @param currVersion
     * @return
     * @throws Exception
     */
	public synchronized boolean deleteAttribute(Dbms dbms, String id, String ref, String name, String currVersion) throws Exception {
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		//--- check if the metadata has been deleted
		if (md == null)
			return false;

		String schema = dataManager.getMetadataSchema(dbms, id);
        EditLib editLib = dataManager.getEditLib();
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
			return false;

		//--- get element to remove
		Element el = editLib.findElement(md, ref);

		if (el == null)
			throw new IllegalStateException("Element not found at ref = " + ref);

		//--- remove editing info added by previous call
		editLib.removeEditingInfo(md);

		el.removeAttribute(name);

		editLib.contractElements(md);
        String parentUuid = null;
        md = dataManager.updateFixedInfo(schema, id, null, md, parentUuid, DataManager.UpdateDatestamp.no, dbms, null);

        String changeDate = null;
		XmlSerializer.update(dbms, id, md, changeDate);

        // Notifies the metadata change to metatada notifier service
        dataManager.notifyMetadataChange(dbms, md, id);

		//--- update search criteria
        boolean indexGroup = false;
        dataManager.indexMetadata(dbms, id, indexGroup);

		return true;
	}
}