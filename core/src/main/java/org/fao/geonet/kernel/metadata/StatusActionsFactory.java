//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import jeeves.server.context.ServiceContext;

import java.lang.reflect.Constructor;

/**
 * Handle creation of {@link StatusActions} instances.
 */
public class StatusActionsFactory {

    Class<StatusActions> statusRules;

    /**
     * StatusAction implementation configured for creation.
     */
    private String className;
    private static final String DEFAULT_STATUS_ACTION_CLASS = "org.fao.geonet.kernel.metadata.DefaultStatusActions";

    /**
     * Setup factory with default StatusActions implementation.
     */
    public StatusActionsFactory() {
        new StatusActionsFactory(DEFAULT_STATUS_ACTION_CLASS);
    }

    /**
     * Setup factory to use named StatusActions implementation.
     * @param className StatusActions implementation
     */
    public StatusActionsFactory(String className) {
        this.className = className;
        try {
            this.statusRules = (Class<StatusActions>) Class.forName(this.className);
        } catch (ClassNotFoundException e) {
            Log.warning(Geonet.DATA_MANAGER, String.format(
                "Class name '%s' is not found. You MUST use a valid class name loaded in the classpath. " +
                    "The default status action class is used (ie. %s)",
                this.className, DEFAULT_STATUS_ACTION_CLASS
            ));
            try {
                this.statusRules = (Class<StatusActions>) Class.forName(DEFAULT_STATUS_ACTION_CLASS);
            } catch (ClassNotFoundException e1) {
                Log.error(Geonet.DATA_MANAGER, String.format(
                    "Default class name '%s' is not found. This should not happen.",
                    DEFAULT_STATUS_ACTION_CLASS
                ));
            }
        }
    }

    /**
     * Creates a StatusActions class and initializes it using Reflections.
     *
     * @param context ServiceContext from Jeeves
     */
    public StatusActions createStatusActions(ServiceContext context) throws Exception {
        Constructor<StatusActions> constructor = this.statusRules.getConstructor();
        StatusActions statusAction = constructor.newInstance();
        statusAction.init(context);

        return statusAction;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
