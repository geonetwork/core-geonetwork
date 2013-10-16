package jeeves.server.overrides;

import org.springframework.context.ApplicationContext;

import java.util.Properties;

interface Updater {
    public abstract Object update(ApplicationContext applicationContext, Properties properties);
}