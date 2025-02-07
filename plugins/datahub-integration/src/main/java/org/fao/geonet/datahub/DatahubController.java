package org.fao.geonet.datahub;

import org.fao.geonet.utils.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.fao.geonet.kernel.schema.SchemaPlugin.LOGGER_NAME;


@Controller
public class DatahubController {



    @RequestMapping("/srv/datahub")
    public void defaultsubRoot4(HttpServletRequest request, HttpServletResponse response)  {
        Log.debug(LOGGER_NAME,"enter in /srv/datahub");
    }
}
