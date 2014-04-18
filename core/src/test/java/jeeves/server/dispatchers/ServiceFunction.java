package jeeves.server.dispatchers;

import jeeves.server.context.ServiceContext;
import org.jdom.Element;

/**
 * The function of the MockService.
 * <p/>
 * Created by Jesse on 3/11/14.
 */
interface ServiceFunction {
    public Element exec(Element params, ServiceContext context) throws Exception;
}
