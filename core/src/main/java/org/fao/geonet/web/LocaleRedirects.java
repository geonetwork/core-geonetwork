package org.fao.geonet.web;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.constants.Jeeves;
import jeeves.server.overrides.ConfigurationOverrides;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Handles requests where there is no locale and a redirect to a correct (and localized) service is needed.  For example
 * index.html redirects to /srv/eng/home but that redirect should depend on the language of the users browser.
 * <p/>
 * Created by Jesse on 12/4/13.
 */
@Controller
@Lazy(value = true)
public class LocaleRedirects {


    private static final String LANG_PARAMETER = "hl";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    private static final String REFERER_PARAMETER = "referer";
    private static final String NODE_PARAMETER = "node";

    private static final Set<String> SPECIAL_HEADERS;

    static {
        HashSet<String> headers = new HashSet<String>();
        headers.add(LANG_PARAMETER);
        headers.add(REFERER_PARAMETER);
        SPECIAL_HEADERS = Collections.unmodifiableSet(headers);
    }

    @Autowired
    private ApplicationContext _appContext;

    private String _homeRedirectUrl;

    private String _defaultLanguage;

    @RequestMapping(value = "/home")
    public ModelAndView home(final HttpServletRequest request,
                             @CookieValue(value = Jeeves.LANG_COOKIE, required = false) String langCookie,
                             @RequestParam(value = LANG_PARAMETER, required = false) String langParam,
                             @RequestParam(value = NODE_PARAMETER, required = false) String node,
                             @RequestHeader(value = ACCEPT_LANGUAGE_HEADER, required = false) final String langHeader) {
        String lang = lang(langParam, langCookie, langHeader);
        return redirectURL(createServiceUrl(request, _homeRedirectUrl, lang, node));
    }

    @RequestMapping(value = "/login.jsp")
    public ModelAndView login(final HttpServletRequest request,
                              @RequestParam(value = LANG_PARAMETER, required = false) String langParam,
                              @RequestParam(value = NODE_PARAMETER, required = false) String node,
                              @CookieValue(value = Jeeves.LANG_COOKIE, required = false) String langCookie,
                              @RequestHeader(value = ACCEPT_LANGUAGE_HEADER, required = false) final String langHeader) {
        String lang = lang(langParam, langCookie, langHeader);
        return redirectURL(createServiceUrl(request, "catalog.signin", lang, node));
    }

    @RequestMapping(value = "/accessDenied.jsp")
    public ModelAndView accessDenied(final HttpServletRequest request,
                                     @RequestParam(value = LANG_PARAMETER, required = false) String langParam,
                                     @RequestParam(value = NODE_PARAMETER, required = false) String node,
                                     @CookieValue(value = Jeeves.LANG_COOKIE, required = false) String langCookie,
                                     @RequestParam(value = REFERER_PARAMETER, required = false) String referer,
                                     @RequestHeader(value = ACCEPT_LANGUAGE_HEADER, required = false) final String langHeader) {
        String lang = lang(langParam, langCookie, langHeader);
        if (referer == null || referer.trim().isEmpty() ||
            referer.contains("accessDenied") || referer.contains("service-not-allowed")) {
            referer = "UNKNOWN";
        }
        return redirectURL(createServiceUrl(request, "service-not-allowed?referer=" + referer, lang, node));
    }

    private ModelAndView redirectURL(final String url) {

        RedirectView rv = new RedirectView(url);
        rv.setStatusCode(HttpStatus.FOUND);
        return new ModelAndView(rv);
    }

    private String createServiceUrl(HttpServletRequest request, String service, String lang, String node) {
        ConfigurableApplicationContext context = JeevesDelegatingFilterProxy.getApplicationContextFromServletContext(_appContext.getBean(ServletContext.class));
        String currentNode = context.getBean(NodeInfo.class).getId();

        node = node == null ? currentNode : node;

        final Enumeration parameterNames = request.getParameterNames();
        StringBuilder headers = new StringBuilder();
        while (parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            if (!SPECIAL_HEADERS.contains(paramName)) {
                for (String value : request.getParameterValues(paramName)) {
                    if (headers.length() > 0) {
                        headers.append('&');
                    }
                    headers.append(paramName);
                    if (!value.isEmpty()){
                        headers.append('=').append(value);
                    }
                }
            }
        }
        final String queryString;
        if (headers.length() == 0) {
            queryString = "";
        } else {
            if (service.contains("?")) {
                queryString = "&" + headers;
            } else {
                queryString = "?" + headers;
            }
        }
        return request.getContextPath() + "/" + node + "/" + lang + "/" + service + queryString;
    }

    private String lang(String langParam, String langCookie, String langHeader) {

        if (langParam != null) {
            return langParam;
        }
        if (langCookie != null) {
            return langCookie;
        }
        if (langHeader == null) {
            return _defaultLanguage;
        }

        String userLang = langHeader.split("-|,", 2)[0].toLowerCase();

        if (userLang.matches("^en")) {
            userLang = "eng";
        } else if (userLang.matches("^fr")) {
            userLang = "fre";
        } else if (userLang.matches("^de")) {
            userLang = "ger";
        } else if (userLang.matches("^it")) {
            userLang = "ita";
        } else if (userLang.matches("^ca")) {
            userLang = "cat";
        } else if (userLang.matches("^es")) {
            userLang = "spa";
        } else if (userLang.matches("^fi")) {
            userLang = "fin";
        } else if (userLang.matches("^pl")) {
            userLang = "pol";
        } else if (userLang.matches("^no")) {
            userLang = "nor";
        } else if (userLang.matches("^nl")) {
            userLang = "dut";
        } else if (userLang.matches("^pt")) {
            userLang = "por";
        } else if (userLang.matches("^ar")) {
            userLang = "ara";
        } else if (userLang.matches("^zh")) {
            userLang = "chi";
        } else if (userLang.matches("^ru")) {
            userLang = "rus";
        } else if (userLang.matches("^tr")) {
            userLang = "tur";
        } else {
            userLang = _defaultLanguage;
        }

        return userLang;
    }

    @PostConstruct
    public void init() throws BeansException {
        final String configPath = _appContext.getBean("configPath", String.class);
        final ServletContext servletContext = _appContext.getBean(ServletContext.class);
        try {
            final Element guiConfig = ConfigurationOverrides.DEFAULT.loadXmlFileAndUpdate(configPath + "config-gui.xml", servletContext);
            final String xpath = "client/@url";
            this._homeRedirectUrl = Xml.selectString(guiConfig, xpath);
            if (_homeRedirectUrl == null) {
                throw new FatalBeanException("No redirect URL was found in " + configPath + "config-gui.xml" + " at xpath: " + xpath);
            }
        } catch (Exception e) {
            throw new FatalBeanException("Error loading guiConfig: " + configPath + "config-gui.xml", e);
        }

        try {
            final Element config = ConfigurationOverrides.DEFAULT.loadXmlFileAndUpdate(configPath + "config.xml", servletContext);
            final String xpath = "default/language";
            this._defaultLanguage = Xml.selectString(config, xpath);
            if (_defaultLanguage == null) {
                _defaultLanguage = Geonet.DEFAULT_LANGUAGE;
            }
        } catch (Exception e) {
            throw new FatalBeanException("Error loading config.xml: " + configPath + "config-gui.xml", e);
        }


    }
}
