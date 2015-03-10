//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

import jeeves.config.springutil.ServerBeanPropertyUpdater;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.statistic.PathSpec;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

//=============================================================================

/**
 * TODO javadoc.
 */
public class Set implements Service {
    /**
     * Reload services or not once settings are updated. Some service use DoAction as forward service.
     */
    private boolean reloadServices = false;

    public void init(java.nio.file.Path appPath, ServiceConfig params) throws Exception {
        reloadServices = "y".equalsIgnoreCase(params.getValue("reloadServices", "n"));
    }

    /**
     * TODO javadoc.
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        Map<String, String> values = new HashMap<String, String>();
        for (Object obj : params.getChildren()) {
            if (obj instanceof Element) {
                Element param = (Element) obj;
                values.put(param.getName().replace('.', '/'), param.getValue());
            }
        }

        String currentUuid = sm.getSiteId();

        if (!sm.setValues(values))
            throw new OperationAbortedEx("Cannot set all values");

        // And reload services
        String newUuid = values.get(SettingManager.SYSTEM_SITE_SITE_ID_PATH);

        if (newUuid != null && !currentUuid.equals(newUuid)) {
            final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
            final SourceRepository sourceRepository = context.getBean(SourceRepository.class);
            final Source source = sourceRepository.findOne(currentUuid);
            Source newSource = new Source(newUuid, source.getName(), source.getLabelTranslations(), source.isLocal());
            sourceRepository.save(newSource);

            PathSpec<Metadata, String> servicesPath = new PathSpec<Metadata, String>() {
                @Override
                public Path<String> getPath(Root<Metadata> root) {
                    return root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.sourceId);
                }
            };
            metadataRepository.createBatchUpdateQuery(servicesPath, newUuid, MetadataSpecs.isHarvested(false));

            sourceRepository.delete(source);
        }

        SettingInfo info = context.getBean(SettingInfo.class);
        ServerBeanPropertyUpdater.updateURL(info.getSiteUrl(true) + context.getBaseUrl(), context.getApplicationContext());

        // Reload services affected by updated settings
        if (reloadServices) {
            DoActions.doActions(context);
        }

        return new Element(Jeeves.Elem.RESPONSE).setText("ok");
    }
}
