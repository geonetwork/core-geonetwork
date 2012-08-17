package jeeves.config.springutil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jeeves.server.UserSession;
import jeeves.server.sources.http.JeevesServlet;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * Clears the UserSession
 * 
 * @author jeichar
 */
public class LogoutUserSessionHandler implements LogoutHandler {

	@Override
	public void logout(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication) {
		HttpSession httpSession = request.getSession(false);
		if(httpSession != null) {
			Object tmp = httpSession.getAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY);
			if (tmp instanceof UserSession) {
				UserSession userSession = (UserSession) tmp;
				userSession.clear();
			}
		}

	}

}
