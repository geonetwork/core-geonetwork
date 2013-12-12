package jeeves.config.springutil;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles Access Denied exceptions during spring security.
 *
 * Created by Jesse on 12/5/13.
 */
public class JeevesAccessDeniedHandler implements AccessDeniedHandler {

    private String _errorPage;
    private Escaper _escaper = UrlEscapers.urlPathSegmentEscaper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws
            IOException, ServletException {
        if (!response.isCommitted()) {
            if (_errorPage != null) {
                request.setAttribute(WebAttributes.ACCESS_DENIED_403, accessDeniedException);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                final String referer = _escaper.escape(request.getRequestURI());
                RequestDispatcher dispatcher = request.getRequestDispatcher(_errorPage +"?referer="+ referer);
                dispatcher.forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
            }
        }
    }

    public void setErrorPage(String errorPage) {
        this._errorPage = errorPage;
    }
}
