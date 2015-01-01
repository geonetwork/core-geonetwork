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

package org.fao.geonet.services.mef;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.mef.MEFLib.Format;
import org.fao.geonet.kernel.mef.MEFLib.Version;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.*;

/**
 * Export one or more metadata records in Metadata Exchange Format (MEF) file
 * format. See http ://trac.osgeo.org/geonetwork/wiki/MEF for more details.
 */
public class Export implements Service {
	private Path stylePath;
	private ServiceConfig _config;

	public void init(Path appPath, ServiceConfig params) throws Exception {
		this.stylePath = appPath.resolve(Geonet.Path.SCHEMAS);
		this._config = params;
	}

	/**
	 * Service to do a MEF export.
	 *
	 * @param params
	 *            Service input parameters:
	 *            <ul>
	 *            <li>uuid: Required for MEF1. Optional for MEF2, Current
	 *            selection is used</li>
	 *            <li>format: One of {@link Format}</li>
	 *            <li>skipUuid: Does not add uuid and site information for
	 *            information file.
	 *            <li>version: If {@link Version} parameter not set, return MEF
	 *            version 1 file (ie. need an UUID parameter).</li>
	 *            <li>relation: export related metadata or not. True by default.
	 *            Related metadata is : child metadata (Using parentUuid search
	 *            field), service metadata (Using operatesOn search field),
	 *            related metadata (Using xml.relation.get service).</li>
	 *            </ul>
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {

		// Get parameters
		Path file = null;
		String uuid = Util.getParam(params, "uuid", null);
		String format = Util.getParam(params, "format", "full");
		String version = Util.getParam(params, "version", null);
		boolean skipUUID = Boolean.parseBoolean(Util.getParam(params, "skipUuid", "false"));
        boolean resolveXlink = Boolean.parseBoolean(Util.getParam(params, "resolveXlink", "true"));
        boolean removeXlinkAttribute = Boolean.parseBoolean(Util.getParam(params, "removeXlinkAttribute", "true"));
		String relatedMetadataRecord = Util
				.getParam(params, "relation", "true");

		UserSession session = context.getUserSession();

		Log.info(Geonet.MEF, "Create export task for selected metadata(s).");
		SelectionManager selectionManger = SelectionManager.getManager(session);
		Set<String> uuids = selectionManger
				.getSelection(SelectionManager.SELECTION_METADATA);
		Set<String> uuidsBeforeExp = Collections
				.synchronizedSet(new HashSet<String>(0));
        Log.info(Geonet.MEF, "Current record(s) in selection: " + uuids.size());
        uuidsBeforeExp.addAll(uuids);

        // If provided uuid, export the metadata record only
        if (uuid != null) {
            SelectionManager.getManager(session).close(SelectionManager.SELECTION_METADATA);

            SelectionManager.getManager(session).addSelection(
                    SelectionManager.SELECTION_METADATA, uuid);

            uuids = selectionManger
                    .getSelection(SelectionManager.SELECTION_METADATA);
        }


        // If provided uuid, export the metadata record only
        if (uuid != null) {
            SelectionManager.getManager(session).close(SelectionManager.SELECTION_METADATA);

            SelectionManager.getManager(session).addSelection(
                    SelectionManager.SELECTION_METADATA, uuid);

            uuids = selectionManger
                    .getSelection(SelectionManager.SELECTION_METADATA);
        }

		// MEF version 1 only support one metadata record by file.
		// Uuid parameter MUST be set and add to selection manager before
		// export.
		if (version == null) {
			file = MEFLib.doExport(context, uuid, format, skipUUID, resolveXlink, removeXlinkAttribute);
		} else {
			// MEF version 2 support multiple metadata record by file.

			if (relatedMetadataRecord.equals("true")) {
				// Adding children in MEF file
				Set<String> tmpUuid = new HashSet<String>();
				for (Iterator<String> iter = uuids.iterator(); iter.hasNext();) {
					String _uuid = (String) iter.next();

					// Creating request for services search
					Element childRequest = new Element("request");
					childRequest.addContent(new Element("parentUuid")
							.setText(_uuid));
					childRequest.addContent(new Element("to").setText("1000"));

					// Get children to export - It could be better to use GetRelated service TODO
					Set<String> childs = getUuidsToExport(_uuid, context,
							childRequest);
					if (childs.size() != 0) {
						tmpUuid.addAll(childs);
					}

					// Creating request for services search
					Element servicesRequest = new Element(Jeeves.Elem.REQUEST);
					servicesRequest.addContent(new Element(
							org.fao.geonet.constants.Params.OPERATES_ON)
							.setText(_uuid));
					servicesRequest.addContent(new Element(
							org.fao.geonet.constants.Params.TYPE)
							.setText("service"));

					// Get linked services for export
					Set<String> services = getUuidsToExport(_uuid, context,
							servicesRequest);
					if (services.size() != 0) {
						tmpUuid.addAll(services);
					}
				}

				if (selectionManger.addAllSelection(SelectionManager.SELECTION_METADATA, tmpUuid)) {
                    Log.info(Geonet.MEF, "Child and services added into the selection");
                }
			}

			uuids = selectionManger.getSelection(SelectionManager.SELECTION_METADATA);
			Log.info(Geonet.MEF, "Building MEF2 file with " + uuids.size()
                                 + " records.");

			file = MEFLib.doMEF2Export(context, uuids, format, false, stylePath, resolveXlink, removeXlinkAttribute);
		}

		// -- Reset selection manager
		selectionManger.close();
		selectionManger.addAllSelection(SelectionManager.SELECTION_METADATA,
				uuidsBeforeExp);

		String fname = String.valueOf(Calendar.getInstance().getTimeInMillis());

		return BinaryFile.encode(200, file, "export-" + format + "-" + fname + ".zip", true).getElement();
	}

	/**
	 * Run an XML query and return a list of UUIDs.
	 * 
	 * @param uuid
	 *            Metadata identifier
	 * @param context
	 *            Current context
	 * @param request
	 *            XML Request to run which will search for related metadata
	 *            records to export
	 * @return List of related UUIDs to export
	 * @throws Exception
	 */
	private Set<String> getUuidsToExport(String uuid, ServiceContext context,
			Element request) throws Exception {
        if(Log.isDebugEnabled(Geonet.MEF))
            Log.debug(Geonet.MEF, "Creating searcher to run request: " + Xml.getString(request));

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager searchMan = gc.getBean(SearchManager.class);
		MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);

		Set<String> uuids = new HashSet<String>();

		// perform the search
		searcher.search(context, request, _config);

		// If element type found, then get their uuid
		if (searcher.getSize() != 0) {
            if(Log.isDebugEnabled(Geonet.MEF))
                Log.debug(Geonet.MEF, "  Exporting record(s) found for metadata: " + uuid);
			Element elt = searcher.present(context, request, _config);

			// Get ISO records only
			@SuppressWarnings("unchecked")
            List<Element> isoElt = elt.getChildren();
			for (Element md : isoElt) {
				// -- Only metadata record should be processed
				if (!md.getName().equals("summary")) {
					String mdUuid = md.getChild(Edit.RootChild.INFO,
							Edit.NAMESPACE).getChildText(Edit.Info.Elem.UUID);
                    if(Log.isDebugEnabled(Geonet.MEF)) Log.debug(Geonet.MEF, "    Adding: " + mdUuid);
					uuids.add(mdUuid);
				}
			}
		}
		searcher.close();
		Log.info(Geonet.MEF, "  Found " + uuids.size() + " record(s).");

		return uuids;
	}
}