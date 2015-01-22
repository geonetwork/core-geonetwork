package org.fao.geonet.services.metadata;

import com.google.common.base.Optional;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.AddElemValue;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.utils.Log;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.lib.Lib;
import org.jdom.*;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import java.util.HashMap;
import java.util.Iterator;
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
     * Apply a list of changes to the metadata record in current editing session.
     * 
     * <p>
     * The changes are a list of KVP. A key contains at least the element identifier from
     * the meta-document. A key starting with an "X" should contain an XML fragment 
     * for the value.
     * </p>
     * 
     * The following KVP combinations are allowed:
     * <ul>
     * <li>ElementId=ElementValue </li>
     * <li>ElementId_AttributeName=AttributeValue</li>
     * <li>ElementId_AttributeNamespacePrefixCOLONAttributeName=AttributeValue</li>
     * <li>XElementId=ElementValue</li>
     * <li>XElementId_replace=ElementValue</li>
     * <li>XElementId_ElementName=ElementValue</li>
     * <li>XElementId_ElementName_replace=ElementValue</li>
     * <li>P{key}=xpath with P{key}_xml=XML snippet</li>
     * </ul>
     * 
     * ElementName MUST contain "{@value #EditLib.COLON_SEPARATOR}" instead of ":" for prefixed elements.
     * 
     * <p>
     * When using X key ElementValue could contains many XML fragments (eg. 
     * &lt;gmd:keywords .../&gt;{@value #XML_FRAGMENT_SEPARATOR}&lt;gmd:keywords .../&gt;)
     * separated by {@link #XML_FRAGMENT_SEPARATOR}. All those fragments are inserted
     * to the last element of this type in its parent if ElementName is set.
     * If not, the element with ElementId is replaced.
     * If _replace suffix is used, then all elements having the same type than elementId are removed before insertion.
     * 
     * </p>
     * 
     * <p>
     * <pre>
     *  _Pd2295e223:/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/
     *              gmd:citation/gmd:CI_Citation/
     *              gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'creation']
     *              
     *  _Pd2295e223_xml:&lt;gmd:date/&gt; ... &lt;/gmd:date&gt;
     * </pre>
     * </p>
     * 
     * @param id        Metadata internal identifier.
     * @param changes   List of changes to apply.
     * @param currVersion       Editing version which is checked against current editing version.
     * @return  The update metadata record
     * @throws Exception
     */
    protected Element applyChangesEmbedded(String id,
                                        Map<String, String> changes, String currVersion) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);

        String schema = dataManager.getMetadataSchema(id);
        MetadataSchema metadataSchema = dataManager.getSchema(schema);
        EditLib editLib = dataManager.getEditLib();

        // --- check if the metadata has been modified from last time
        if (currVersion != null && !editLib.getVersion(id).equals(currVersion)) {
            Log.error(Geonet.EDITOR, "Version mismatch: had " + currVersion + 
                                            " but expected " + editLib.getVersion(id));
            return null;
        }

        // --- get metadata from session
        Element md = getMetadataFromSession(session, id);

        // Store XML fragments to be handled after other elements update
        Map<String, String> xmlInputs = new HashMap<String, String>();
        Map<String, AddElemValue> xmlAndXpathInputs = new HashMap<String, AddElemValue>();

        // --- update elements
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            String ref = entry.getKey().trim();
            String value = entry.getValue().trim();
            String attribute = null;
            
            // Avoid empty key
            if (ref.equals("")) {
                continue;
            }
            
            // Catch element starting with a X to replace XML fragments
            if (ref.startsWith("X")) {
                ref = ref.substring(1);
                xmlInputs.put(ref, value);
                continue;
            } else if (ref.startsWith("P") && ref.endsWith("_xml")) {
                continue;
            } else if (ref.startsWith("P") && !ref.endsWith("_xml")) {
                // Catch element starting with a P for xpath update mode
                String snippet = changes.get(ref + "_xml");

                if(Log.isDebugEnabled(Geonet.EDITOR)) {
                  Log.debug(Geonet.EDITOR, "Add element by XPath: " + value);
                  Log.debug(Geonet.EDITOR, "  Snippet is : " + snippet);
                }

                if (snippet != null && !"".equals(snippet)) {
                    xmlAndXpathInputs.put(value, new AddElemValue(snippet));
                } else {
                    Log.warning(Geonet.EDITOR, "No XML snippet or value found for xpath " + value + " and element ref " + ref);
                }
                continue;
            }

            if (updatedLocalizedTextElement(md, schema, ref, value, editLib)) {
                continue;
            }

            int at = ref.indexOf('_');
            if (at != -1) {
                attribute = ref.substring(at + 1);
                ref = ref.substring(0, at);
            }
            
            Element el = editLib.findElement(md, ref);
            if (el == null) {
                Log.error(Geonet.EDITOR, EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);
                continue;
            }
            
            // Process attribute
            if (attribute != null) {
                Pair<Namespace, String> attInfo = parseAttributeName(attribute, EditLib.COLON_SEPARATOR, id, md, editLib);
                String localname = attInfo.two();
                Namespace attrNS = attInfo.one();
                if (el.getAttribute(localname, attrNS) != null) {
                    el.setAttribute(new Attribute(localname, value, attrNS));
                }
            } else {
                // Process element value
                @SuppressWarnings("unchecked")
                List<Content> content = el.getContent();
                
                for (Iterator<Content> iterator = content.iterator(); iterator.hasNext();) {
                    Content content2 = iterator.next();
                    if (content2 instanceof Text) {
                        iterator.remove();
                    }
                }
                el.addContent(value);
            }
        }
        
        // Deals with XML fragments to insert or update
        if (!xmlInputs.isEmpty()) {
            editLib.addXMLFragments(schema, md, xmlInputs);
        }

        // Deals with XML fragments and XPath to insert or update
        if (!xmlAndXpathInputs.isEmpty()) {
            editLib.addElementOrFragmentFromXpaths(md, xmlAndXpathInputs, metadataSchema, true);
        }
        
        setMetadataIntoSession(session,(Element)md.clone(), id);
        
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
        if(Log.isDebugEnabled(Geonet.EDITOR))
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
        if(Log.isDebugEnabled(Geonet.EDITOR))
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
        if(Log.isDebugEnabled(Geonet.EDITOR))
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
	    boolean keepXlinkAttributes = false;
		Element md = dataManager.getMetadata(srvContext, id, forEditing, withValidationErrors, keepXlinkAttributes);
		UserSession session = srvContext.getUserSession();
		setMetadataIntoSession(session, md, id);
		return md;
	}

    /**
     * For Ajax Editing : adds an element or an attribute to a metadata element ([add] link).
     *
     * @param session
     * @param id
     * @param ref
     * @param name
     * @param childName
     * @return
     * @throws Exception
     */
	public synchronized Element addElementEmbedded(UserSession session, String id, String ref, String name, String childName)  throws Exception {
	    Lib.resource.checkEditPrivilege(context, id);
		String  schema = dataManager.getMetadataSchema(id);
		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- ref is parent element so find it
		EditLib editLib = dataManager.getEditLib();
        Element el = editLib.findElement(md, ref);
		if (el == null)
			throw new IllegalStateException(EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);

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
				@SuppressWarnings("unchecked")
                List<Element> attributeDefs = el.getChildren(Edit.RootChild.ATTRIBUTE, Edit.NAMESPACE);
				for (Element attributeDef : attributeDefs) {
					if (attributeDef != null && attributeDef.getAttributeValue(Edit.Attribute.Attr.NAME).equals(name)) {
						Element defaultChild = attributeDef.getChild(Edit.Attribute.Child.DEFAULT, Edit.NAMESPACE);
						if (defaultChild != null) {
							defaultValue = defaultChild.getAttributeValue(Edit.Attribute.Attr.VALUE);
						}
					}
				}
				
				Pair<Namespace, String> attInfo = parseAttributeName(name, ":", id, md, editLib);
			    //--- Add new attribute with default value
                el.setAttribute(new Attribute(attInfo.two(), defaultValue, attInfo.one()));
                
				// TODO : add attribute should be false and del true after adding an attribute
				child = el;
			} else {
				//--- normal element
				child = editLib.addElement(mds, el, name);
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
			child = editLib.addElement(mds, el, name);
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
     * @param session
     * @param id
     * @param ref
     * @param parentRef
     * @return
     * @throws Exception
     */
	public synchronized Element deleteElementEmbedded(UserSession session, String id, String ref, String parentRef) throws Exception {
	    Lib.resource.checkEditPrivilege(context, id);

		String schema = dataManager.getMetadataSchema(id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- locate the geonet:info element and clone for later re-use
		Element info = (Element)(md.getChild(Edit.RootChild.INFO,Edit.NAMESPACE)).clone();
		md.removeChild(Edit.RootChild.INFO,Edit.NAMESPACE);

		//--- get element to remove
        EditLib editLib = dataManager.getEditLib();
		Element el = editLib.findElement(md, ref);

		if (el == null)
			throw new IllegalStateException(EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);


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
				@SuppressWarnings("unchecked")
                List<Element> children = parent.getContent(chFilter);

				for (Element ch : children) {
					String name = ch.getAttributeValue("name");
					if (name != null && name.equals(uName)) {
						result = (Element) ch.clone();
						break;
					}
				}
				
				// -- now delete the element as requested
				parent.removeContent(me);
				
				//--- existing geonet child element not present so create it and insert it
				//--- where the last element was deleted
				if (result == null) {
					result = editLib.createElement(schema,el,parent);
					parent.addContent(me,result);
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
	 * @param session
	 * @param id
	 * @param ref	Attribute identifier (eg. _169_uom).
	 * @return
	 * @throws Exception
	 */
	public synchronized Element deleteAttributeEmbedded(UserSession session, String id, String ref) throws Exception {
	    Lib.resource.checkEditPrivilege(context, id);

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
		    Pair<Namespace, String> attInfo = parseAttributeName(attributeName, ":", id, md, editLib);
		    el.removeAttribute(attInfo.two(), attInfo.one());
		}

		//--- store the metadata in the session again
		setMetadataIntoSession(session,(Element)md.clone(), id);

		return result;
	}

    private Pair<Namespace, String> parseAttributeName(String attributeName, String separator,
            String id, Element md, EditLib editLib) throws Exception {
        
        Integer indexColon = attributeName.indexOf(separator);
        String localname = attributeName;
        Namespace attrNS = Namespace.NO_NAMESPACE;
        // ... with qualified name
        if (indexColon != -1) {
            String prefix = attributeName.substring(0, indexColon);
            localname = attributeName.substring(indexColon + separator.length());
            String  schema = dataManager.getMetadataSchema(id);
            String namespace = editLib.getNamespace(prefix + ":" + localname, md, dataManager.getSchema(schema));
            attrNS = Namespace.getNamespace(prefix, namespace);
        }
        return Pair.write(attrNS, localname);
    }
    /**
     * For Ajax Editing : swap element with sibling ([up] and [down] links).
     *
     * @param session
     * @param id
     * @param ref
     * @param down
     * @throws Exception
     */
	public synchronized void swapElementEmbedded(UserSession session, String id, String ref, boolean down) throws Exception {
	    Lib.resource.checkEditPrivilege(context, id);

	    dataManager.getMetadataSchema(id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- get element to swap
        EditLib editLib = dataManager.getEditLib();
		Element elSwap = editLib.findElement(md, ref);

		if (elSwap == null)
			throw new IllegalStateException(EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);

		//--- swap the elements
		int iSwapIndex = -1;

		@SuppressWarnings("unchecked")
        List<Element> list = elSwap.getParentElement().getChildren(elSwap.getName(), elSwap.getNamespace());

		int i = -1;
		for (Element element : list) {
		    i++;
            if (element == elSwap) {
                iSwapIndex = i;
                break;
            }
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
     * @param id
     * @param lang
     * @return
     * @throws Exception
     */
	public Element validateMetadataEmbedded(UserSession session, String id, String lang) throws Exception {
		String schema = dataManager.getMetadataSchema(id);

		//--- get metadata from session and clone it for validation
		Element realMd = getMetadataFromSession(session, id);
		Element md = (Element)realMd.clone();

		//--- remove editing info
        EditLib editLib = dataManager.getEditLib();
		editLib.removeEditingInfo(md);
		editLib.contractElements(md);
        String parentUuid = null;
        md = dataManager.updateFixedInfo(schema, Optional.of(Integer.valueOf(id)), null, md, parentUuid, UpdateDatestamp.NO, context);

		//--- do the validation on the metadata
		return dataManager.doValidate(session, schema, id, md, lang, false).one();

	}

    /**
     * For Editing : adds an attribute from a metadata ([add] link).
	 * FIXME: Modify and use within Ajax controls
     *
     * @param id
     * @param ref
     * @param name
     * @param currVersion
     * @return
     * @throws Exception
     */
	public synchronized boolean addAttribute(String id, String ref, String name, String currVersion) throws Exception {
	    Lib.resource.checkEditPrivilege(context, id);

		Element md = xmlSerializer.select(context, id);

		//--- check if the metadata has been deleted
		if (md == null)
			return false;

		String schema = dataManager.getMetadataSchema(id);
        EditLib editLib = dataManager.getEditLib();
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
			return false;

		//--- get element to add
		Element el = editLib.findElement(md, ref);

		if (el == null)
			Log.error(Geonet.DATA_MANAGER, EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);
			//throw new IllegalStateException("Element not found at ref = " + ref);

		//--- remove editing info added by previous call
		editLib.removeEditingInfo(md);

        if (el != null) {
            el.setAttribute(new Attribute(name, ""));
        }

        editLib.contractElements(md);
        String parentUuid = null;
		md = dataManager.updateFixedInfo(schema, Optional.of(Integer.valueOf(id)), null, md, parentUuid, UpdateDatestamp.NO, context);
        String changeDate = null;
        xmlSerializer.update(id, md, changeDate, false, null, context);

        // Notifies the metadata change to metatada notifier service
        dataManager.notifyMetadataChange(md, id);

		//--- update search criteria
        dataManager.indexMetadata(id, true);

        return true;
	}

    /**
     * For Editing : removes an attribute from a metadata ([del] link).
	 * FIXME: Modify and use within Ajax controls
     *
     * @param id
     * @param ref
     * @param name
     * @param currVersion
     * @return
     * @throws Exception
     */
	public synchronized boolean deleteAttribute(String id, String ref, String name, String currVersion) throws Exception {
	    Lib.resource.checkEditPrivilege(context, id);

		Element md = xmlSerializer.select(context, id);

		//--- check if the metadata has been deleted
		if (md == null)
			return false;

		String schema = dataManager.getMetadataSchema(id);
        EditLib editLib = dataManager.getEditLib();
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
			return false;

		//--- get element to remove
		Element el = editLib.findElement(md, ref);

		if (el == null)
			throw new IllegalStateException(EditLib.MSG_ELEMENT_NOT_FOUND_AT_REF + ref);

		//--- remove editing info added by previous call
		editLib.removeEditingInfo(md);

		el.removeAttribute(name);

		editLib.contractElements(md);
        String parentUuid = null;
        md = dataManager.updateFixedInfo(schema, Optional.of(Integer.valueOf(id)), null, md, parentUuid, UpdateDatestamp.NO, context);

        String changeDate = null;
				xmlSerializer.update(id, md, changeDate, false, null, context);

        // Notifies the metadata change to metatada notifier service
        dataManager.notifyMetadataChange(md, id);

		//--- update search criteria
        dataManager.indexMetadata(id, true);

        return true;
	}
}
