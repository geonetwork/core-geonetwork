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
import java.util.StringJoiner;

import org.fao.geonet.kernel.schema.subtemplate.AbstractReplacer;
import org.fao.geonet.kernel.schema.subtemplate.ConstantsProxy;
import org.fao.geonet.kernel.schema.subtemplate.ManagersProxy;
import org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

public class ContactReplacer extends AbstractReplacer {

    public ContactReplacer(List<Namespace> namespaces,
                           ManagersProxy managersProxy,
                           ConstantsProxy constantsProxy) {
        super(namespaces, managersProxy, constantsProxy);
    }

    @Override
    public String getAlias() {
        return SubtemplatesByLocalXLinksReplacer.CONTACT;
    }

    @Override
    protected String getElemXPath() {
        return ".//gmd:CI_ResponsibleParty/parent::*|.//*[@gco:isoType=\"gmd:CI_ResponsibleParty\"]/parent::*";
    }

    @Override
    protected int queryAddExtraClauses(StringBuilder query, Element contact, String lang) throws JDOMException {
        String individualName = getFieldValue(contact, ".//gmd:individualName", lang);
        String organisationName = getFieldValue(contact, ".//gmd:organisationName", lang);
        String email = getFieldValue(contact, ".//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress", lang);

        query.append(" +root:\"gmd:CI_ResponsibleParty\"");
        query.append(String.format(" +contactIndividualNameObject.\\*:\"%s\"", individualName));
        query.append(String.format(" +contactOrgObject.\\*:\"%s\"", organisationName));
        query.append(String.format(" +contactEmailObject.\\*:\"%s\"", email));
        return 4;
    }

    @Override
    protected void xlinkAddExtraParams(Element contact, StringJoiner params) throws JDOMException {
        String roleXPath = "gmd:role/gmd:CI_RoleCode/@codeListValue";
        String roleCodeListValue = Xml.selectString(contact, ".//" + roleXPath, namespaces);
        params.add(String.format("process=%s~%s", roleXPath, roleCodeListValue));
    }
}
