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

package org.fao.geonet.services.metadata;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * TODO javadoc.
 *
 */
public class StatusActionsFactory {

	Class<? extends StatusActions> statusRules;

	/**
	  * Constructor.
		*
		* @param statusRules Class defined in WEB-INF/config.xml that defines status actions
		*/
	public StatusActionsFactory(Class<? extends Object> statusRules) {
		this.statusRules = (Class<? extends StatusActions>) statusRules;
	}	

	/**
	  * Creates a StatusActions class and initializes it using Reflections.
		*
		* @param context ServiceContext from Jeeves
		* @param dbms Database management system channel
		*/
	public StatusActions createStatusActions(ServiceContext context, Dbms dbms) throws Exception {
		Constructor<? extends StatusActions> ct = statusRules.getConstructor();
		StatusActions sa = ct.newInstance();
		
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
     * @return
     * @throws Exception
		*/
	public Set<String> statusChange(StatusActions sa, String status, Set<String> metadataIds, String changeDate, String changeMessage) throws Exception {

		Method statusChange = statusRules.getMethod("statusChange", new Class[] {
      String.class,       /* Status */
      Set.class,          /* Metadata ids */
      String.class,       /* changeDate */
      String.class        /* changeMessage */
      });

        try {
            Set<String> result = (Set<String>)statusChange.invoke(sa, status, metadataIds, changeDate, changeMessage);
            return result;
        }
        catch(InvocationTargetException x) {
            System.out.println(x.getCause().getMessage());
            x.getCause().printStackTrace();
            throw x;
        }
	}

	/**
	  * When record is edited do status change to Draft and reset permissions.
		*
		* @param sa StatusActions class to call statusChange method from
		* @param id The metadata records to set status on
		* @param minorEdit Is this a minor edit?
     * @throws Exception
		*/
	public void onEdit(StatusActions sa, String id, boolean minorEdit) throws Exception {

		Method onEdit = statusRules.getMethod("onEdit", new Class[] {
      String.class,    /* metadata id */
      boolean.class /* minorEdit */
      });
        try {
        onEdit.invoke(sa, id, minorEdit);
        }
        catch(InvocationTargetException x) {
            System.out.println(x.getCause().getMessage());
            x.getCause().printStackTrace();
            throw x;
        }
	}

    /**
     * When metadata is created, sets status to JUSTCREATED.
     *
     * @param sa
     * @param id
     * @throws Exception
     */
    public void onCreate(StatusActions sa, String id) throws Exception {
        Method onCreate = statusRules.getMethod("onCreate", new Class[]{
                String.class     // metadata id
        });
        try {
            onCreate.invoke(sa, id);
        }
        catch(InvocationTargetException x) {
            System.out.println(x.getCause().getMessage());
            x.getCause().printStackTrace();
            throw x;
        }
    }

    /**
     * When edit sessino is canceled, set current status to the last status before the current DRAFT status.
     *
     * @param sa StatusActions class to call statusChange method from
     * @param id The metadata records to set status on
     * @throws Exception hmm
     */
    public void onCancelEdit(StatusActions sa, String id) throws Exception {

        Method onCancelEdit = statusRules.getMethod("onCancelEdit", new Class[] {
                String.class    /* metadata id */
      });

        try {
        onCancelEdit.invoke(sa, id);
        }
        catch(InvocationTargetException x) {
            System.out.println(x.getCause().getMessage());
            x.getCause().printStackTrace();
            throw x;
        }
	}

}
