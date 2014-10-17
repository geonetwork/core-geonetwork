package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;

import java.io.IOException;

/**
 * A handler for start and end processing events.
 *
 * @author Jesse on 10/16/2014.
 */
public class StartEndHandler {
    private final Closure function;

    public StartEndHandler(Closure function) {
        this.function = function;
    }

    public void handle(StringBuilder resultantXml) throws IOException {
        if (this.function != null) {
            final Object result = this.function.call();
            Handler.processResult(result, resultantXml);
        }
    }
}
