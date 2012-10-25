package jeeves.server.local;

import java.util.Collection;
import java.util.Collections;

import jeeves.guiservices.session.JeevesUser;
import jeeves.server.JeevesEngine;
import jeeves.server.ProfileManager;
import jeeves.server.UserSession;
import jeeves.utils.Xml;

import org.jdom.Element;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Provides JVM-local running/invokation of Jeeves engine.
 * <p/>
 * In normal operation Jeeves is started within a J2EE servlet
 * engine like Jetty. For testing/debugging it is very useful
 * to allow a local (minimal) instance of the Jeeves engine to be run.
 * Throught the dispatch() functions configured services can be invoked.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class LocalJeeves
{
	static private JeevesEngine jeeves;
	static private UserSession session;


	/**
	 * Main, for testing Jeeves init/exit.
	 * <p/>
	 * May use -DGN_HOME=<svn top dir> to specify root dir.
	 *
	 * @param args not required
	 */
	public static void main(String[] args)
	{
		init(args[0], args[1], args[2]);
		exit();
		p("ALL OK");
		System.exit(0);
	}

	/**
	 * Dispatch Jeeves service request.
	 *
	 * @param aRequest a Jeeves request
	 * @return a Jeeves response element
	 */
	static public Element dispatch(Element aRequest)
	{
		return dispatch(aRequest.getAttributeValue("url"), (Element) aRequest.getChildren().get(0));
	}

	/**
	 * Dispatch Jeeves service request.
	 *
	 * @param anURL  a relative service URL e.g. "/eng/xml.harvesting.start"
	 * @param params parameters
	 * @return a Jeeves response element
	 */
	static public Element dispatch(String anURL, Element params)
	{
		try
		{
			p("-------- REQUEST -------");
			p("url=" + anURL);
			p("\n" + Xml.getString(params));
			LocalServiceRequest serviceReq = LocalServiceRequest.create(anURL, params);
			jeeves.dispatch(serviceReq, session);
			if (serviceReq != null)
			{
				p("-------- RESULT -------");
				p(serviceReq.getResultString());
				return serviceReq.getResult();
			}
		} catch (Throwable t)
		{
			err("Error in service req", t);
		}

		return null;
	}

	/**
	 * Destroy local Jeeves engine.
	 */
	static public void exit()
	{
		try
		{
			p("exit...");
			if (jeeves == null)
			{
				return;
			}
			jeeves.destroy();
			p("exit OK");
		} catch (Throwable t)
		{
			err("Cannot exit ", t);
		} finally {
			jeeves = null;
		}
	}


	/**
	 * Is local Jeeves engine running ?
	 */
	static public boolean isRunning()
	{
		return jeeves != null;
	}

	/**
	 * Create local Jeeves engine.
	 */
	static public void init(String appPath, String configPath, String baseUrl)
	{
		try
		{
			p("init...");

			// Needed to explicitly select Saxon over default Xalan
			System.setProperty("javax.xml.transform.TransformerFactory",
									 "net.sf.saxon.TransformerFactoryImpl");

			// Init jeeves with proper config
			jeeves = new JeevesEngine();
			// null JeevesServlet arg because not running in servlet here
			jeeves.init(appPath, configPath, baseUrl, null); 

			// Make session with all permissions
			session = new UserSession();
			JeevesUser user = new JeevesUser(jeeves.getProfileManager())
			.setId("0")
			.setUsername("local")
			.setName("local")
			.setProfile(ProfileManager.ADMIN)
			.setEmail( "local@localhost");
		
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities() ) ;
		authentication.setDetails(user);

		if(SecurityContextHolder.getContext() == null) {
			SecurityContextHolder.createEmptyContext();
		}
		SecurityContextHolder.getContext().setAuthentication(authentication);

			// Add shutdown hook to destroy Jeeves on System.exit()
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				public void run()
				{
					exit();
				}
			});

			p("init OK");

		} catch (Throwable t)
		{
			err("Cannot init ", t);
			jeeves = null;
		}

	}

	private static void p(String s)
	{
		System.out.println("[LocalJeeves] " + s);
	}

	private static void err(String s, Throwable t)
	{
		p("ERROR: " + s + " msg=" + t.getMessage());
		t.printStackTrace();
	}
}
