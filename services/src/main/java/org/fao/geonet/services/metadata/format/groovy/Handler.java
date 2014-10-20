package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

import java.io.IOException;

/**
 * Represents a Handler for handling Xml elements and returning the resulting data.
 *
 * @author Jesse on 10/15/2014.
 */
public abstract class Handler implements Comparable<Handler> {
    protected int priority = 0;
    private boolean processChildren = false;
    private Closure handlerFunction;

    public Handler(int priority, Closure handlerFunction) {
        this.priority = priority;
        this.handlerFunction = handlerFunction;

    }

    @Override
    public int compareTo(Handler o) {
        return Integer.compare(o.priority, this.priority);
    }

    public abstract boolean select(TransformationContext rootPath, GPathResult element);

    public boolean processChildren() {
        return this.processChildren && this.handlerFunction.getMaximumNumberOfParameters() > 1;
    }

    public HandlerResult handle(TransformationContext context, GPathResult elem, StringBuilder resultantXml, String childData)
            throws IOException {
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
                result = this.handlerFunction.call(elem, childData);
                break;
            case 3:
                result = this.handlerFunction.call(elem, childData, context);
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
            resultantXml.append((String) result);
        } else {
            resultantXml.append(result);
        }
    }

    protected static void createPath(GPathResult element, StringBuilder path) {
        if (element.parent() != element) {
            createPath(element.parent(), path);
            path.append(">");
        }
        path.append(element.name());
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setProcessChildren(boolean processChildren) {
        this.processChildren = processChildren;
    }
}
