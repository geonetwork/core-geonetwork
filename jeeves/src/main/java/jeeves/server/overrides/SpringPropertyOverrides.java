package jeeves.server.overrides;

import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class SpringPropertyOverrides {

    List<Updater> updaters = new LinkedList<Updater>();
    private Properties properties;

    public SpringPropertyOverrides(List<Element> springOverrides, Properties properties) {
        this.properties = properties;
        for (Element element : springOverrides) {
            Updater updater = create(element);
            this.updaters.add(updater);
        }
    }

    public void onFinishedRefresh(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (Updater updater : updaters) {
            if (updater.runOnFinish()) {
                updater.update(beanFactory, properties);
            }
        }
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (Updater updater : updaters) {
            if (!updater.runOnFinish()) {
                updater.update(beanFactory, properties);
            }
        }
    }
    Updater create(Element element) {
        if("set".equalsIgnoreCase(element.getName()) || "add".equalsIgnoreCase(element.getName())) {
            return PropertyUpdater.create(element);
        } else if ("addInterceptUrl".equalsIgnoreCase(element.getName())) {
            return new AddInterceptUrlUpdater(element);
        } else if ("removeInterceptUrl".equalsIgnoreCase(element.getName())) {
            return new RemoveInterceptUrlUpdater(element);
        } else if ("SetInterceptUrl".equalsIgnoreCase(element.getName())) {
            return new SetInterceptUrlUpdater(element);
        } else {
            throw new IllegalArgumentException(element.getName()+" is not known type of updater");
        }
    }

}
