package org.fao.geonet.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Lazy(value = true)
public class CasClientRedirect {

    @RequestMapping(value = "/casRedirect")
    public void redirect(final HttpServletRequest request,
            final HttpServletResponse response,
            @RequestParam(value = "service", required = false) String serviceUrl) throws IOException {
        response.sendRedirect(serviceUrl);
    }
}
