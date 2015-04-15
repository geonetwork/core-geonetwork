//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.metadata;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom status action class for Sextant.
 * 
 * Changes defined in https://forge.ifremer.fr/mantis/view.php?id=15011
 * 
 * @author francois
 * 
 */
public class SextantStatusActions extends DefaultStatusActions {

	/**
	 * Constructor.
	 */
	public SextantStatusActions() {
		super();
	}

	/**
	 * Called when a record is edited to set/reset status.
	 * Default status for record is DRAFT.
	 * 
	 * @param id
	 *            The metadata id that has been edited.
	 * @param minorEdit
	 *            If true then the edit was a minor edit.
	 */
	public void onEdit(int id, boolean minorEdit) throws Exception {
		
		// TODO : turn it off - it should be activated only for some records
//		if (dm.getCurrentStatus(dbms, id).equals(Params.Status.UNKNOWN)) {
//			dm.setStatus(context, dbms, id,
//					Integer.valueOf(Params.Status.DRAFT),
//					new ISODate().toString(),
//					"Set metadata with status UNKNOWN to status DRAFT");
//		}
//      https://forge.ifremer.fr/mantis/view.php?id=24431
//		super.onEdit(id, minorEdit);
	}
	
	/** 
	  * Called when need to set status on a set of metadata records.
	  *
		* @param status The status to set.
		* @param metadataIds The set of metadata ids to set status on.
		* @param changeDate The date the status was changed.
		* @param changeMessage The message explaining why the status has changed.
		*/
        public Set<Integer> statusChange(String status, Set<Integer> metadataIds,
                ISODate changeDate, String changeMessage) throws Exception {
		Set<Integer> unchanged = new HashSet<Integer>();

        // -- process the metadata records to set status
        for (Integer mid : metadataIds) {
            String currentStatus = dm.getCurrentStatus(mid);

            // --- if the status is already set to value of status then do nothing
            if (status.equals(currentStatus)) {
                unchanged.add(mid);
            }

            if (status.equals(Params.Status.APPROVED)) {
                // setAllOperations(mid); - this is a short cut that could be enabled
            } else if (status.equals(Params.Status.DRAFT) ||
                       status.equals(Params.Status.REJECTED)) {
                unsetAllOperations(mid);
            }

            // --- set status, indexing is assumed to take place later
            dm.setStatusExt(context, mid, Integer.valueOf(status),
                    changeDate, changeMessage);
        }

        // --- inform content reviewers if the status is submitted
        if (status.equals(Params.Status.SUBMITTED)) {
            informContentReviewers(metadataIds, changeDate.toString(), changeMessage);
            // --- inform owners if status is approved
        } else if (status.equals(Params.Status.APPROVED) || status.equals(Params.Status.REJECTED) || status.equals(Params.Status.RETIRED) || status.equals(Params.Status.UNKNOWN)) {
            informOwners(metadataIds, changeDate.toString(), changeMessage, status);
        }

        return unchanged;
	}
}