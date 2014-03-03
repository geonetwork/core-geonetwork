//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import jeeves.xlink.XLink;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.util.Sha1Encoder;
import org.jdom.*;

import java.io.File;
import java.util.*;

/**
 * Extracts subtemplates from a set of selected metadata records.
 */
public class BatchExtractSubtemplates extends NotInReadOnlyModeService {
	private Map<String,List<Namespace>> namespaceList = new HashMap<String,List<Namespace>>();

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getBean(DataManager.class);
		UserSession   session   = context.getUserSession();

		Set<Integer> metadata = new HashSet<Integer>();
		Set<Integer> notFound = new HashSet<Integer>();
		Set<Integer> notOwner = new HashSet<Integer>();
		Set<Integer> subtemplates = new HashSet<Integer>();

		// Get xpath, extract-title XSLT name, category and test from args
		String xpath = Util.getParam(params, Params.XPATH);

		String getTit = Util.getParam(params, Params.EXTRACT_TITLE, "");
		String xpathTit = Util.getParam(params, Params.XPATH_TITLE, "");
		if (getTit.length() > 0) {
			if (!(new File(getTit).exists())) {
				throw new IllegalArgumentException("Cannot find xslt "+getTit+" to extract a title field from a subtemplate");
			}
		} else {
			if (xpathTit.length() == 0) {
				throw new IllegalArgumentException("Must specify an xpath parameter "+Params.XPATH_TITLE+" or an XSLT file as parameter "+Params.EXTRACT_TITLE+" to select a title field from a subtemplate");
			}
		}
		String uuid = Util.getParam(params, Params.UUID, "");

		String category = Util.getParam(params, Params.CATEGORY);
		String changeParam = Util.getParam(params, Params.DOCHANGES, "off");
		boolean doChanges = changeParam.equals("on");

		if (!(category.equals("_none_")) && category.startsWith("_")) {
			category = category.substring(1);
		} else {
			category = null;
		}

		Element response = new Element(Jeeves.Elem.RESPONSE);

