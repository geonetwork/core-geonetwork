package jeeves.server.overrides;

import java.util.Properties;

import org.springframework.context.ApplicationContext;

class ValueValueLoader implements ValueLoader {

    private String value;

    public ValueValueLoader(String value) {
        this.value = value;
    }

    @Override
    public String load(ApplicationContext context, Properties properties) {
        return this.value;
    }
    
}