/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect.bearer;

import org.fao.geonet.kernel.security.openidconnect.OIDCConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SubjectAccessTokenValidatorTest {

    String userName = "MYUSERNAME";


    public  SubjectAccessTokenValidator   getValidator(){
        SubjectAccessTokenValidator validator = new SubjectAccessTokenValidator();
        return validator;
    }

    @Test
    public void testAzureGood() throws  Exception {
        Map jwt = new HashMap();
        Map userInfo = new HashMap();

        jwt.put("sub",userName);
        userInfo.put("sub",userName);

        SubjectAccessTokenValidator validator = getValidator();
        validator.verifyToken(jwt,userInfo);
    }

    @Test
    public void testKeyCloakGood() throws  Exception {
        Map jwt = new HashMap();
        Map xms_st = new HashMap();

        Map userInfo = new HashMap();

        xms_st.put("sub",userName);
        jwt.put("xms_st",xms_st);
        userInfo.put("sub",userName);

        SubjectAccessTokenValidator validator = getValidator();
        validator.verifyToken(jwt,userInfo);
    }

    @Test(expected=Exception.class)
    public void testbad1() throws  Exception {
        Map jwt = new HashMap();
        Map userInfo = new HashMap();

        SubjectAccessTokenValidator validator = getValidator();
        validator.verifyToken(jwt,userInfo);
    }


    @Test(expected=Exception.class)
    public void testbad2() throws  Exception {
        Map jwt = new HashMap();
        Map xms_st = new HashMap();

        Map userInfo = new HashMap();

        xms_st.put("sub","baduser");
        jwt.put("xms_st",xms_st);
        userInfo.put("sub",userName);


        SubjectAccessTokenValidator validator = getValidator();
        validator.verifyToken(jwt,userInfo);
    }


}
