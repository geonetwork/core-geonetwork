package org.fao.geonet.services.system;

import com.vividsolutions.jts.util.Assert;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.domain.responses.OkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Update the writable information in {@link org.fao.geonet.SystemInfo}.
 * @author Jesse on 1/23/2015.
 */
@Controller("systeminfo/")
public class UpdateSystemInfo {
    @Autowired
    private SystemInfo info;

    @RequestMapping(value = "/{lang}/systeminfo/staging", produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public OkResponse update(@RequestParam(required = true) String newProfile) {
        Assert.isTrue(!newProfile.isEmpty(), "newProfile must not be an empty string");
        this.info.setStagingProfile(newProfile);
        return new OkResponse();
    }
}
