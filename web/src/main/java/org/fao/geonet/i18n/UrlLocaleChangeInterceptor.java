package org.fao.geonet.i18n;

import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Strongly based on LocaleChangeInterceptor from Spring
 * 
 * @author delawen
 * 
 */
public class UrlLocaleChangeInterceptor extends HandlerInterceptorAdapter {
	public static final Integer DEFAULT_URL_POSITION = 0;
	private Integer urlPosition = DEFAULT_URL_POSITION;

	public void setUrlPosition(Integer p) {
		this.urlPosition = p;
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler)
			throws ServletException {

		String url = request.getRequestURI();
		String[] path = url.split("/");
		String newLocale = null;
		Integer position = urlPosition;

		if (path.length >= position) {
			newLocale = path[position];
			if (newLocale != null) {
				LocaleResolver localeResolver = RequestContextUtils
						.getLocaleResolver(request);
				if (localeResolver == null) {
					throw new IllegalStateException(
							"No LocaleResolver found: not in a DispatcherServlet request?");
				}
				LocaleEditor localeEditor = new LocaleEditor();
				localeEditor.setAsText(newLocale);
				localeResolver.setLocale(request, response,
						(Locale) localeEditor.getValue());
			}
		}

		// Proceed in any case.
		return true;
	}

}
