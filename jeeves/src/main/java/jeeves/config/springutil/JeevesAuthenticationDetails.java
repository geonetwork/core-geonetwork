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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        JeevesAuthenticationDetails that = (JeevesAuthenticationDetails) o;

        if (_nodeInfo != null ? !_nodeInfo.equals(that._nodeInfo) : that._nodeInfo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_nodeInfo != null ? _nodeInfo.hashCode() : 0);
        return result;
    }
}
