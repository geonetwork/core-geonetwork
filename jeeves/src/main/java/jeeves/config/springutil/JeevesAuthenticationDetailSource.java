package jeeves.config.springutil;

import org.fao.geonet.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * A details source that adds the referer to the Authentication details
 * User: Jesse
 * Date: 12/4/13
 * Time: 8:12 AM
 */
public class JeevesAuthenticationDetailSource extends WebAuthenticationDetailsSource {
    @Autowired
    private NodeInfo _nodeInfo;
    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new JeevesAuthenticationDetails(context, _nodeInfo);
    }
}
