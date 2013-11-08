package jeeves.server.overrides;

import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

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

    public void applyOverrides(ApplicationContext applicationContext) throws BeansException {
        for (Updater updater : updaters) {
            updater.update(applicationContext, properties);
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