		// --- see if we need to process selected set or just uuid
		if (uuid.length() == 0) { // no uuid so process selected set
    	if(context.isDebugEnabled()) context.debug("Get selected metadata");

			SelectionManager sm = SelectionManager.getManager(session);

			synchronized(sm.getSelection("metadata")) {
			for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
				uuid = (String) iter.next();
				processRecord(context, uuid, category, xpath, getTit, xpathTit, doChanges, metadata, notFound, notOwner, subtemplates, response);

			}
			}
			// Clear the selection after extraction
			SelectionManager.updateSelection("metadata", session, params.addContent(new Element("selected").setText("remove-all")), context);

		} else { // just process the uuid passed in
			processRecord(context, uuid, category, xpath, getTit, xpathTit, doChanges, metadata, notFound, notOwner, subtemplates, response);
		}

        dataMan.flush();

        // -- reindex metadata
		context.info("Re-indexing metadata");
		Set<Integer> indexers = new HashSet<Integer>();
		indexers.addAll(metadata);
		indexers.addAll(subtemplates);
		BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataMan, indexers);
		r.process();

		// -- for the moment just return the sizes - we could return the ids
		// -- at a later stage for some sort of result display
		response.addContent(new Element("done")			.setText(metadata.size()+""));
		response.addContent(new Element("subtemplates").setText(subtemplates.size()+""));
		response.addContent(new Element("notOwner").setText(notOwner.size()+""));
		response.addContent(new Element("notFound").setText(notFound.size()+""));
		return response; 
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private void processRecord(ServiceContext context, String uuid, String category, String xpath, String getTit, String xpathTit, boolean doChanges, Set<Integer> metadata, Set<Integer> notFound, Set<Integer> notOwner, Set<Integer> subtemplates, Element response) throws Exception {

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getBean(DataManager.class);
		AccessManager accessMan = gc.getBean(AccessManager.class);

		if (context.isDebugEnabled())
			context.debug("Extracting subtemplates from metadata with uuid:"+ uuid);

        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        Metadata metadataEntity = metadataRepository.findOneByUuid(uuid);

		// Metadata may have been deleted since selection
		if (metadataEntity != null) {
            String id   = "" + metadataEntity.getId();

			if (!accessMan.isOwner(context, id)) {
				notOwner.add(metadataEntity.getId());
			} else {
				extractSubtemplates(context, dataMan, metadataEntity, category, xpath, getTit, xpathTit, doChanges, metadata, subtemplates, response);
			}
		} else {
      if(context.isDebugEnabled())
       	context.debug("  Metadata not found in db:"+ uuid);
		}
	}

	@SuppressWarnings("unchecked")
    private void extractSubtemplates(ServiceContext context, DataManager dataMan, Metadata metadataEntity, String category, String xpath, String getTit, String xpathTit, boolean doChanges, Set<Integer> metadata, Set<Integer> subtemplates, Element response) throws Exception {

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		// get metadata
		Element md = dataMan.getMetadataNoInfo(context, "" + metadataEntity.getId());
        MetadataDataInfo mdInfo = metadataEntity.getDataInfo();

		// Build a list of all Namespaces in the metadata document
		List<Namespace> metadataNamespaces = namespaceList.get(mdInfo.getSchemaId());
		if (metadataNamespaces == null) {
			metadataNamespaces = new ArrayList<Namespace>();
			Namespace ns = md.getNamespace();
			if (ns != null) {
				metadataNamespaces.add(ns);
				metadataNamespaces.addAll(md.getAdditionalNamespaces());
				namespaceList.put(mdInfo.getSchemaId(), metadataNamespaces);
			}
		}

		new Document(md);
		// select all nodes that come back from the xpath selectNodes
		List<?> nodes = Xml.selectNodes(md, xpath, metadataNamespaces);
		if (context.isDebugEnabled() || !doChanges) {
			context.debug("xpath \n"+xpath+"\n returned "+nodes.size()+" results");
			if (!doChanges) {
				response.addContent(new Element("xpath").setText(xpath));
				response.addContent(new Element("xpathReturned").setText(nodes.size()+" results"));
			}
		}


		// for each node
		for (Iterator<?> iter = nodes.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof Element) {
				Element elem = (Element)o;
				if (context.isDebugEnabled() || !doChanges) {
					context.debug("Test: Subtemplate with \n"+Xml.getString(elem));
					if (!doChanges) {
						response.addContent(new Element("subtemplate").setText(Xml.getString(elem)));
					}
				}

				// extract title from node
				String title = null;
				if (getTit.length() > 0) { // use xslt path in getTit
					Element xmlTitle = Xml.transform((Element)elem.clone(), getTit);
					if (context.isDebugEnabled() || !doChanges) {
						context.debug("Test: Title \n"+Xml.getString(xmlTitle));
					}
					title = xmlTitle.getText();
				} else { // use xpathTit
					List<?> titNodes = Xml.selectNodes(elem, xpathTit, metadataNamespaces);
					StringBuilder sb = new StringBuilder();
					for (Iterator<?> iterTit = titNodes.iterator(); iterTit.hasNext();) {
						Object oTit = iterTit.next();
						if (oTit instanceof Element) { // getText
							Element eTit = (Element)oTit;
							sb.append(eTit.getTextTrim());
						} else if (oTit instanceof Comment) { // not sure about this one!
							Comment cTit = (Comment)oTit;
							sb.append(cTit.getText());
						} else if (oTit instanceof Attribute) {
							Attribute aTit = (Attribute)oTit;
							sb.append(aTit.getValue());
						} else if (oTit instanceof Text) {
							Text tTit = (Text)oTit;
							sb.append(tTit.getTextTrim());
						}
						sb.append(" ");
					}
					title = sb.toString().trim();
				}

				if (!doChanges) {
					response.addContent(new Element("title").setText(title));
				}

	
				String uuid = elem.getAttributeValue("uuid");
				if (uuid == null || uuid.length() == 0) {
					// calc uuid based on sha1sum of content - this is the way to
					// generate this safely
					uuid = Sha1Encoder.encodeString(elem.getValue());
				}
	
				if (doChanges) { // insert subtemplate if it isn't already present
					if (dataMan.getMetadataId(uuid) != null) {
						context.debug("Test: Subtemplate with uuid "+uuid+" already exists");
					} else {
						//if (context.isDebug()) context.info("Test: Add subtemplate uuid "+uuid);

						// add node as a subtemplate
						String docType = null, createDate = null, changeDate = null;
						String group = "1"; 
						int user = context.getUserSession().getUserIdAsInt(); 
						boolean ufo = false, indexImmediate = false;

                        String sId = dataMan.insertMetadata(context, mdInfo.getSchemaId(), (Element) elem.clone(), uuid, user, group,
                                gc.getBean(SettingManager.class).getSiteId(), "s", docType, category, createDate, changeDate, ufo, indexImmediate);
						subtemplates.add(Integer.valueOf(sId));
					}
				}

				// replace node in record with a new node and xlink to the subtemplate
				Element parent = elem.getParentElement();
				int iIndex = parent.indexOf(elem);
				Element newElem = new Element(elem.getName(), elem.getNamespace());
				newElem.setAttribute("uuidref", uuid);
                newElem.setAttribute("href", context.getBean(SettingManager.class).getSiteURL(context) +"/xml.metadata.get?uuid="+uuid, XLink.NAMESPACE_XLINK);
				newElem.setAttribute("show", "replace", XLink.NAMESPACE_XLINK);
				parent.removeContent(iIndex);
				parent.addContent(iIndex,newElem);
				if (!doChanges) {
					response.addContent(new Element("replacedElement").setText(Xml.getString(newElem)));
				}
					
			}

			if (!doChanges) break; // we only process the first template
		}

		// update metadata record
		if (context.isDebugEnabled() || !doChanges) {
			if (nodes.size() > 0) {
				context.debug("Would update record with \n"+Xml.getString(md));
			}
		}

		if (doChanges) {
			boolean validate = false, ufo = false, indexImmediate = false;
			dataMan.updateMetadata(context, "" + metadataEntity.getId(), md, validate, ufo, indexImmediate, context.getLanguage(), new ISODate().toString(), true);

			metadata.add(metadataEntity.getId());
		}
		
	}

}