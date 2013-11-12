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

package org.fao.geonet.services.util.z3950;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.jzkit.ServiceDirectory.CollectionDescriptionDBO;
import org.jzkit.ServiceDirectory.SearchServiceDescriptionDBO;
import org.jzkit.configuration.api.Configuration;
import org.jzkit.configuration.api.ConfigurationException;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * helperclass to get a list of remote searchable collections from the 
 * repositories in the JZkit configuration
 *
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 * @author 'Simon Pigot'
 *
 */
public class RepositoryInfo {
       
       private String dn;
       private String name;
			 private String code;
			 private String serverCode;
			 private String classname;
       
       private RepositoryInfo(String dn, String name, String code, String serverCode, String classname) {
               this.name=name;
               this.dn=dn;
							 this.code=code;
							 this.serverCode=serverCode;
							 this.classname = classname;
       }
       
       public String getDn() {
               return dn;
       }

       public String getName() {
               return name;
       }
       
       public String getCode() {
               return code;
       }
       
       public String getServerCode() {
               return serverCode;
       }
       
       public String getClassName() {
               return classname;
       }
       
       public String toString() {
               return getName()+":"+getDn()+":"+getCode()+":"+getServerCode()+":"+getClassName();
       }
       
       
	/**
	 * returns the list of repositories that are configured in JZkit
   * @param srvContext
   * @return
   * @throws org.jzkit.configuration.api.ConfigurationException
   */
	public static Collection<RepositoryInfo> getRepositories(ServiceContext srvContext) throws ConfigurationException {

   	GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
		ApplicationContext app_context = gc.getApplicationContext();
		Vector<RepositoryInfo> ret = new Vector<RepositoryInfo>();
		
		if (app_context != null) {
			Configuration conf = (Configuration)app_context.getBean("JZKitConfig");
			@SuppressWarnings("unchecked")
            Iterator<SearchServiceDescriptionDBO> it = conf.enumerateRepositories();
               
			while (it.hasNext()) {
	   		SearchServiceDescriptionDBO ssd = it.next();
	     	Collection<CollectionDescriptionDBO> col = ssd.getCollections();
				if (col.size()>0) {
					Iterator<CollectionDescriptionDBO> colit = col.iterator();
                    if(Log.isDebugEnabled(Geonet.Z3950))
                        Log.debug(Geonet.Z3950, "Service "+ssd.getServiceName()+" has "+col.size()+" collections "+colit.hasNext());
					while (colit.hasNext()) {
						CollectionDescriptionDBO oneCol = colit.next();
                        if(Log.isDebugEnabled(Geonet.Z3950))
                            Log.debug(Geonet.Z3950, "Adding collection "+oneCol.getCode()+":"+oneCol.getCollectionName()+":"+oneCol.getLocalId()+":"+ssd.getCode());
						ret.add( new RepositoryInfo(  oneCol.getCode() , oneCol.getCollectionName(), oneCol.getLocalId(), ssd.getCode(), ssd.getClassName()) ) ;                               
					}
				}
			}
		}
               
		return ret;
	}

}
