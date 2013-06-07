//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

public class StatusActionsFactory {

	Class<StatusActions> statusRules;

	/**
	  * Constructor.
		*
		* @param statusRules Class defined in WEB-INF/config.xml that defines status actions
		*/
	public StatusActionsFactory(Class<StatusActions> statusRules) {
		this.statusRules = statusRules;
	}	

	/**
	  * Creates a StatusActions class and initializes it using Reflections.
		*
		* @param context ServiceContext from Jeeves
		* @param dbms Database management system channel
		*/
	public StatusActions createStatusActions(ServiceContext context, Dbms dbms) throws Exception {
		Constructor<StatusActions> ct = statusRules.getConstructor();
		StatusActions sa = (StatusActions)ct.newInstance();
		
		Method init = statusRules.getMethod("init", new Class[] {
			ServiceContext.class, /* context */
			Dbms.class            /* dbms channel */
		});

		init.invoke(sa, new Object [] { context, dbms }); 
		return sa;
	}

    /**
     * Calls statusChange method from the StatusActions class using Reflections.
     * 
     * @param sa StatusActions class to call statusChange method from
     * @param status The status to set on a group of metadata ids
     * @param metadataIds The set of metadata records to set status on
     * @param changeDate The date of the status change
     * @param changeMessage The explanation for the status change
     */
    public Set<Integer> statusChange(StatusActions sa, String status, Set<Integer> metadataIds, String changeDate, String changeMessage)
            throws Exception {

        Method statusChange = statusRules.getMethod("statusChange", new Class[] { 
                String.class, /* Status */
                Set.class, /* Metadata ids */
                String.class, /* changeDate */
                String.class /* changeMessage */
        });

        @SuppressWarnings("unchecked")
        Set<Integer> result = (Set<Integer>) statusChange.invoke(sa, new Object[] { status, metadataIds, changeDate, changeMessage });
        return result;
    }

	/**
	  * When record is edited do status change to Draft and reset permissions.
		*
		* @param sa StatusActions class to call statusChange method from
		* @param id The metadata records to set status on
		* @param minorEdit Is this a minor edit?
		*/
	public void onEdit(StatusActions sa, int id, boolean minorEdit) throws Exception {

		Method onEdit = statusRules.getMethod("onEdit", new Class[] {
      int.class,    /* metadata id */
      boolean.class /* minorEdit */
      });

    onEdit.invoke(sa, new Object[] {
      id,
      minorEdit
      });
	}

}
