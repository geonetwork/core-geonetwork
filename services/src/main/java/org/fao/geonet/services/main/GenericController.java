package org.fao.geonet.services.main;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Util;
import org.fao.geonet.exceptions.FileUploadTooBigEx;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jeeves.constants.Jeeves;
import jeeves.server.JeevesEngine;
import jeeves.server.UserSession;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.sources.ServiceRequest;
import jeeves.server.sources.ServiceRequestFactory;

@Controller
public class GenericController {
    public static final String USER_SESSION_ATTRIBUTE_KEY = Jeeves.Elem.SESSION;
    
    /**
     * Detect crawlers. Useful to avoid creating sessions for them.
     */
    private Pattern regex = Pattern.compile(ServiceManager.BOT_REGEXP, Pattern.CASE_INSENSITIVE);

    @RequestMapping(value = "/{lang}/{service}")
    @ResponseBody
    public void dispatch(@PathVariable String lang,
                         @PathVariable String service, HttpServletRequest request,
                         HttpServletResponse response)
            throws Exception {
        HttpSession httpSession = request.getSession(false);
        
        String userAgent = request.getHeader("user-agent");
        
        Matcher m = regex.matcher(userAgent);
        boolean notCrawler = !m.find();
        
        
        if(httpSession == null && notCrawler) {
            httpSession = request.getSession(true);
        } else if(httpSession != null && !notCrawler) {
            //Shouldn't get here, but in any case, free the memory
            request.getSession().invalidate();
            httpSession = null;
        }
        
        String ip = request.getRemoteAddr();
        // if we do have the optional x-forwarded-for request header then
        // use whatever is in it to record ip address of client
        String forwardedFor = request.getHeader("x-forwarded-for");
        if (forwardedFor != null)
            ip = forwardedFor;

        Log.info(Log.REQUEST, "==========================================================");

        Log.info(Log.REQUEST,  "HTML Request (from " + ip + ") : " + request.getRequestURI());
        if (Log.isDebugEnabled(Log.REQUEST)) {
            Log.debug(Log.REQUEST, "Method       : " + request.getMethod());
            Log.debug(Log.REQUEST, "Content type : " + request.getContentType());
            // Log.debug(Log.REQUEST, "Context path : "+ req.getContextPath());
            // Log.debug(Log.REQUEST, "Char encoding: "+
            // req.getCharacterEncoding());
            Log.debug(Log.REQUEST, "Accept       : " + request.getHeader("Accept"));
            // Log.debug(Log.REQUEST, "Server name  : "+ req.getServerName());
            // Log.debug(Log.REQUEST, "Server port  : "+ req.getServerPort());
        }

        if (Log.isDebugEnabled(Log.REQUEST)) {
            if(httpSession != null) {
                Log.debug(Log.REQUEST, "Session id is " + httpSession.getId());
            } else {
                Log.debug(Log.REQUEST, "No session created");                
            }
        }

        UserSession session = null;
        
        if(httpSession != null) {
            session = (UserSession) httpSession.getAttribute(USER_SESSION_ATTRIBUTE_KEY);
    
            if (session == null) {
                // --- create session
    
                session = new UserSession();
    
                httpSession.setAttribute(USER_SESSION_ATTRIBUTE_KEY, session);
                session.setsHttpSession(httpSession);
    
                if (Log.isDebugEnabled(Log.REQUEST))
                    Log.debug(Log.REQUEST, "Session created for client : " + ip);
            }
        }

        ServiceRequest srvReq = null;

        ApplicationContext jeevesApplicationContext = ApplicationContextHolder.get();
        JeevesEngine jeeves = jeevesApplicationContext.getBean(JeevesEngine.class);
        try {
            final Path uploadDir = jeeves.getUploadDir();
            srvReq = ServiceRequestFactory.create(request, response, uploadDir, jeeves.getMaxUploadSize());
        } catch (FileUploadTooBigEx e) {
            StringBuffer sb = new StringBuffer();
            sb.append("File upload too big - exceeds ").append(jeeves.getMaxUploadSize()).append(" Mb\n");
            sb.append("Error : ").append(e.getClass().getName()).append("\n");
            response.sendError(400, sb.toString());

            // now stick the stack trace on the end and log the whole lot
            sb.append("Stack :\n");
            sb.append(Util.getStackTrace(e));
            Log.error(Log.REQUEST, sb.toString());

        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();

            sb.append("Cannot build ServiceRequest\n");
            sb.append("Cause : ").append(e.getMessage()).append("\n");
            sb.append("Error : ").append(e.getClass().getName()).append("\n");
            response.sendError(400, sb.toString());

            // now stick the stack trace on the end and log the whole lot
            sb.append("Stack :\n");
            sb.append(Util.getStackTrace(e));
            Log.error(Log.REQUEST, sb.toString());
        }

        // --- execute request

        jeeves.dispatch(srvReq, session);
    }
}