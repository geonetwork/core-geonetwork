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

package org.fao.geonet.monitor.health;

import java.util.List;

import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvestHistory_;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.kernel.harvest.Common;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.HarvesterSettingRepository;
import org.fao.geonet.repository.specification.HarvestHistorySpecs;
import org.jdom.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.yammer.metrics.core.HealthCheck;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;

/**
 * Verifies that all metadata have been correctly indexed (without errors)
 * <p/>
 * User: jeichar Date: 3/26/12 Time: 9:01 AM
 */
public class HarvestersHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() throws Exception {
                final HarvesterSettingRepository repository = context.getBean(HarvesterSettingRepository.class);
                final HarvestManager harvestManager = context.getBean(HarvestManager.class);
                final HarvestHistoryRepository historyRepository = context.getBean(HarvestHistoryRepository.class);
                StringBuilder errors = new StringBuilder();

                final List<HarvesterSetting> nodes = repository.findAllByPath("harvesting/node/site/uuid");

                for (HarvesterSetting node : nodes) {
                    final String harvestUuid = node.getValue();
                    final AbstractHarvester harvester = harvestManager.getHarvester(harvestUuid);
                    if (harvester.getStatus() == Common.Status.ACTIVE) {
                        Specification<HarvestHistory> spec = HarvestHistorySpecs.hasHarvesterUuid(harvestUuid);
                        final PageRequest pageRequest = new PageRequest(0, 1, Sort.Direction.DESC, HarvestHistory_.harvestDate.getName());
                        final Page<HarvestHistory> historyPage = historyRepository.findAll(spec, pageRequest);
                        if (!historyPage.getContent().isEmpty()) {
                            final HarvestHistory harvestHistory = historyPage.getContent().get(0);
                            if (harvestHistory != null && harvestHistory.getInfoAsXml() != null &&
                                harvestHistory.getInfoAsXml().getChild("stack") != null) {
                                final Element stack = harvestHistory.getInfoAsXml().getChild("stack");
                                Element errorMsg = stack.getChild("message");
                                errors.append("\n  * ");
                                errors.append(harvester.getParams().getName()).append(" (").append(harvester.getType()).append(")");
                                errors.append(": ").append(errorMsg);
                            }
                        }
                    }
                }

                if (errors.length() == 0) {
                    return Result.healthy();
                } else {
                    return Result.unhealthy("The following harvesters has errors during their last harvest:" + errors);
                }
            }
        };
    }
}
