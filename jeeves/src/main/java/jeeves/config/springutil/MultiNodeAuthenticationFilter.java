package jeeves.config.springutil;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import jeeves.constants.Jeeves;
import jeeves.server.sources.ServiceRequestFactory;
import jeeves.server.sources.http.JeevesServlet;
import org.fao.geonet.Constants;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.User;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getApplicationContextFromServletContext;

/**
 * This filter is designed to ensure that users logged in one node is not logged in in the others.
 * <p/>
 * User: Jesse
 * Date: 11/26/13
 * Time: 7:24 AM
 */
public class MultiNodeAuthenticationFilter extends GenericFilterBean {
    private String _location;

    public void setLocation(String location) {
        this._location = location;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            final Authentication user = context.getAuthentication();

            if (user != null) {
                final ConfigurableApplicationContext appContext = getApplicationContextFromServletContext(getServletContext());
                final String nodeId = appContext.getBean(NodeInfo.class).getId();

                final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

                final String lang = getRequestLanguage(appContext, httpServletRequest);
                final String redirectedFrom = httpServletRequest.getRequestURI();

                String oldNodeId = null;

                for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
                    String authName = grantedAuthority.getAuthority();
                    if (authName.startsWith(User.NODE_APPLICATION_CONTEXT_KEY)) {
                        oldNodeId = authName.substring(User.NODE_APPLICATION_CONTEXT_KEY.length());
                        break;
                    }
                }

                if (getServletContext().getAttribute(User.NODE_APPLICATION_CONTEXT_KEY + oldNodeId) == null) {
                    // the application context associated with the node id doesn't exist so log user out.
                    SecurityContextHolder.clearContext();
                } else if (_location != null) {
                    if (oldNodeId != null && !oldNodeId.equals(nodeId)) {
                        final Escaper escaper = UrlEscapers.urlFormParameterEscaper();
                        final String location = getServletContext().getContextPath() + _location.replace("@@lang@@", escaper.escape(lang))
                                .replace("@@nodeId@@", escaper.escape(nodeId))
                                .replace("@@redirectedFrom@@", escaper.escape(redirectedFrom))
                                .replace("@@oldNodeId@@", escaper.escape(oldNodeId))
                                .replace("@@oldUserName@@", escaper.escape(user.getName()));

                        String requestURI = httpServletRequest.getRequestURI();
                        // drop the ! at the end so we can view the xml of the warning page
                        if (requestURI.endsWith("!")) {
                            requestURI = requestURI.substring(0, requestURI.length() - 1);
                        }
                        final boolean isNodeWarningPage = requestURI.equals(location.split("\\?")[0]);
                        if (!isNodeWarningPage) {
                                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                                httpServletResponse.sendRedirect(httpServletResponse.encodeRedirectURL(location));
                                return;
                        }
                    }
                } else {
                    throwAuthError();
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String getRequestLanguage(ConfigurableApplicationContext appContext, HttpServletRequest request) {
        String language = request.getParameter("hl");
        if (language == null) {
            final Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(Jeeves.LANG_COOKIE)) {
                    language = cookie.getValue();
                    break;
                }
            }
        }

        if (language == null) {
            String pathInfo = request.getPathInfo();
            language = ServiceRequestFactory.extractLanguage(pathInfo);
        }

        if (language == null) {
            language = appContext.getBean("defaultLanguage", String.class);
        }
        if (language == null) {
            language = "eng";
        }
        return language;
    }

    private void throwAuthError() {
        throw new WrongNodeAuthenticationException(
                "The current user was logged into a different node.  " +
                "To login to this node the user must logout and login to the new node.  " +
                "It is also possible to return to the old node.");
    }
}
