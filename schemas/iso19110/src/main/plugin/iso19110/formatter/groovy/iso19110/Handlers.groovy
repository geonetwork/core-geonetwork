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

package iso19110

import org.fao.geonet.api.records.formatters.groovy.Environment
import org.fao.geonet.api.records.formatters.groovy.Functions
import org.fao.geonet.api.records.formatters.groovy.util.NavBarItem
import org.fao.geonet.api.records.formatters.groovy.util.Summary

/**
 * @author Jesse on 6/24/2015.
 */
class Handlers extends iso19139.Handlers {
    def iso19139RootPackageEl;

    public Handlers(org.fao.geonet.api.records.formatters.groovy.Handlers handlers, Functions f, Environment env) {
        super(handlers, f, env);
        rootEl = 'gfc:FC_FeatureCatalogue'
        packageViews = [rootEl]
        iso19139RootPackageEl = super.rootPackageEl
        rootPackageEl = iso19100RootPackageEl
    }

    private def iso19100RootPackageEl = { el ->
        Summary summary = new Summary(handlers, env, f);

        summary.title = isofunc.isoText(el['gmx:name'])
        summary.abstr = ''
        summary.content = iso19139RootPackageEl(el)
        summary.addNavBarItem(new NavBarItem(f.translate('complete'), null, '.container > .entry:not(.overview)'))
        summary.addNavBarItem(commonHandlers.createXmlNavBarItem())
        summary.addCompleteNavItem = false
        summary.addOverviewNavItem = false

        summary.result
    }

}
