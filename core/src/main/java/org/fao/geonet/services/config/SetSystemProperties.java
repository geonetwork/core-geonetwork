package org.fao.geonet.services.config;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.util.ThreadPool;
import org.jdom.Element;

/**
 * Configure geonetwork's thread pool to not add tasks to thread pool but instead execute them in calling thread.
 *
 * Required for integration tests
 *
 * User: jeichar
 * Date: 1/19/12
 * Time: 9:01 PM
 */
public class SetSystemProperties implements Service {

    public void init(String appPath, ServiceConfig params) throws Exception {
        // empty
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        final String propertyName = Util.getParam(params, "name");
        final boolean newValue = Boolean.parseBoolean(Util.getParam(params, "value"));
        if(propertyName.equalsIgnoreCase(ThreadPool.SEQUENTIAL_EXECUTION)) {
            System.setProperty(ThreadPool.SEQUENTIAL_EXECUTION, Boolean.toString(newValue));
        } else if (propertyName.equalsIgnoreCase(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD)){
            System.setProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD, Boolean.toString(newValue));
        } else {
            throw new IllegalArgumentException("system property: "+propertyName+" is not permitted to be set via web API");
        }
        return new Element("response").setText("ok");
    }
}
