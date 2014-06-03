package jeeves.config.springutil;

import org.fao.geonet.domain.User;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;

/**
 * @author Jesse on 6/3/2014.
 */
public class JeevesDispatcherServlet extends DispatcherServlet{
    private String nodeId;

    @Override
    protected WebApplicationContext findWebApplicationContext() {
        final ServletContext servletContext = getServletContext();
        return createWebApplicationContext((org.springframework.context.ApplicationContext) servletContext.getAttribute(User.NODE_APPLICATION_CONTEXT_KEY + this.nodeId));
    }


    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

}
