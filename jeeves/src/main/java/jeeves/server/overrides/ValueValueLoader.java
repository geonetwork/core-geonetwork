package jeeves.server.overrides;

import org.springframework.context.ApplicationContext;

import java.util.Properties;

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