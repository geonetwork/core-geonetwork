package jeeves.config.springutil;

import org.fao.geonet.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getApplicationContextFromServletContext;

/**
 * Created by Jesse on 2/17/14.
 */
public class JeevesNodeAwareLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {
    @Autowired
    ServletContext context;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        super.handle(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        final ConfigurableApplicationContext applicationContext = getApplicationContextFromServletContext(context);

        NodeInfo nodeInfo = applicationContext.getBean(NodeInfo.class);

        String urlPattern = super.determineTargetUrl(request, response);

        return urlPattern.replace("@@nodeId@@", nodeInfo.getId());
    }
}
