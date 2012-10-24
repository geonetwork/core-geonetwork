package jeeves.config.springutil;

import java.io.IOException;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class JeevesApplicationContext extends XmlWebApplicationContext {
	
	@Override
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader)
			throws IOException {
		reader.setValidating(false);
		super.loadBeanDefinitions(reader);
	}

}
