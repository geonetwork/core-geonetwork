package jeeves.config.springutil;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception that indicates a user which was logged into one node is now visiting a different node.
 *
 * User: Jesse
 * Date: 11/26/13
 * Time: 3:56 PM
 */
public class WrongNodeAuthenticationException extends AuthenticationException {
    public WrongNodeAuthenticationException(String msg) {
        super(msg);
    }
}
