package jeeves.server.overrides;

import java.util.Properties;

import org.springframework.context.ApplicationContext;

interface ValueLoader {
    Object load(ApplicationContext context, Properties properties);
}