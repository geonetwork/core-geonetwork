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
public class JeevesDispatcherServlet extends DispatcherServlet {

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
