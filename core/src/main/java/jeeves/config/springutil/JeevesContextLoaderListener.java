package jeeves.config.springutil;

import com.google.common.io.Files;
import jeeves.server.JeevesEngine;
import jeeves.server.overrides.ConfigurationOverrides;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initializes the ApplicationContexts for each node.
 */
public class JeevesContextLoaderListener implements ServletContextListener {


    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        try {
        final Pattern nodeNamePattern = Pattern.compile("[a-zA-Z0-9_\\-]+");
        final ServletContext servletContext = sce.getServletContext();

        JeevesApplicationContext defaultContext = null;
        File[] nodes = getNodeConfigurationFiles(servletContext);

        if (nodes.length == 0) {
            throw new IllegalArgumentException("Need at least one node defined");
        }

        ConfigurationOverrides overrides = ConfigurationOverrides.DEFAULT;

        String parentConfigFile = "/WEB-INF/config-spring-geonetwork-parent.xml";

        JeevesApplicationContext parentAppContext = new JeevesApplicationContext(null, null, parentConfigFile);
        parentAppContext.setServletContext(servletContext);
        parentAppContext.refresh();

        String commonConfigFile = "/WEB-INF/config-spring-geonetwork.xml";
        for (File node : nodes) {
            String nodeId = Files.getNameWithoutExtension(node.getName());
            if (!nodeNamePattern.matcher(nodeId).matches()) {
                throw new IllegalArgumentException(nodeId + " has an illegal name.  Node names must be of the form: [a-zA-Z_\\-]+ ");

            }

            JeevesApplicationContext jeevesAppContext = new JeevesApplicationContext(overrides, parentAppContext,
                    "classpath:mapfish-spring-application-context.xml", commonConfigFile, node.toURI().toString());

            jeevesAppContext.setServletContext(servletContext);
            jeevesAppContext.refresh();

            // initialize all JPA Repositories.  This should be done outside of the init
            // because spring-data-jpa first looks up named queries (based on method names) and
            // if the query is not found an exception is thrown.  This exception will set rollback
            // on the transaction if a transaction is active.
            //
            // We want to initialize all repositories here so they are not lazily initialized
            // at random places through out the code where it may be in a transaction.
            jeevesAppContext.getBeansOfType(JpaRepository.class, false, true);

            servletContext.setAttribute(User.NODE_APPLICATION_CONTEXT_KEY + nodeId, jeevesAppContext);

            // check if the context is the default context
            NodeInfo nodeInfo = jeevesAppContext.getBean(NodeInfo.class);
            nodeInfo.setId(nodeId);

            boolean isDefault = nodeInfo.isDefaultNode();

            if (isDefault) {
                if (defaultContext != null) {
                    throw new IllegalArgumentException("Two nodes where defined as the default.  This is not acceptable.");
                }
                defaultContext = jeevesAppContext;

                servletContext.setAttribute(User.NODE_APPLICATION_CONTEXT_KEY, jeevesAppContext);
        }
        }

        if (defaultContext == null) {
            throw new IllegalArgumentException("There are no default contexts defined");
        }
        } catch (Throwable e) {
        	e.printStackTrace();
            JeevesEngine.handleStartupError(e);
        }
    }

    public static String[] getNodeIds(final ServletContext servletContext) {
        final File[] nodeConfigurationFiles = getNodeConfigurationFiles(servletContext);
        String [] ids = new String [nodeConfigurationFiles.length];

        for (int i = 0; i < nodeConfigurationFiles.length; i++) {
            File file = nodeConfigurationFiles[i];
            ids[i] = Files.getNameWithoutExtension(file.getName());
        }
        Arrays.sort(ids);
        return ids;
    }

    private static File[] getNodeConfigurationFiles(ServletContext servletContext) {

        final File nodeConfigDir = new File(servletContext.getRealPath("/WEB-INF/config-node"));
        final File[] files = nodeConfigDir.listFiles();
        if (files == null) {
            throw new IllegalStateException("No node configuration file found in: "+nodeConfigDir);
        }
        return files;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();

        for (String node : getNodeIds(sce.getServletContext())) {
            if (!node.trim().isEmpty()) {
                JeevesApplicationContext jeevesAppContext = (JeevesApplicationContext) servletContext.getAttribute(
                        User.NODE_APPLICATION_CONTEXT_KEY + node.trim());
                jeevesAppContext.destroy();
            }
        }
    }
}
