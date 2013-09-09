//=============================================================================
//===   Copyright (C) 2001-2010 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.notifier;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;

import javax.transaction.TransactionManager;

public class MetadataNotifierTask implements Runnable {
    private ServiceContext context;
    private GeonetContext gc;

    public MetadataNotifierTask(ServiceContext context, GeonetContext gc) {
        this.context = context;
        this.gc = gc;
    }

    public void run() {
        try {
            final TransactionManager manager = context.getBean(TransactionManager.class);
            manager.begin();
            try {
                gc.getBean(MetadataNotifierManager.class).updateMetadataBatch(context, gc);
                manager.commit();
            } catch (Exception x) {
                manager.rollback();
                System.out.println(x.getMessage());
                x.printStackTrace();
            }
        } catch (Exception x) {
            System.out.println(x.getMessage());
            x.printStackTrace();
        }
    }
}
