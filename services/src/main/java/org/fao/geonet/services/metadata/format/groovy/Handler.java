package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

import java.io.IOException;

/**
 * Represents a Handler for handling Xml elements and returning the resulting data.
 *
 * @author Jesse on 10/15/2014.
 */
public abstract class Handler extends Selectable implements Comparable<Handler> {
    private Closure handlerFunction;

    public Handler(int priority, Closure handlerFunction) {
        super(priority);
        this.handlerFunction = handlerFunction;
    }

    @Override
    public int compareTo(Handler o) {
        return Integer.compare(o.priority, this.priority);
    }

    public HandlerResult handle(TransformationContext context, GPathResult elem, StringBuilder resultantXml)
            throws IOException {
        Logging.debug("Executing handler '%2$s' on element %1$s.", elem, this);
        final int maximumNumberOfParameters = this.handlerFunction.getMaximumNumberOfParameters();
        Object result;
        switch (maximumNumberOfParameters) {
            case 0:
                result = this.handlerFunction.call();
                break;
            case 1:
                result = this.handlerFunction.call(elem);
                break;
            case 2:
                result = this.handlerFunction.call(elem, context);
                break;
            default:
                throw new IllegalStateException("Too many arguments in handler '" + this + "' there are: " +
                                                maximumNumberOfParameters);
        }

        processResult(result, resultantXml);
        return new HandlerResult();
    }

    /**
     * Process the result of the handler function.
     */
    public static void processResult(Object result, StringBuilder resultantXml) throws IOException {
        if (result == null) {
            return;
        }

        if (result instanceof String) {
            resultantXml.append(result);
        } else {
            resultantXml.append(result);
        }
    }

    protected static void createPath(GPathResult element, StringBuilder path) {
        if(element != null) {
            if (element.parent() != element) {
                createPath(element.parent(), path);
                path.append(">");
            }
            path.append(element.name());
        }
    }

    @Override
    public final String extraToString() {
        return handlerExtraToString();
    }

    protected abstract String handlerExtraToString();
}
