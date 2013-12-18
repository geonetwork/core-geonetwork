package org.fao.geonet.web;

import jeeves.constants.Jeeves;
import jeeves.server.overrides.ConfigurationOverrides;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import static org.fao.geonet.constants.Geonet.DEFAULT_LANGUAGE;

/**
 * Handles requests where there is no locale and a redirect to a correct (and localized) service is needed.  For example
 * index.html redirects to /srv/eng/home but that redirect should depend on the language of the users browser.
 * <p/>
 * Created by Jesse on 12/4/13.
 */
@Controller
@Lazy(value = false)
public class LocaleRedirects {

    @Autowired
    private ApplicationContext _appContext;

    private String _homeRedirectUrl;

    private String _defaultLanguage;

    @RequestMapping(value = "/home")
    public ModelAndView home(final HttpServletRequest request,
                             @CookieValue(value = Jeeves.LANG_COOKIE, required = false) String langCookie,
                             @RequestParam(value = "hl", required = false) String langParam,
                             @RequestHeader(value = "Accept-Language", required = false) final String langHeader) {
        String lang = lang(langParam, langCookie, langHeader);
        return redirectURL(createServiceUrl(request, _homeRedirectUrl, lang));
    }

    @RequestMapping(value = "/login.jsp")
    public ModelAndView login(final HttpServletRequest request,
                              @RequestParam(value = "hl", required = false) String langParam,
                              @CookieValue(value = Jeeves.LANG_COOKIE, required = false) String langCookie,
                              @RequestHeader(value = "Accept-Language", required = false) final String langHeader) {
        String lang = lang(langParam, langCookie, langHeader);
        return redirectURL(createServiceUrl(request, "login.form", lang));
    }

    @RequestMapping(value = "/accessDenied.jsp")
    public ModelAndView accessDenied(final HttpServletRequest request,
                                     @RequestParam(value = "hl", required = false) String langParam,
                                     @CookieValue(value = Jeeves.LANG_COOKIE, required = false) String langCookie,
                                     @RequestParam(value = "referer", required = false) String referer,
                                     @RequestHeader(value = "Accept-Language", required = false) final String langHeader) {
        String lang = lang(langParam, langCookie, langHeader);
        if (referer == null || referer.trim().isEmpty() ||
            referer.contains("accessDenied") || referer.contains("service-not-allowed")) {
            referer = "UNKNOWN";
        }
        return redirectURL(createServiceUrl(request, "service-not-allowed?referer=" + referer, lang));
    }

    private ModelAndView redirectURL(final String url) {

        RedirectView rv = new RedirectView(url);
        rv.setStatusCode(HttpStatus.MOVED_TEMPORARILY);
        ModelAndView mv = new ModelAndView(rv);
        return mv;
    }

    private String createServiceUrl(HttpServletRequest request, String service, String lang) {
        return request.getContextPath() + "/srv/" + lang + "/" + service;
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
