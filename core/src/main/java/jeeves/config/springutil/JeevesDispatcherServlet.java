/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package jeeves.config.springutil;

import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.User;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Define the transaction policy and set the node id depending on the URL path.
 *
 * @author Jesse on 6/3/2014.
 */
public class JeevesDispatcherServlet extends DispatcherServlet {

    @Override
    protected void doDispatch(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        TransactionManager.runInTransaction("jeevesDispatchServlet", getWebApplicationContext(),
            TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED,
            TransactionManager.CommitBehavior.ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS,
            false, new TransactionTask<Void>() {
                @Override
                public Void doInTransaction(TransactionStatus transaction) throws Throwable {
                    setNodeId(request);
                    JeevesDispatcherServlet.super.doDispatch(request, response);
                    return null;
                }
            });
    }

    @Override
    protected WebApplicationContext findWebApplicationContext() {
        final ServletContext servletContext = getServletContext();
        return createWebApplicationContext(
            (org.springframework.context.ApplicationContext) servletContext.getAttribute(User.NODE_APPLICATION_CONTEXT_KEY));
    }


    private void setNodeId(HttpServletRequest request) {
        NodeInfo node = ApplicationContextHolder.get().getBean(NodeInfo.class);
        if (node == null) {
            return; // Should not happen
        }
        // URL path contains the node id as the first part of the URL
        // eg. /srv/eng/catalogue.search or /srv/api/...
        String path = request.getPathInfo();
        if (path != null && path.length() > 1 && path.contains("/")) {
            String id = request.getPathInfo().split("/")[1];
            node.setId(NodeInfo.EXCLUDED_NODE_IDS.contains(id) ? NodeInfo.DEFAULT_NODE : id);
        } else {
            // eg. when accessing /
            node.setId(NodeInfo.DEFAULT_NODE);
        }
    }

}
