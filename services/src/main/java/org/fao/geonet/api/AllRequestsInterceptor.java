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

package org.fao.geonet.api;

import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * In charge of creating a new {@link UserSession} if not existing.
 * Avoid to create any sessions for crawlers.
 */
public class AllRequestsInterceptor extends HandlerInterceptorAdapter {

    /**
     * List of bots to avoid.
     */
    private final String BOT_REGEXP_FILTER_DEFAULT =
        ".*(bot|crawler|baiduspider|80legs|ia_archiver|voyager|yahoo! slurp|mediapartners-google|Linguee Bot|SemrushBot|heritrix).*";

    @Value("${bot.regexpFilter}")
    public String botRegexpFilter = null;

    private Pattern regex = null;

    @PostConstruct
    private void initBotRegexpFilter() {
        // Check for null or cases where maven resource filter (@bot.regexpFilter@) was not evaluated as it was missing.
        if (botRegexpFilter == null || "".equals(botRegexpFilter) || "@bot.regexpFilter@".equals(botRegexpFilter)) {
            botRegexpFilter = BOT_REGEXP_FILTER_DEFAULT;
        }
        regex = Pattern.compile(botRegexpFilter, Pattern.CASE_INSENSITIVE);
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        createSessionForAllButNotCrawlers(request);
        return super.preHandle(request, response, handler);
    }

    /**
     * Create the {@link UserSession} and add it to the HttpSession.
     * <p>
     * If a crawler, check that session is null and if not, invalidate it.
     */
    private void createSessionForAllButNotCrawlers(HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");

        if (!isCrawler(userAgent)) {
            final HttpSession httpSession = request.getSession(true);
            UserSession session = (UserSession) httpSession.getAttribute(Jeeves.Elem.SESSION);
            if (session == null) {
                session = new UserSession();

                httpSession.setAttribute(Jeeves.Elem.SESSION, session);
              //  session.setsHttpSession(httpSession);

                if (Log.isDebugEnabled(Log.REQUEST)) {
                    Log.debug(Log.REQUEST, "Session created for client : " + request.getRemoteAddr());
                }
            }
        } else {
            HttpSession httpSession = request.getSession(false);
            if (Log.isDebugEnabled(Log.REQUEST)) {
                Log.debug(Log.REQUEST, String.format(
                    "Crawler '%s' detected. Session MUST be null: %s",
                    userAgent,
                    request.getSession(false) == null
                ));
            }
            if (httpSession != null) {
                httpSession.invalidate();
            }
        }
    }

    public boolean isCrawler(String userAgent) {
        if (StringUtils.isNotBlank(userAgent)) {
            Matcher m = regex.matcher(userAgent);
            return m.find();
        }
        return false;
    }

    public String getBotRegexpFilter() {
        return botRegexpFilter;
    }

    public void setBotRegexpFilter(String botRegexpFilter) {
        this.botRegexpFilter = botRegexpFilter;
        initBotRegexpFilter();
    }
}
