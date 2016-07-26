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

package org.fao.geonet.services.user;

import jeeves.constants.Jeeves;

import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.UserNotFoundEx;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Update the profile of logged user
 */
@Deprecated
public class InfoUpdate extends NotInReadOnlyModeService {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String surname = Util.getParam(params, Params.SURNAME);
        String name = Util.getParam(params, Params.NAME);
        String address = Util.getParam(params, Params.ADDRESS, "");
        String city = Util.getParam(params, Params.CITY, "");
        String state = Util.getParam(params, Params.STATE, "");
        String zip = Util.getParam(params, Params.ZIP, "");
        String country = Util.getParam(params, Params.COUNTRY, "");
        String email = Util.getParam(params, Params.EMAIL, "");
        String organ = Util.getParam(params, Params.ORG, "");
        String kind = Util.getParam(params, Params.KIND, "");


        // check old password
        UserSession session = context.getUserSession();
        String userId = session.getUserId();

        if (userId == null)
            throw new UserNotFoundEx(null);

        final UserRepository userRepository = context.getBean(UserRepository.class);
        final User user = userRepository.findOne(userId);

        user.setName(name)
            .setKind(kind)
            .setOrganisation(organ)
            .setSurname(surname);
        user.getPrimaryAddress()
            .setAddress(address)
            .setCity(city)
            .setCountry(country)
            .setState(state)
            .setZip(zip);
        user.getEmailAddresses().clear();
        user.getEmailAddresses().add(email);

        userRepository.save(user);

        return new Element(Jeeves.Elem.RESPONSE);
    }
}
