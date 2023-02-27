package org.fao.geonet.api.es.queryrewrite;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface ESQueryRewriter {

    String rewriteQuery(HttpSession httpSession, HttpServletRequest request, String jsonESQuery);

}
