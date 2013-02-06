package jeeves.server.overrides;

import java.util.Properties;

import org.springframework.context.ApplicationContext;

interface Updater {
    public abstract Object update(ApplicationContext applicationContext, Properties properties);
}