package jeeves.config.springutil;

import org.fao.geonet.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getApplicationContextFromServletContext;

/**
 * Created by Jesse on 2/17/14.
 */
public class JeevesNodeAwareLoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {
    @Autowired
    ServletContext context;

    public JeevesNodeAwareLoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        final ConfigurableApplicationContext applicationContext = getApplicationContextFromServletContext(context);

        NodeInfo nodeInfo = applicationContext.getBean(NodeInfo.class);

        final String baseUrl = super.buildRedirectUrlToLoginPage(request, response, authException);
        String newUrl = baseUrl.replace("@@nodeId@@", nodeInfo.getId());

        return newUrl;
    }
}
