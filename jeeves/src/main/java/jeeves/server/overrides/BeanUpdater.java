package jeeves.server.overrides;

import java.util.Properties;

import jeeves.utils.Log;

import org.jdom.Element;
import org.springframework.context.ApplicationContext;

abstract class BeanUpdater implements Updater {
    protected String beanName;
    public void setBeanName(Element element) {
        this.beanName = element.getAttributeValue("bean");
    }
    

    @Override
    public final Object update(ApplicationContext applicationContext, Properties properties) {
        Object bean = applicationContext.getBean(beanName);
        if(bean != null) {
            return update(applicationContext, properties, bean);
        } else {
            Log.warning(Log.JEEVES, "Unable apply override to bean: "+beanName+" because bean was not found");
            return null;
        }
        
    }


    protected abstract Object update(ApplicationContext applicationContext, Properties properties, Object bean);
    
}