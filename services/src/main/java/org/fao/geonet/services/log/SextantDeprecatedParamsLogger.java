package org.fao.geonet.services.log;

import org.fao.geonet.constants.Geonet;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
public class SextantDeprecatedParamsLogger {
    static private class DeprecatedParams {
        private String location;
        private String wmsurl = "";
        private String layername = "";
        private String owscontext = "";

        public DeprecatedParams() { }

        public String getLocation() {
            return location;
        }
        public String getWmsurl() {
            return wmsurl;
        }
        public String getLayername() {
            return layername;
        }
        public String getOwscontext() {
            return owscontext;
        }
        public void setLocation(String location) {
            this.location = location;
        }
        public void setWmsurl(String wmsurl) {
            this.wmsurl = wmsurl;
        }
        public void setLayername(String layername) {
            this.layername = layername;
        }
        public void setOwscontext(String owscontext) {
            this.owscontext = owscontext;
        }
    }

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Geonet.GEONETWORK);

    @RequestMapping(
            value = "/deprecated.params.log",
            method = RequestMethod.POST,
            consumes = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String exec(@RequestBody DeprecatedParams values, HttpServletRequest request) {
        StringBuilder message = new StringBuilder(
                String.format("[SEXTANT] Usage of deprecated url params was notified (request host: %s)",
                        request.getRemoteHost()))
                .append(String.format("\n    - location: %s", values.location));

        if (!values.owscontext.isEmpty()) {
            message.append(String.format("\n    - owscontext: %s", values.owscontext));
        }
        if (!values.wmsurl.isEmpty() || !values.layername.isEmpty()) {
            message.append(String.format("\n    - wmsurl: %s", values.wmsurl));
            message.append(String.format("\n    - layername: %s", values.layername));
        }

        LOGGER.warn(message.toString());

        return "Logged, thanks!";
    }
}
