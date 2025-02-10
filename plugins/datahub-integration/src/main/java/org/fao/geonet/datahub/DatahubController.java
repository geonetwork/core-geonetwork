package org.fao.geonet.datahub;

import org.fao.geonet.utils.Log;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.fao.geonet.kernel.schema.SchemaPlugin.LOGGER_NAME;
@RequestMapping(value = {
    "/{path:[a-zA-Z0-9_\\-]+}"
})
@Controller("datahub")
public class DatahubController {

    @RequestMapping("/datahub")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void handleDatahub(HttpServletRequest request, HttpServletResponse response) {
        Log.debug(LOGGER_NAME, "enter in /[a-zA-Z0-9_\\-]+/datahub");
    }

    @RequestMapping("/{locale:[a-z]{2,3}}/datahub")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void handleLocalizedDatahub(HttpServletRequest request, HttpServletResponse response) {
        Log.debug(LOGGER_NAME, "enter in /[a-zA-Z0-9_\\-]+/[a-z]{2,3}/datahub");
    }
}
