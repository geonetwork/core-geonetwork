//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package jeeves.server.context;

import jeeves.monitor.MonitorManager;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.Level;
import org.fao.geonet.Logger;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

//=============================================================================

/**
 * Contains a minimun context for a job execution (schedule, service etc...)
 */
public class BasicContext {

    private final ConfigurableApplicationContext jeevesApplicationContext;

    protected Logger logger = Log.createLogger(BasicContext.class,Log.JEEVES_MARKER);
    protected Map<String, Object> htContexts;
    private String baseUrl;
    private EntityManager entityManager;

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public BasicContext(ConfigurableApplicationContext jeevesApplicationContext, Map<String, Object> contexts, EntityManager entityManager) {
        this.jeevesApplicationContext = jeevesApplicationContext;
        htContexts = Collections.unmodifiableMap(contexts);
        this.entityManager = entityManager;
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------


    public Logger getLogger() {
        return logger;
    }

    //--- read/write objects
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String name) {
        baseUrl = name;
    }

    //--------------------------------------------------------------------------

    public Path getAppPath() {
        return this.jeevesApplicationContext.getBean(GeonetworkDataDirectory.class).getWebappDir();
    }

    //--------------------------------------------------------------------------

    public Object getHandlerContext(String contextName) {
        return htContexts.get(contextName);
    }

    //--------------------------------------------------------------------------

    public MonitorManager getMonitorManager() {
        return getBean(MonitorManager.class);
    }

    //--------------------------------------------------------------------------

    public ConfigurableApplicationContext getApplicationContext() {
        return jeevesApplicationContext;
    }
    //--------------------------------------------------------------------------

    public
    @Nonnull
    <T> T getBean(Class<T> beanType) {
        return jeevesApplicationContext.getBean(beanType);
    }

    public
    @Nonnull
    <T> T getBean(String name, Class<T> beanType) {
        return jeevesApplicationContext.getBean(name, beanType);
    }


    //--------------------------------------------------------------------------

    public EntityManager getEntityManager() {
        return entityManager;
    }

    //--------------------------------------------------------------------------

    @Deprecated
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    @Deprecated
    public void debug(final String message) {
        getLogger().debug(message);
    }

    @Deprecated
    public void info(final String message) {
        logger.info(message);
    }

    @Deprecated
    public void warning(final String message) {
        logger.warning(message);
    }

    @Deprecated
    public void error(final String message) {
        logger.error(message);
    }

    @Deprecated
    public void error(Throwable ex) {
        logger.error(ex);
    }

    @Deprecated
    public void fatal(final String message) {
        logger.fatal(message);
    }

    @Deprecated
    public String getModule() {
        return getLogger().getModule();
    }

    @Deprecated
    public void setAppender(FileAppender fa) {
        logger.setAppender(fa);
    }

    @Deprecated
    public String getFileAppender() {
        return getLogger().getFileAppender();
    }

    @Deprecated
    public Level getThreshold() {
        return getLogger().getThreshold();
    }

    /**
     * Return the id of the current node.
     *
     * @return the id of the current node.
     */
    public String getNodeId() {
        try {
            return this.jeevesApplicationContext.getBean(NodeInfo.class).getId();
        } catch (Exception e) {}
        return NodeInfo.DEFAULT_NODE;
    }
}

//=============================================================================

