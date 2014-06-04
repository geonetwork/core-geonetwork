package jeeves.config.springutil;

import jeeves.services.ReadWriteController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class intercepts MVC requests and verifies that:
 * <ol>
 *     <li>The system is no in readonly mode</li>
 *     <li>If the system is readonly then only allow tests <em>without</em> the {@link jeeves.services.ReadWriteController} annotation</li>
 * </ol>
 * @author Jesse on 6/4/2014.
 */
public class ReadOnlyMvcInterceptor extends HandlerInterceptorAdapter {
    public static final String SERVLET_CONTEXT_ATTR_KEY = "readOnlyMode";

    @Autowired
    ServletContext servletContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Boolean isReadOnly = (Boolean) servletContext.getAttribute(SERVLET_CONTEXT_ATTR_KEY);
            if (isReadOnly == null) {
                isReadOnly = false;
            }
            if (isReadOnly &&
                handlerMethod.getBean() != null &&
                handlerMethod.getBean().getClass().getAnnotation(ReadWriteController.class) != null) {
                throw new InReadOnlyModeException(request.getPathInfo());
            }
        }

        return super.preHandle(request, response, handler);
    }
    private static class InReadOnlyModeException extends RuntimeException {
        InReadOnlyModeException(String req) {
            super("Server is in Readonly mode, requested service is not allowed when in read only mode: " + req);
        }
    }
}
