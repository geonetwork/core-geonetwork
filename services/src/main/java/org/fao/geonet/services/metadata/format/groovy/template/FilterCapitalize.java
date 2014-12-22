package org.fao.geonet.services.metadata.format.groovy.template;

import org.springframework.stereotype.Component;

/**
 * Uppercase the string.
 *
 * @author Jesse on 12/19/2014.
 */
@Component("capitalize")
public class FilterCapitalize implements TextContentFilter {

    @Override
    public String process(TRenderContext context, String rawValue) {
        return toTitleCase(rawValue);
    }

    public static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" ");
        }

        return sb.toString().trim();
    }
}
