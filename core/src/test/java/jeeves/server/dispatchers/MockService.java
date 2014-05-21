package jeeves.server.dispatchers;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;

/**
 * For testing with {@link ServiceManagerIntegratedTest}
 *
 * Created by Jesse on 3/11/14.
 */
public class MockService implements Service {

    private ServiceFunction service;
    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        if (service != null) {
            return service.exec(params, context);
        }
        return null;
    }

    public void setService(ServiceFunction service) {
        this.service = service;
    }
}
