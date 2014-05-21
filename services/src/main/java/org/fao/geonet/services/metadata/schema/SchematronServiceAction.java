package org.fao.geonet.services.metadata.schema;

import jeeves.server.ServiceConfig;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.services.thesaurus.EditElement;
import org.jdom.Element;

/**
 * Parse the action parameter of one of the schematron services.
 *
 * Created by Jesse on 2/13/14.
 */
public enum SchematronServiceAction {
    LIST, ADD, EXISTS, DELETE, EDIT;

    public static SchematronServiceAction lookup(ServiceConfig params) {
        String action = params.getValue(Params.ACTION);
        for (SchematronServiceAction serviceAction : values()) {
            if (serviceAction.name().equalsIgnoreCase(action)) {
                return serviceAction;
            }
        }

        throw new BadParameterEx(Params.ACTION, action);
    }
}
