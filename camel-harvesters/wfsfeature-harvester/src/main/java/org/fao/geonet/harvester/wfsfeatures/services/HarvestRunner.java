package org.fao.geonet.harvester.wfsfeatures.services;

import org.fao.geonet.harvester.wfsfeatures.event.WfsIndexingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Created by fgravin on 10/29/15.
 */

@Controller
public class HarvestRunner {

    @Autowired
    private ApplicationContext appContext;

    @RequestMapping(value = "/{uiLang}/wfs.harvest")
    @ResponseBody
    public String localServiceDescribe(
            @RequestParam String url,
            NativeWebRequest webRequest) throws Exception {

        WfsIndexingEvent event = new WfsIndexingEvent(appContext, java.net.URLDecoder.decode(url));
        appContext.publishEvent(event);

        return url;
    }
}
