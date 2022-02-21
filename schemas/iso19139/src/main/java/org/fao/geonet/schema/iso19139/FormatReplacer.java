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

package org.fao.geonet.schema.iso19139;

import java.util.List;

import org.fao.geonet.kernel.schema.subtemplate.AbstractReplacer;
import org.fao.geonet.kernel.schema.subtemplate.ConstantsProxy;
import org.fao.geonet.kernel.schema.subtemplate.ManagersProxy;
import org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

public class FormatReplacer extends AbstractReplacer {

    public FormatReplacer(List<Namespace> namespaces,
                          ManagersProxy managersProxy,
                          ConstantsProxy constantsProxy) {
        super(namespaces, managersProxy, constantsProxy);
    }

    @Override
    public String getAlias() {
        return SubtemplatesByLocalXLinksReplacer.FORMAT;
    }

    @Override
    protected String getElemXPath() {
        return ".//gmd:MD_Format/parent::*";
    }

    @Override
    protected int queryAddExtraClauses(StringBuilder query, Element format, String lang) throws JDOMException {
        String name = getFieldValue(format, ".//gmd:name", lang);
        String version = getFieldValue(format, ".//gmd:version", lang);

        query.append(String.format(" +formatName:\"%s\"", name));
        query.append(String.format(" +formatVersion:\"%s\"", version));
        query.append(" +root:\"gmd:MD_Format\"");
        return 3;
    }
}
