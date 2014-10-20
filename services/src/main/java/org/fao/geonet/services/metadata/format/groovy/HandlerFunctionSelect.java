package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

/**
 * @author Jesse on 10/16/2014.
 */
public class HandlerFunctionSelect extends Handler {
    private final Closure matcherFunction;

    public HandlerFunctionSelect(Closure matcher, int priority, Closure handlerFunction) {
        super(priority, handlerFunction);
        this.matcherFunction = matcher;
    }

    @Override
    public boolean canHandle(TransformationContext rootPath, GPathResult elem) {
        final int maximumNumberOfParameters = this.matcherFunction.getMaximumNumberOfParameters();
        boolean matches;
        switch (maximumNumberOfParameters) {
            case 0:
                matches = (boolean) this.matcherFunction.call();
                break;
            case 1:
                matches = (boolean) this.matcherFunction.call(elem);
                break;
            case 2:
                StringBuilder path = new StringBuilder();
                createPath(elem, path);
                matches = (boolean) this.matcherFunction.call(elem, path.toString());
                break;
            default:
                throw new IllegalStateException("Too many arguments in handler '" + this + "' there are: " + maximumNumberOfParameters);
        }

        return matches;
    }


    @Override
    public String toString() {
        return "FunctionMatcher, Priority=" + super.priority;
    }

}
