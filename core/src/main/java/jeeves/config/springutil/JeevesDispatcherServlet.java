package jeeves.config.springutil;

import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.fao.geonet.domain.User;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jesse on 6/3/2014.
 */
public class JeevesDispatcherServlet extends DispatcherServlet{

    private String nodeId;

    @Override
    protected void doDispatch(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        TransactionManager.runInTransaction("jeevesDispatchServlet", getWebApplicationContext(),
                TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED,
                TransactionManager.CommitBehavior.ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS,
                false, new TransactionTask<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus transaction) throws Throwable {
                        JeevesDispatcherServlet.super.doDispatch(request, response);

                        return null;
                    }
                });
    }

    @Override
    protected WebApplicationContext findWebApplicationContext() {
        final ServletContext servletContext = getServletContext();
        return createWebApplicationContext((org.springframework.context.ApplicationContext) servletContext.getAttribute(User.NODE_APPLICATION_CONTEXT_KEY + this.nodeId));
    }


    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

}
