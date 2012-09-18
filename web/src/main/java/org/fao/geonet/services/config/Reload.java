//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.config;

import jeeves.interfaces.Logger;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.Producer;
import org.fao.geonet.jms.message.reindex.ReloadLuceneConfigMessage;
import org.fao.geonet.kernel.csw.CatalogDispatcher;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;

import javax.servlet.ServletContext;

public class Reload implements Service {

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		String status = "true";
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		ServiceConfig handlerConfig = gc.getHandlerConfig();
		String luceneConfigXmlFile = handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_CONFIG);
		String path = context.getAppPath();

        ServletContext servletContext = null;
        if(context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }

		LuceneConfig lc = new LuceneConfig(path, servletContext, luceneConfigXmlFile);

		// Update related services to Lucene config
		// SearchManager
		SearchManager sm = gc.getSearchmanager();
		sm.reloadLuceneConfiguration(lc);

		// CatalogueDispatcher
		CatalogDispatcher cd = gc.getCatalogDispatcher();
		cd.reloadLuceneConfiguration(lc);

        if (ClusterConfig.isEnabled()) {
            ReloadLuceneConfigMessage message = new ReloadLuceneConfigMessage();
            message.setSenderClientID(ClusterConfig.getClientID());
            Producer reloadLuceneConfigProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.RELOADINDEXCONF);
            reloadLuceneConfigProducer.produce(message);
        }

		Logger logger = context.getLogger();
		logger.info("  - Lucene configuration is:");
		String config = lc.toString();
		logger.info(config);

		return new Element("response")
			.addContent(new Element("status").setText(status))
			.addContent(new Element("config").setText(config));
	}
}
