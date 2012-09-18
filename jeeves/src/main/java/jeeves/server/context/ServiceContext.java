//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server.context;

import jeeves.interfaces.Logger;
import jeeves.monitor.MonitorManager;
import jeeves.server.ProfileManager;
import jeeves.server.UserSession;
import jeeves.server.resources.ProviderManager;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.server.sources.ServiceRequest.OutputMethod;
import jeeves.server.sources.http.JeevesServlet;
import jeeves.utils.Log;

import java.util.Hashtable;
import java.util.Map;

//=============================================================================

/** Contains the context for a service execution
  */

public class ServiceContext extends BasicContext
{

    private final static InheritableThreadLocal<ServiceContext> threadLocalInstance = new InheritableThreadLocal<ServiceContext>();

    /**
     * ServiceManager sets the service context thread local when dispatch is called.  this method will
     * return null or the service context
     *
     * @return the service context set by service context or null if no in an inherited thread
     */
    public static ServiceContext get() {
        return threadLocalInstance.get();
    }

    /**
     * Called to set the Service context for this thread and inherited threads
     */
    public void setAsThreadLocal() {
        threadLocalInstance.set(this);
    }


    private UserSession    userSession;
	private ProfileManager profilMan;

	private InputMethod    input;
	private OutputMethod   output;
	private Map<String, String> headers;

	private String language;
	private String service;
	private String ipAddress;
	private String uploadDir;
	private int    maxUploadSize;
	private JeevesServlet servlet;
	private boolean startupError = false;
	Map<String,String> startupErrors;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public ServiceContext(String service, MonitorManager mm, ProviderManager pm, ProfileManager p, Hashtable<String, Object> contexts)
	{
		super(mm, pm, contexts);

		profilMan    = p;
		setService(service);
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public String getLanguage()  { return language;  }
	public String getService()   { return service;   }
	public String getIpAddress() { return ipAddress; }
	public String getUploadDir() { return uploadDir; }
    public int getMaxUploadSize() { return maxUploadSize; }

	public UserSession    getUserSession()    { return userSession; }
	public ProfileManager getProfileManager() { return profilMan;   }

	public InputMethod  getInputMethod()  { return input;  }
	public OutputMethod getOutputMethod() { return output; }
	public Map<String,String> getStartupErrors() { return startupErrors; }
	public boolean isStartupError() { return startupError; }
	public boolean isServletInitialized() { 
		if (servlet != null) return servlet.isInitialized(); 
		else return true; // Jeeves not running in servlet container eg for testing
	}

	//--------------------------------------------------------------------------

	public void setLanguage (String lang)    { language  = lang;    }
	public void setServlet (JeevesServlet serv)    { servlet  = serv;    }
	public void setIpAddress(String address) { ipAddress = address; }
	public void setUploadDir(String dir)     { uploadDir = dir;     }
    public void setMaxUploadSize(int size)   { maxUploadSize = size;}
    public void setStartupErrors(Map<String,String> errs)   { startupErrors = errs; startupError = true; }

	public void setInputMethod (InputMethod m)  { input  = m; }
	public void setOutputMethod(OutputMethod m) { output = m; }

	//--------------------------------------------------------------------------

	public void setService  (String service)
	{
		this.service = service;
		logger       = Log.createLogger(Log.WEBAPP +"."+ service);
	}

	//--------------------------------------------------------------------------

	public void setUserSession(UserSession session)
	{
		userSession = session;
	}

	//--------------------------------------------------------------------------

	public void setLogger(Logger l)
	{
		logger = l;
	}

	//--------------------------------------------------------------------------

	/**
	 * @return The map of headers from the request
	 */
	public Map<String, String> getHeaders()
	{
		return headers;
	}

	/**
	 * Set the map of headers from the request
	 * @param headers The new headers to be set.
	 */
	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}

	public JeevesServlet getServlet() {
        return servlet;
    }

}

//=============================================================================

