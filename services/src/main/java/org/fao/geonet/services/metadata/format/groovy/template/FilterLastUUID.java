package org.fao.geonet.services.metadata.format.groovy.template;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Uppercase the string.
 *
 * @author Jesse on 12/19/2014.
 */
@Component("lastUUID")
public class FilterLastUUID implements TextContentFilter {

    @Override
    public String process(TRenderContext context, String rawValue) {
        String uuid = FilterGenerateUUID.LAST_UUID.get();
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            FilterGenerateUUID.LAST_UUID.set(uuid);
        }
        return uuid;
    }

}
