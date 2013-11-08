package jeeves.server.overrides;

import org.springframework.context.ApplicationContext;

import java.util.Properties;

interface ValueLoader {
    Object load(ApplicationContext context, Properties properties);
}