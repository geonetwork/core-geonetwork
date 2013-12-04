package jeeves.config.springutil;

import org.fao.geonet.NodeInfo;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * An authentications details object that adds extra information for multinode security.
 *
 * User: Jesse
 * Date: 12/4/13
 * Time: 8:14 AM
 */
public class JeevesAuthenticationDetails extends WebAuthenticationDetails {
    private final NodeInfo _nodeInfo;

    public JeevesAuthenticationDetails(HttpServletRequest context, NodeInfo nodeInfo) {
        super(context);
        this._nodeInfo = nodeInfo;
    }

    public String getNodeId() {
        return _nodeInfo.getId();
    }
}
