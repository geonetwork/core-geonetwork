//=============================================================================
//===  Copyright (C) 2009 World Meteorological Organization
//===  This program is free software; you can redistribute it and/or modify
//===  it under the terms of the GNU General Public License as published by
//===  the Free Software Foundation; either version 2 of the License, or (at
//===  your option) any later version.
//===
//===  This program is distributed in the hope that it will be useful, but
//===  WITHOUT ANY WARRANTY; without even the implied warranty of
//===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===  General Public License for more details.
//===
//===  You should have received a copy of the GNU General Public License
//===  along with this program; if not, write to the Free Software
//===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===  Contact: Timo Proescholdt
//===  email: tproescholdt_at_wmo.int
//==============================================================================

package org.fao.geonet;

import jeeves.server.context.ServiceContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This class is a gateway between the Spring Application Context and the other contexts of
 * Geonetwork It is used to store references to the different contexts used in geonetwork. A
 * reference to it will be distributed via the Spring ApplicationContext
 *
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 */
public class ContextContainer implements ApplicationContextAware {

    /** Shared service context for GeoNetwork application */
    private ServiceContext srvctx;

    /** Shared application context provided during initialization */
    private ApplicationContext ctx;

    public ContextContainer() {


    }

    /**
     * Service context shared for GeoNetwork application.
     *
     * This is an application wide service context with limited functionality, no user session for example.
     *
     * Use of {@link ServiceContext#get()} is recommended in most situations for access
     * to current user session.
     *
     * @return Shared service context for GeoNetwork application
     */
    public ServiceContext getSrvctx() {
        return srvctx;
    }

    /**
     * Service context shared for GeoNetwork application.
     *
     * @param srvctx Shared app handler service context for GeoNetwork application
     */
    public void setSrvctx(ServiceContext srvctx) {
        this.srvctx = srvctx;
    }

    /**
     * GeoNetwork application context provided during initialization.
     *
     * @return geonetwork application context
     */
    public ApplicationContext getApplicationContext() {
        if (ctx == null) throw new RuntimeException("applicationcontext not yet initialized");
        return ctx;
    }

    /**
     * GeoNetwork application context provided during initialization.
     * @param arg0
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext arg0)
        throws BeansException {

        ctx = arg0;
    }


}

