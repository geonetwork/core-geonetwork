package org.fao.geonet.services.metadata.schema;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;

/**
 * Provides the Distpatch API for the different actions available to all the services.
 *
 * Created by Jesse on 2/13/14.
 */
public abstract class AbstractSchematronService implements Service {
    private SchematronServiceAction _action = SchematronServiceAction.LIST;

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        this._action = SchematronServiceAction.lookup(params);
    }

    @Override
    public final Element exec(Element params, ServiceContext context) throws Exception {
        switch (_action) {
            case ADD:
                return add(params, context);
            case DELETE:
                return delete(params, context);
            case EDIT:
                return edit(params, context);
            case EXISTS:
                Element response = new Element(Jeeves.Elem.RESPONSE);
                return response.setText(Boolean.toString(exists(params, context)));
            default:
                return list(params, context);
        }

    }

    protected abstract Element list(Element params, ServiceContext context) throws Exception;

    protected abstract boolean exists(Element params, ServiceContext context) throws Exception;

    protected abstract Element edit(Element params, ServiceContext context) throws Exception;

    protected abstract Element delete(Element params, ServiceContext context) throws Exception;

    protected abstract Element add(Element params, ServiceContext context) throws Exception;
}
