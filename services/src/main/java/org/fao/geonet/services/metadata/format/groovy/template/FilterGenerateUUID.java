package org.fao.geonet.services.metadata.format.groovy.template;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Uppercase the string.
 *
 * @author Jesse on 12/19/2014.
 */
@Component("generateUUID")
public class FilterGenerateUUID implements TextContentFilter {

    static ThreadLocal<String> LAST_UUID = new InheritableThreadLocal<>();

    @Override
    public String process(TRenderContext context, String rawValue) {
        LAST_UUID.set(UUID.randomUUID().toString());

        return LAST_UUID.get();
    }

}
