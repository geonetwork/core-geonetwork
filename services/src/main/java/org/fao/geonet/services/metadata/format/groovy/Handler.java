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

        GPathResult x;
    }

    @Override
    public int compareTo(Handler o) {
        return Integer.compare(this.priority, o.priority);
    }

    public abstract boolean canHandle(String rootPath, GPathResult element);

    public boolean processChildren() {
        return this.processChildren;
    }

    public HandlerResult handle(GPathResult elem, StringBuilder resultantXml) throws IOException {
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
                Closer closer = Closer.create();
                try {
                    final StringWriter writer = closer.register(new StringWriter());
                    MarkupBuilder html = new MarkupBuilder(writer);
                    result = this.handlerFunction.call(elem, html);
                    final String markupString = writer.toString();
                    if (!markupString.isEmpty()) {
                        result = markupString;
                    }
                } finally {
                    closer.close();
                }
                break;
            default:
                throw new IllegalStateException("Too many arguments in handler '" + this + "' there are: " + maximumNumberOfParameters);
        }


        processResult(this, result, resultantXml);
        return new HandlerResult();
    }

    /**
     * Process the result of the handler function.
     */
    public static void processResult(Object handler, Object result, StringBuilder resultantXml) {
        if (result instanceof String) {
            resultantXml.append((String) result);
        } else if (result instanceof GStringImpl) {
            resultantXml.append(result);
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
}
