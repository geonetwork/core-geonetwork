package jeeves.config.springutil;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.geonetwork.config.MigrateConfiguration;
import org.springframework.context.Lifecycle;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class JeevesContextLoaderListener extends ContextLoaderListener {
	public JeevesContextLoaderListener() {
		System.out.println("Created JeevesContextLoader");
	}
	
	
	@Override
	protected void customizeContext(ServletContext servletContext,
			ConfigurableWebApplicationContext applicationContext) {
	}
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		String appPath = event.getServletContext().getRealPath("/");

		if (!appPath.endsWith(File.separator)) {
			appPath += File.separator;
		}

		String configPath = appPath + "WEB-INF" + File.separator;

		// migrate from old configuration to new spring configuration if needed
		new MigrateConfiguration().migrate(configPath, true);
		
		super.contextInitialized(event);
		Lifecycle context = (Lifecycle) WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
		context.start();
	}
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		Lifecycle context = (Lifecycle) WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
		if(context != null) {
			context.stop();
		}
		super.contextDestroyed(event);
	}
}
