package org.fao.geonet.services.metadata.format.groovy.template;

/**
 * Filters to apply to the result of text replacement when parsing text.  For example the text replacement of:
 *
 * <code>{{text | filter}}</code>
 *
 * Will result in the model value "text" being retrieved from the model and then the filter with the name "filter" will process the value
 * to give the final value.
 *
 * @author Jesse on 12/19/2014.
 */
public interface TextContentFilter {
    /**
     * Process the input (raw) value.
     *
     * @param context the render context this filter is operating withing
     * @param rawValue the pre-filter value
     * @return the filtered value.
     */
    String process(TRenderContext context, String rawValue);
}
