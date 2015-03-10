package jeeves.server.overrides;

import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Properties;

abstract class BeanUpdater implements Updater {
    protected String beanName;
    public void setBeanName(Element element) {
        this.beanName = element.getAttributeValue("bean");
    }
    

    @Override
    public final void update(ConfigurableListableBeanFactory beanFactory, Properties properties) {
        try {
            BeanDefinition bean = beanFactory.getBeanDefinition(beanName);
            update(beanFactory, properties, bean);
        } catch (NoSuchBeanDefinitionException e) {
            Log.warning(Log.JEEVES, "Unable apply override to bean: "+beanName+" because bean was not found");
        }
        
    }

    @Override
    public boolean runOnFinish() {
        return false;
    }

    protected abstract void update(ConfigurableListableBeanFactory beanFactory, Properties properties, BeanDefinition bean);
    
}