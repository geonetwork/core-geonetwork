package org.fao.geonet.services.metadata.format.groovy.template;

/**
 * Exception raised in the template package.
 *
 * @author Jesse on 11/29/2014.
 */
public class TemplateException extends RuntimeException {
    public TemplateException(String message) {
        super(message);
    }
    public TemplateException(Throwable e) {
        super(e);
    }
    public TemplateException(String message, Throwable e) {
        super(message, e);
    }
}
