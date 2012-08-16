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

package org.fao.geonet.lib;

import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Geonetwork;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.springframework.security.access.AccessDeniedException;

import java.io.File;
import java.util.Set;

/**
 * Utility class to deal with data and removed directory.
 * Also provide user privileges checking method. 
 *
 */
public class ResourceLib {
	/**
	 * Get GeoNetwork data directory defined on startup see
	 * {@link Geonetwork#start(org.jdom.Element, ServiceContext)}.
	 * 
	 * @param context
	 * @return
	 */
	public String getDataDir(ServiceContext context) {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		String dataDir = gc.getHandlerConfig().getMandatoryValue(
				Geonet.Config.DATA_DIR);

		return dataDir;
	}

	/**
	 * Get metadata public or private data directory. See
	 * {@link #getDir(String, String, String)}.
	 */
	public String getDir(ServiceContext context, String access, String id) {
		return getDir(getDataDir(context), access, id);
	}

	/**
	 * Get metadata data directory. See {@link #getMetadataDir(String, String)}.
	 */
	public String getMetadataDir(ServiceContext context, String id) {
		return getMetadataDir(getDataDir(context), id);
	}

	/**
	 * Get metadata public or private data directory
	 * 
	 * @param dataDir
	 *            The root data directory
	 * @param access
	 *            The type of data directory. {@link Params.Access#PUBLIC} or
	 *            {@link Params.Access#PRIVATE}
	 * @param id
	 *            The metadata identifier
	 * @return The data directory
	 */
	public String getDir(String dataDir, String access, String id) {
		String mdDir = getMetadataDir(dataDir, id);
		String subDir = (access != null && access.equals(Params.Access.PUBLIC)) ? Params.Access.PUBLIC
				: Params.Access.PRIVATE;
		return mdDir + subDir + "/";
	}

	/**
	 * Get the metadata data directory
	 * 
	 * @param dataDir
	 *            The root data directory
	 * @param id
	 *            The metadata identifier
	 * @return The metadata data directory
	 */
	public String getMetadataDir(String dataDir, String id) {
		String group = pad(Integer.parseInt(id) / 100, 3);
		String groupDir = group + "00-" + group + "99";

		return dataDir + "/" + groupDir + "/" + id + "/";
	}

	/**
	 * Check that the operation is allowed for current user. See
	 * {@link AccessManager#getOperations(ServiceContext, String, String)}.
	 * 
	 * @param context
	 * @param id
	 *            The metadata identifier
	 * @param operation
	 *            See {@link AccessManager}.
	 * @throws Exception
	 */
	public void checkPrivilege(ServiceContext context, String id,
			String operation) throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);

		AccessManager accessMan = gc.getAccessManager();

		Set hsOper = accessMan.getOperations(context, id, context
				.getIpAddress());
		
		if (!hsOper.contains(operation)) {
			denyAccess(context);
		}
	}

	private void denyAccess(ServiceContext context) throws Exception {
		if (context.getUserSession().isAuthenticated()) {
			throw new AccessDeniedException("User is not permitted to access this resource");
		} else {
			throw new OperationNotAllowedEx();
		}
	}

	public void checkEditPrivilege(ServiceContext context, String id)
			throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		AccessManager am = gc.getAccessManager();

		if (!am.canEdit(context, id))
			denyAccess(context);
	}

	/**
	 * @return the absolute path of the folder choosen to store all deleted
	 *         metadata
	 */
	public String getRemovedDir(ServiceContext context) {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);

		String remDir = gc.getSettingManager().getValue(
				"system/removedMetadata/dir");

		if (!new File(remDir).isAbsolute())
			remDir = context.getAppPath() + remDir;

		return remDir;
	}

	/**
	 * See {@link #getRemovedDir(String, String)}
	 * 
	 * @param context
	 * @param id
	 * @return
	 */
	public String getRemovedDir(ServiceContext context, String id) {
		return getRemovedDir(getRemovedDir(context), id);
	}

	/**
	 * @return the absolute path of the folder where the given metadata should
	 *         be stored when it is removed
	 */
	public String getRemovedDir(String removedDir, String id) {
		String group = pad(Integer.parseInt(id) / 100, 3);
		String groupDir = group + "00-" + group + "99";

		return removedDir + "/" + groupDir + "/";
	}

	// -----------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// -----------------------------------------------------------------------------

	private String pad(int group, int lenght) {
		String text = Integer.toString(group);

		while (text.length() < lenght)
			text = "0" + text;

		return text;
	}
}