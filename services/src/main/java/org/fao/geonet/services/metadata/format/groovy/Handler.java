package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.io.Closer;
import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.MarkupBuilder;
import org.codehaus.groovy.runtime.GStringImpl;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Represents a Handler for handling Xml elements and returning the resulting data.
 *
 * @author Jesse on 10/15/2014.
 */
public abstract class Handler implements Comparable<Handler> {
    private int priority = 0;
    private boolean processChildren = false;
    private Closure handlerFunction;

    public Handler(int priority, Closure handlerFunction) {
        this.priority = priority;
        this.handlerFunction = handlerFunction;

    }

    @Override
    public int compareTo(Handler o) {
        return Integer.compare(this.priority, o.priority);
    }

    public abstract boolean canHandle(TransformationContext rootPath, GPathResult element);

    public boolean processChildren() {
        return this.processChildren && this.handlerFunction.getMaximumNumberOfParameters() > 2;
    }

    public HandlerResult handle(TransformationContext context, GPathResult elem, StringBuilder resultantXml, String childData) throws IOException {
        final int maximumNumberOfParameters = this.handlerFunction.getMaximumNumberOfParameters();
        Object result;
        Closer closer = Closer.create();
        try {
            final StringWriter writer = closer.register(new StringWriter());
            MarkupBuilder html = new MarkupBuilder(writer);

            switch (maximumNumberOfParameters) {
                case 0:
                    result = this.handlerFunction.call();
                    break;
                case 1:
                    result = this.handlerFunction.call(elem);
                    break;
                case 2:
                    result = this.handlerFunction.call(elem, html);
                    break;
                case 3: {
                    result = this.handlerFunction.call(elem, html, childData);
                    break;
                }
                default:
                    throw new IllegalStateException("Too many arguments in handler '" + this + "' there are: " +
                                                    maximumNumberOfParameters);
            }

            final String markupString = writer.toString();
            if (!markupString.isEmpty()) {
                result = markupString;
            }
        } finally {
            closer.close();
        }

        processResult(this, result, resultantXml);
        return new HandlerResult();
    }

    /**
     * Process the result of the handler function.
     */
    public static void processResult(Object handler, Object result, StringBuilder resultantXml) throws IOException {
        if (result == null) {
            return;
        }

        if (result instanceof String) {
            resultantXml.append((String) result);
        } else if (result instanceof GStringImpl) {
            resultantXml.append(result);
        } else if (result instanceof FileResult) {
            resultantXml.append(((FileResult) result).resolve());
        } else {
            throw new AssertionError("Handler '" + handler + "' returned a result that is not recognized: \n Result Class:"
                                     + result.getClass() + "\nresult.toString():" + result);
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
